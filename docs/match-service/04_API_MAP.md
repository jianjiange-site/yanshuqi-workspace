# MatchService 接口地图

## 通用约定

| 项 | 说明 |
|----|------|
| App 不传 userId | 由 gateway 从 JWT 解析 `callerUserId` |
| 对外 ID | `matchId` = `match.biz_id`；`visitId` = `profile_visit.biz_id` |
| REST 包装 | `Result<T>` 统一响应 |
| gRPC 错误 | `MatchGrpcExceptionAdvice` + `MatchErrorCode` 映射 Status |

---

## REST 接口（mobile-gateway）

Base path：`/api/v1/match`

| 方法 | 路径 | 说明 | gRPC 映射 |
|------|------|------|-----------|
| GET | `/feed?count=5` | 拉当日 Feed 下一批 | `GetTodayFeed` |
| POST | `/swipe` | LEFT / RIGHT 划卡 | `Swipe` |
| POST | `/super-hi` | SuperHi | `SuperHi` |
| GET | `/quota` | 配额查询 | `GetQuota` |
| GET | `/matches?pageSize&pageToken` | 匹配列表 | `ListMatches` |
| POST | `/visit/{targetUserId}` | 记录主页访问 | `RecordVisit` |
| GET | `/visits?pageSize&pageToken` | 访问列表 | `ListVisits` |

### 请求体示例

**SwipeReq**：`targetUserId`, `direction`（LEFT/RIGHT）

**SuperHiReq**：`targetUserId`, `clientRequestId`

---

## gRPC 接口（match-service）

Proto：`proto/match/match_service.proto`

| RPC | Request 关键字段 | Response 关键字段 |
|-----|------------------|-------------------|
| `GetTodayFeed` | callerUserId, count | cards[], exhausted |
| `Swipe` | callerUserId, targetUserId, direction | matchId |
| `SuperHi` | callerUserId, targetUserId, clientRequestId | matchId, coinsUsed |
| `GetQuota` | callerUserId | subscriptionTier, cards/left/right/superHi 已用剩余 |
| `ListMatches` | callerUserId, pageSize, pageToken | matches[], nextPageToken |
| `RecordVisit` | callerUserId, targetUserId | success |
| `ListVisits` | callerUserId, pageSize, pageToken | visits[], nextPageToken |

---

## Gateway 实现类

| 类 | 职责 |
|----|------|
| `MatchController` | REST 入口 |
| `CallerUserResolver` | JWT → callerUserId |
| `MatchGrpcClient` | gRPC stub 调用 |
| `MatchProtoAdapter` | Proto ↔ VO |

## Match-Service 实现类

| 类 | 职责 |
|----|------|
| `MatchGrpcService` | gRPC 入口，委托各 Service |
| `FeedService` | GetTodayFeed |
| `SwipeService` | Swipe / SuperHi |
| `QuotaService` | GetQuota |
| `MatchQueryService` | ListMatches |
| `ProfileVisitService` | RecordVisit |
| `ProfileVisitQueryService` | ListVisits |

---

## 错误码（节选）

| MatchErrorCode | gRPC Status | 场景 |
|----------------|-------------|------|
| INVALID_ARGUMENT | INVALID_ARGUMENT | 参数非法 |
| DUPLICATE_SWIPE | ALREADY_EXISTS | 重复 swipe（幂等返回 matchId=0） |
| QUOTA_EXHAUSTED | RESOURCE_EXHAUSTED | 配额耗尽 |
| INSUFFICIENT_COINS | FAILED_PRECONDITION | SuperHi 金币不足 |
| EXTERNAL_RPC_NOT_IMPLEMENTED | UNIMPLEMENTED | grpc 占位未实现 |

完整映射见 `MatchGrpcStatusMapper`。
