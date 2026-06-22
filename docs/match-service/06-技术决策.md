# MatchService 技术决策

## 1. 为什么 Feed 用 Redis LIST

**决策**：`yanshuqi:match:feed:<userId>` 使用 Redis LIST，LPOP 消费、RPUSH/DEL+RPUSH 写入。

**原因**：
- Feed 是有序队列，LIST 天然 FIFO，O(1) LPOP
- D0 append、D1 全量覆盖（DEL+RPUSH）语义清晰
- 与用户级 key 隔离，便于 per-user 队列管理

**替代**：Sorted Set（需 score 维护）、DB 分页（延迟高）

---

## 2. 为什么 LPOP 不扣配额

**决策**：`FeedService.getTodayFeed` 仅 LPOP + 过滤，不调用 `QuotaService.consume*`。

**原因**：
- 用户可能 LPOP 后未 swipe（关闭 App），若 LPOP 扣配额会误伤
- 配额语义绑定「有效 swipe 行为」，与 `SwipeService` 扣减一致
- Feed 仍用 `isCardsExhausted` 控制是否继续出卡

**替代**：LPOP 即扣 → 用户流失导致配额浪费

---

## 3. 为什么还要 swiped SET 二次过滤

**决策**：除 DB `user_swipe_history` 外，维护 Redis SET `yanshuqi:match:swiped:<userId>`。

**原因**：
- D1 生成在 UTC 07:00，队列可能含生成后、D1 前已 swipe 的用户
- SET 查询 O(1)，Feed LPOP 循环内快速过滤
- swipe 时同步写入 SET，与 DB 双写保证过滤时效

**替代**：仅 DB 过滤 → Feed 每次 LPOP 打 PG，性能差

---

## 4. 为什么配额用 Redis Hash

**决策**：`yanshuqi:match:quota:<userId>:<yyyyMMdd>` Hash 存 cards/left/right/superHi 已用数。

**原因**：
- 四类配额同 key 原子 HINCRBY
- UTC 日期后缀自然日切
- 读多写多，Redis 优于 PG 行锁

**替代**：PG 配额表 → 高并发 swipe 锁竞争

---

## 5. 为什么 Swipe 要 Redisson 锁 + DB 唯一约束

**决策**：`SwipeLockExecutor` + `uk_user_swipe_user_target`。

**分工**：
- **Redisson 锁**：同 user-target 并发请求串行，避免双扣配额、双写 match
- **DB 唯一约束**：锁失效或跨实例竞态时的最终兜底

**替代**：仅锁 → 锁超时可能双写；仅 DB → 重复请求抛异常体验差

---

## 6. 为什么 match 用 low/high 唯一约束

**决策**：`user_id_low = min(a,b)`，`user_id_high = max(a,b)`，`uk_match_pair`。

**原因**：
- A-B 与 B-A 是同一匹配对，规范化后唯一索引防重复
- 查询「我的 match」可用 `(user_id_low = me OR user_id_high = me)`

**替代**：双向两行 → 数据冗余、一致性难

---

## 7. 为什么 SuperHi 用 clientRequestId 幂等

**决策**：Redis key `yanshuqi:match:superhi:req:<userId>:<clientRequestId>` TTL 36h。

**原因**：
- 移动端网络重试会重复 POST
- SuperHi 涉及扣币 + 创建 match，必须返回同一 matchId
- client 生成 requestId，服务端无状态幂等

**替代**：仅 DB 唯一 → 扣币已发生无法返回首次结果

---

## 8. 为什么 DH 延迟用 TaskScheduler

**决策**：`DhDelayedMatchService` 使用 Spring `TaskScheduler` 内存调度 15s–120s。

**原因**：
- 教学项目规模，延迟窗口短，实现简单
- 不引入 MQ 延迟队列降低复杂度

**代价**：进程重启丢失未执行任务（见 07 文档）

**替代**：Redis ZSet 延迟队列、MQ delayed message → 生产更可靠

---

## 9. 为什么 Outbox 做最终一致性

**决策**：match 本地事务成功后写 `match_outbox` PENDING，定时任务调 ImClient。

**原因**：
- 本地 match 与远程 IM 无法同一分布式事务
- Outbox 保证「至少最终通知 IM 一次」
- 失败退避 + DEAD 可人工介入

**替代**：同步调 IM → IM 失败导致 match 回滚，用户体验差

---

## 10. 为什么 D1 不做 MMR / epsilon-greedy

**决策**：D1 使用 `PreferenceBuilder` + 规则排序 `D1Ranker`，无探索策略。

**原因**：
- 阶段目标为「日更队列闭环」，非推荐算法研究
- mock 候选有限，复杂算法收益低
- 文档与测试可验证、可讲解

**替代**：Bandit/MMR → 需大量行为数据与 A/B 基建

---

## 11. 为什么外部依赖用防腐层

**决策**：`CandidateClient` 等接口 + mock/grpc 双实现，`@ConditionalOnProperty` 切换。

**原因**：
- match-service 不 import user/payment/im 业务包
- 默认 mock 可离线全链路回归
- grpc 占位待 proto 补齐后切换，不改业务 Service

**替代**：Service 内直接调 gRPC → 测试耦合、切换困难
