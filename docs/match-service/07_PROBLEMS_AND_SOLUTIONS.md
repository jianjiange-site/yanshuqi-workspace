# MatchService 问题与方案

## 1. 重复 swipe

| 项 | 内容 |
|----|------|
| 问题背景 | 用户双击、网络重试导致同一 target 多次 swipe |
| 为什么难 | 需区分「幂等返回」与「新业务」，且不能双扣配额 |
| 当前方案 | DB `uk_user_swipe_user_target` + 先查后写；重复返回 matchId=0 |
| 替代方案 | 仅客户端去重 |
| 最终取舍 | DB 权威 + 应用层先查，简单可靠 |
| 剩余风险 | 极端并发下先查均 miss 仍可能一条失败（唯一约束兜底） |

## 2. 配额超扣

| 项 | 内容 |
|----|------|
| 问题背景 | 并发 swipe 可能导致 cards 配额超过订阅上限 |
| 为什么难 | 读-改-写非原子 |
| 当前方案 | Redis HINCRBY + swipe 前 `QuotaService` 校验剩余 |
| 替代方案 | Lua 脚本原子 check-and-incr |
| 最终取舍 | HINCRBY + 前置校验，教学规模足够 |
| 剩余风险 | 极高并发下可能短暂超限（可加强 Lua） |

## 3. 并发 swipe（同 user-target）

| 项 | 内容 |
|----|------|
| 问题背景 | 两请求同时 RIGHT 同一 target |
| 为什么难 | 可能双创建 match、双扣配额 |
| 当前方案 | `SwipeLockExecutor` Redisson 锁 `(userId, targetUserId)` |
| 替代方案 | 仅 DB 唯一约束 |
| 最终取舍 | 锁串行化 + DB 兜底 |
| 剩余风险 | 锁 lease 过期需合理配置 |

## 4. 重复 match

| 项 | 内容 |
|----|------|
| 问题背景 | BH 互划与 SuperHi、DH 延迟可能多次触发创建 |
| 为什么难 | 多入口、多线程 |
| 当前方案 | `MatchManager.insertIfAbsent` + `uk_match_pair` |
| 替代方案 | 应用层全局锁 |
| 最终取舍 | low/high 规范化 + insertIfAbsent 返回已有 biz_id |
| 剩余风险 | 低 |

## 5. SuperHi 网络重试

| 项 | 内容 |
|----|------|
| 问题背景 | 扣币成功但响应丢失，客户端重试 |
| 为什么难 | 不能二次扣币、不能两个 match |
| 当前方案 | `clientRequestId` + Redis 幂等存储 36h |
| 替代方案 | 服务端生成 idempotency-key |
| 最终取舍 | 客户端传 clientRequestId，符合移动端实践 |
| 剩余风险 | TTL 外重试视为新请求 |

## 6. DH 延迟任务丢失

| 项 | 内容 |
|----|------|
| 问题背景 | 进程重启后内存 TaskScheduler 任务消失 |
| 为什么难 | 无持久化延迟队列 |
| 当前方案 | 接受教学范围风险；文档明确限制 |
| 替代方案 | Redis ZSet / MQ 延迟消息 |
| 最终取舍 | 简单实现优先 |
| 剩余风险 | **生产需换持久化调度** |

## 7. IM 副作用失败

| 项 | 内容 |
|----|------|
| 问题背景 | match 已落库，ImClient 超时失败 |
| 为什么难 | 跨服务无 2PC |
| 当前方案 | match_outbox PENDING → 定时重试 → DONE/DEAD |
| 替代方案 | 同步调 IM，失败回滚 match |
| 最终取舍 | Outbox 最终一致，match 对用户可见 |
| 剩余风险 | DEAD 需运维补偿；当前 mock IM |

## 8. Feed 里出现已 swipe 用户

| 项 | 内容 |
|----|------|
| 问题背景 | D1 队列生成后用户又 swipe，队列未重建 |
| 为什么难 | 不能每次 Feed 全量重建 |
| 当前方案 | LPOP 后 swiped SET 过滤 + 跳过已 swipe |
| 替代方案 | swipe 时从 LIST 删除（LREM O(n)） |
| 最终取舍 | SET 二次过滤，实现简单 |
| 剩余风险 | 可能多 LPOP 几次才凑满 count |

## 9. D1 多实例重复执行

| 项 | 内容 |
|----|------|
| 问题背景 | 多 pod 同时跑 D1 定时任务 |
| 为什么难 | 重复 DEL+RPUSH 浪费但可能覆盖 |
| 当前方案 | `yanshuqi:lock:match:d1:<yyyyMMdd>` Redisson 锁 |
| 替代方案 | 单实例调度 / K8s CronJob |
| 最终取舍 | 分布式锁 |
| 剩余风险 | 锁 holder 崩溃需 waitTime 后其他实例接管 |

## 10. mock 外部依赖如何切真实 RPC

| 项 | 内容 |
|----|------|
| 问题背景 | 联调需真实 user/payment/im |
| 为什么难 | payment/im proto 未就绪；user 缺 listCandidates RPC |
| 当前方案 | `app.match.external.*-client-mode=grpc` + 占位 Client |
| 替代方案 | 直连业务包（违反边界） |
| 最终取舍 | 防腐层 + 配置切换；未实现 RPC 抛 UNIMPLEMENTED |
| 剩余风险 | **真实联调需上游 proto 与 RPC 补齐**，见 STAGE8 清单 |
