# MatchService 面试复盘材料

## 1. 两分钟项目介绍

MatchService 是 Dating App 的匹配域微服务，负责推荐 Feed、划卡、配额、匹配关系和主页访问。App 经 mobile-gateway 调 REST，gateway 解析 JWT 得到 callerUserId 后 gRPC 转发到 match-service。核心存储是 PostgreSQL（四张业务表）和 Redis（Feed LIST、配额 Hash、swiped SET、分布式锁）。Swipe 支持 LEFT/RIGHT 和 SuperHi；BH 用户互划即时 match，DH 用户延迟 match；副作用通过 Outbox 异步调 IM（当前 mock）。D0 实时冷启动、D1 UTC 07:00 日更推荐。外部 user/payment/im 通过防腐层隔离，默认 mock 可完整回归，grpc 占位待 proto 补齐。

## 2. 我负责的核心工作

- 设计并实现 match 域 proto + gateway REST 双层契约
- PostgreSQL 四表 + Redis 多结构的数据模型与 Manager 单表访问
- Swipe 配额、幂等、Redisson 锁、match low/high 唯一约束
- Feed LPOP + swiped 二次过滤 + D0/D1 推荐队列
- Outbox 最终一致性与 DH 延迟匹配
- 外部依赖防腐层 mock/grpc 可切换

## 3. 简历 Bullet（3–5 条）

- 独立负责 Match 微服务核心链路：Feed 推荐队列、Swipe/SuperHi、BH/DH 匹配策略，gRPC + REST 双层 API，日活配额 Redis Hash 原子扣减。
- 设计 PostgreSQL match 低/高 user_id 规范化唯一约束 + Redisson 分布式锁，解决并发 swipe 重复 match 与配额超扣。
- 实现 Redis LIST Feed + SET 二次过滤 + D1 定时 DEL+RPUSH 日更推荐，LPOP 与 swipe 配额解耦，避免无效扣配额。
- 基于 Outbox 模式实现 match 与 IM 副作用最终一致，支持退避重试与 DEAD 状态。
- 防腐层隔离 user/payment/im 依赖，mock/grpc 配置切换，151+ 单元测试覆盖核心业务。

## 4. 高频追问与回答

### 为什么用 Redis LIST 做 Feed？

Feed 是有序 FIFO 队列，LIST LPOP/RPUSH O(1)。D0 append、D1 全量覆盖用 DEL+RPUSH 语义清晰，per-user key 隔离好。

### 为什么 LPOP 不扣配额？

用户可能拿到卡片但未 swipe。配额应对「有效 swipe 行为」扣减，否则关闭 App 也会消耗 cards 配额。

### 怎么保证 Swipe 幂等？

`(user_id, target_user_id)` DB 唯一约束 + swipe 前先查 history；重复请求返回 matchId=0，不二次扣配额。

### Redisson 锁和 DB 唯一约束分别解决什么？

锁：同 user-target 并发串行，避免双扣配额、双写 match。唯一约束：锁失效或跨实例竞态的最终兜底。

### SuperHi 为什么要 clientRequestId？

移动端网络重试会重复 POST。SuperHi 涉及扣币和创建 match，clientRequestId + Redis 幂等 key 保证同一请求返回同一 matchId。

### match low/high 设计解决什么？

A-B 和 B-A 是同一对。规范化后 `(user_id_low, user_id_high)` 唯一索引防重复 match，查询也简单。

### DH 延迟匹配为什么用内存调度？

教学项目延迟窗口 15s–120s，TaskScheduler 实现简单。代价是进程重启丢任务，生产应换 Redis ZSet 或 MQ 延迟队列。

### Outbox 解决什么一致性问题？

本地 match 事务成功与远程 IM 无法 2PC。Outbox 先落库 PENDING，异步重试 IM，保证最终一致。

### D1 推荐怎么做？

UTC 07:00 定时任务，Redisson 日锁 → 查昨日 swipe 用户 → PreferenceBuilder 建偏好 → mock 召回 → D1Ranker 排序 → bhRatio=0.40 混排 → Redis DEL+RPUSH 覆盖 feed。

### mock 外部服务怎么切真实服务？

配置 `app.match.external.user-client-mode=grpc` 等。grpc Client 已占位；payment/im proto 未就绪、user listCandidates 未实现时会 UNIMPLEMENTED。需上游 proto 补齐后联调。

---

## 5. 诚实边界（面试必说）

- 当前 **mock 模式** 可完整回归，**非全生产真实联调**
- payment/im 真实 RPC 待 proto
- user 批量资料可走 BatchGetRecommendProfiles，候选召回 RPC 仍有缺口
- Testcontainers 集成测试需 Docker
