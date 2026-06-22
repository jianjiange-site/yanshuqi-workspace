# MatchService 分阶段复盘

## 阶段 0：基线扫描

| 项 | 内容 |
|----|------|
| 实现内容 | monorepo 骨架、match-service 空工程、工程约束 |
| 核心文件 | `docs/ENGINEERING_RULES.md`、`match-service/pom.xml` |
| 验收结果 | 服务可启动、健康检查 |
| 遗留风险 | 无业务表 |

## 阶段 1：proto + REST 契约骨架

| 项 | 内容 |
|----|------|
| 实现内容 | `match_service.proto` 7 RPC；gateway MatchController 转发骨架 |
| 核心文件 | `proto/match/match_service.proto`、`MatchController`、`MatchGrpcClient` |
| 验收结果 | REST 可调通（mock/stub 响应） |
| 遗留风险 | 无真实业务逻辑 |

## 阶段 2：数据模型

| 项 | 内容 |
|----|------|
| 实现内容 | Flyway 四表；Entity + Manager 单表访问 |
| 核心文件 | `V20260621_001__create_match_core_tables.sql`、`*Manager.java` |
| 验收结果 | 迁移可执行、单测覆盖 Manager |
| 遗留风险 | 无 Redis 结构 |

## 阶段 3：配额与 Swipe

| 项 | 内容 |
|----|------|
| 实现内容 | QuotaService、SwipeService、Redisson 锁、swiped SET |
| 核心文件 | `QuotaService`、`SwipeService`、`SwipeLockExecutor`、`RedisKeyConstants` |
| 验收结果 | LEFT/RIGHT 幂等、配额扣减 |
| 遗留风险 | 无 match 创建 |

## 阶段 4：匹配触发与 Outbox

| 项 | 内容 |
|----|------|
| 实现内容 | MatchCreationService、BH 互划、SuperHi、DH 延迟、Outbox 重试 |
| 核心文件 | `MatchCreationService`、`DhDelayedMatchService`、`MatchOutboxRetryService` |
| 验收结果 | match + outbox 单测、互划/延迟场景 |
| 遗留风险 | IM mock；DH 内存调度 |

## 阶段 5：Feed 与 D0

| 项 | 内容 |
|----|------|
| 实现内容 | FeedService LPOP、ColdStartService、FeedMerge |
| 核心文件 | `FeedService`、`ColdStartService`、`FeedQueueRepository` |
| 验收结果 | Feed 出卡、D0 冷启动、LPOP 不扣配额 |
| 遗留风险 | 候选 mock |

## 阶段 6：查询接口收口

| 项 | 内容 |
|----|------|
| 实现内容 | ListMatches、RecordVisit、ListVisits 接真实 DB |
| 核心文件 | `MatchQueryService`、`ProfileVisitService`、`ProfileVisitQueryService` |
| 验收结果 | gRPC + REST 查询链路 |
| 遗留风险 | partner 资料 mock  enrichment |

## 阶段 7：D1

| 项 | 内容 |
|----|------|
| 实现内容 | D1Generator、PreferenceBuilder、D1Ranker、D1QueueScheduler |
| 核心文件 | `D1Generator`、`D1QueueScheduler`、`PreferenceBuilder` |
| 验收结果 | DEL+RPUSH、分布式锁、单测 |
| 遗留风险 | 无 MMR；候选 mock |

## 阶段 8：外部服务防腐层

| 项 | 内容 |
|----|------|
| 实现内容 | client 接口 mock/grpc 双实现、配置切换、占位 gRPC Client |
| 核心文件 | `client/mock/*`、`client/grpc/*`、`MatchProperties.external` |
| 验收结果 | verify-match-stage8 通过；默认 mock |
| 遗留风险 | payment/im proto 缺失；user listCandidates 未实现 |

## 阶段 9：最终文档与验收

| 项 | 内容 |
|----|------|
| 实现内容 | 10 份交付文档、verify-match-final 脚本、README 索引 |
| 核心文件 | `docs/match-service/00-10*.md`、`scripts/verify-match-final.*` |
| 验收结果 | 全量 mvn test + 文档齐全 |
| 遗留风险 | Testcontainers 需 Docker；真实 RPC 联调待上游 |
