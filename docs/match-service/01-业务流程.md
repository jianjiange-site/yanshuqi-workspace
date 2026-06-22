# MatchService 业务流程

## 1. 拉取 Feed

| 项 | 说明 |
|----|------|
| 触发入口 | `GET /api/v1/match/feed` → gRPC `GetTodayFeed` |
| 业务目标 | 返回下一批未 swipe 的推荐卡片 |
| 处理步骤 | 1) 检查 cards 配额是否耗尽 2) Redis LIST LPOP 3) swiped SET 二次过滤 4) mock CandidateClient 补 profile 5) 队列空则 D0 冷启动 |
| 读写 | 读：Redis feed LIST、swiped SET、quota Hash；写：LPOP 消费（不扣 quota） |
| 返回 | `MatchCard[]` + `exhausted` |
| 异常 | 配额耗尽 → exhausted=true；参数非法 → INVALID_ARGUMENT |

## 2. D0 冷启动

| 项 | 说明 |
|----|------|
| 触发入口 | FeedService LPOP 空队列时自动触发 |
| 业务目标 | 实时构建首批推荐队列 |
| 处理步骤 | MockCandidateClient 召回 DH/BH → FeedMergeService 混排(bhRatio=0.20) → RPUSH Redis |
| 读写 | 写：Redis feed LIST（append，不 DEL） |
| 返回 | 写入条数（内部） |
| 异常 | 候选不足 → 写入实际数量 |

## 3. D1 日更推荐

| 项 | 说明 |
|----|------|
| 触发入口 | `D1QueueScheduler` UTC 07:00 |
| 业务目标 | 为昨天有 swipe 的用户生成个性化日更队列 |
| 处理步骤 | Redisson 锁 → 查昨天 swipe 用户 → PreferenceBuilder → CandidateRecaller → D1Ranker → D1FeedMergeService(bhRatio=0.40) → **DEL+RPUSH** Redis |
| 读写 | 读：user_swipe_history、mock profile；写：Redis feed LIST 覆盖 |
| 返回 | 每用户写入条数 |
| 异常 | 单用户失败不影响其他；候选不足 WARN |

## 4. LEFT Swipe

| 项 | 说明 |
|----|------|
| 触发入口 | `POST /api/v1/match/swipe` direction=LEFT |
| 业务目标 | 记录左划，扣 cards 配额，不创建 match |
| 处理步骤 | Redisson 锁 → 幂等 insert swipe_history → 扣 quota → 写 swiped SET |
| 读写 | 写：user_swipe_history、quota Hash、swiped SET |
| 返回 | matchId=0 |
| 异常 | 重复 swipe → DUPLICATE_SWIPE；配额超限 → RESOURCE_EXHAUSTED |

## 5. RIGHT Swipe

| 项 | 说明 |
|----|------|
| 触发入口 | `POST /api/v1/match/swipe` direction=RIGHT |
| 业务目标 | 记录右划，BH 互划则即时 match，DH 则延迟 match |
| 处理步骤 | 同 LEFT + MatchCreationService 判断互划 / DH 延迟 |
| 读写 | 写：swipe_history、match（若互划）、outbox、swiped SET |
| 返回 | matchId（biz_id）或 0 |
| 异常 | 并发 → ABORTED |

## 6. SuperHi

| 项 | 说明 |
|----|------|
| 触发入口 | `POST /api/v1/match/super-hi` |
| 业务目标 | 扣配额/金币后立即 match |
| 处理步骤 | clientRequestId 幂等 → 扣 SuperHi 配额或 mock 扣币 → swipe_history SUPER_HI → 即时 match + outbox |
| 读写 | 写：swipe_history、match、outbox、superhi 幂等 key |
| 返回 | matchId + coinsUsed |
| 异常 | 金币不足 → FAILED_PRECONDITION；重试同 clientRequestId → 同 matchId |

## 7. BH 互划匹配

| 项 | 说明 |
|----|------|
| 触发入口 | RIGHT Swipe 后 MatchCreationService |
| 业务目标 | 双方 RIGHT 时创建 match |
| 处理步骤 | 查 target 是否对 caller RIGHT/SUPER_HI → insertIfAbsent match → createPending outbox |
| 读写 | 读/写：match、match_outbox |
| 返回 | match.biz_id |
| 异常 | 重复 pair → 返回已有 match |

## 8. DH 延迟匹配

| 项 | 说明 |
|----|------|
| 触发入口 | RIGHT 目标为 DH 且无即时互划 |
| 业务目标 | 15s–120s 随机延迟后若仍无互划则创建 match |
| 处理步骤 | TaskScheduler 延迟任务 → 再次检查互划 → insertIfAbsent |
| 读写 | 写：match、outbox（内存调度，进程重启会丢失未执行任务） |
| 返回 | 延迟后 matchId |
| 异常 | 延迟期间 target 左划则不再 match |

## 9. Outbox 重试

| 项 | 说明 |
|----|------|
| 触发入口 | `MatchOutboxRetryScheduler` 每 30s |
| 业务目标 | IM 副作用最终一致 |
| 处理步骤 | 扫 PENDING → ImClient.execute → 成功 DONE / 失败退避 / 超次 DEAD |
| 读写 | 读/写：match_outbox |
| 返回 | 无（内部） |
| 异常 | mock 连续失败 → DEAD |

## 10. 匹配列表

| 项 | 说明 |
|----|------|
| 触发入口 | `GET /api/v1/match/matches` |
| 业务目标 | 分页返回我的匹配 |
| 处理步骤 | MatchQueryService → MatchManager 查 match 表 → mock batchGetProfiles 补 partner |
| 读写 | 读：match 单表 |
| 返回 | matchId=biz_id、partner 资料、nextPageToken |
| 异常 | caller 非法 → INVALID_ARGUMENT |

## 11. 主页访问记录

| 项 | 说明 |
|----|------|
| 触发入口 | `POST /api/v1/match/visit/{targetUserId}` |
| 业务目标 | UPSERT 累加 visit_count |
| 处理步骤 | ProfileVisitService 校验 → ProfileVisitManager.upsertVisit |
| 读写 | 写：profile_visit |
| 返回 | success=true |
| 异常 | caller==target → INVALID_ARGUMENT；DB 异常向上抛（match-service 不 fail-open） |

## 12. 访问列表

| 项 | 说明 |
|----|------|
| 触发入口 | `GET /api/v1/match/visits` |
| 业务目标 | 谁访问过我 |
| 处理步骤 | ProfileVisitQueryService → listVisitors |
| 读写 | 读：profile_visit |
| 返回 | visitId=biz_id、visitCount、时间戳 |
