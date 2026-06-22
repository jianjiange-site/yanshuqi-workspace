# MatchService 数据模型

## PostgreSQL（schema: match_center）

### user_swipe_history

记录用户对目标的划卡行为，支撑幂等与 BH 互划判断。

| 字段 | 说明 |
|------|------|
| id | 内部主键 |
| user_id | 划卡者 |
| target_user_id | 目标用户 |
| direction | LEFT / RIGHT / SUPER_HI |
| created_at | 创建时间 |

**唯一约束**：`(user_id, target_user_id)` — 防止重复 swipe。

### match

匹配关系表（PostgreSQL 保留字，表名 `"match"`）。

| 字段 | 说明 |
|------|------|
| id | 内部主键 |
| biz_id | **对外 matchId**（Snowflake） |
| user_id_low / user_id_high | 规范化用户对（low < high） |
| source | BH_MUTUAL / SUPER_HI / DH_DELAYED 等 |
| matched_at | 匹配时间 |

**唯一约束**：`(user_id_low, user_id_high)` — 防止重复 match。

### match_outbox

IM 副作用 Outbox，最终一致性。

| 字段 | 说明 |
|------|------|
| id | 内部主键 |
| match_id | 关联 match.id |
| status | PENDING / DONE / DEAD |
| retry_count | 重试次数 |
| next_retry_at | 下次重试时间 |

### profile_visit

主页访问记录。

| 字段 | 说明 |
|------|------|
| id | 内部主键 |
| biz_id | **对外 visitId** |
| target_user_id | 被访问者 |
| from_user_id | 访问者 |
| visit_count | 累计次数 |
| first_visited_at / last_visited_at | 首次/最近访问 |

**唯一约束**：`(from_user_id, target_user_id)` — UPSERT 累加。

## id + biz_id 规范

| 场景 | 规则 |
|------|------|
| 内部关联 | 使用表 `id`（自增/Snowflake 内部） |
| 对外 API | `matchId` = `match.biz_id`；`visitId` = `profile_visit.biz_id` |
| gRPC / REST | 不传内部 id，App 只见 biz_id |

实现见 `MatchCreationService`、`MatchQueryService`、`ProfileVisitManager`。

## Redis Key 清单

定义于 `RedisKeyConstants.java`：

| Key | 格式 | 结构 | TTL/说明 |
|-----|------|------|----------|
| Feed | `yanshuqi:match:feed:<userId>` | LIST | 当日推荐队列 |
| Quota | `yanshuqi:match:quota:<userId>:<yyyyMMdd>` | Hash | UTC 日切，fields: cards/left/right/superHi |
| Swiped | `yanshuqi:match:swiped:<userId>` | SET | 已 swipe targetUserId |
| Pref | `yanshuqi:match:pref:<userId>` | String(JSON) | D1 偏好快照 |
| SuperHi 幂等 | `yanshuqi:match:superhi:req:<userId>:<clientRequestId>` | String | TTL 36h |
| Swipe 锁 | `yanshuqi:lock:match:swipe:<userId>:<targetUserId>` | Lock | Redisson |
| D1 锁 | `yanshuqi:lock:match:d1:<yyyyMMdd>` | Lock | 防多实例重复 D1 |

**工程约束**：所有 key 必须以 `yanshuqi` 前缀开头。
