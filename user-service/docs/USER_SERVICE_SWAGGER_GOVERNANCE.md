# USER-09-5：缓存、异常、日志工程治理

本阶段仅做 user-service 工程治理收口，不新增业务 RPC、不修改 proto 语义。

## 1. USER-09 缓存策略总览

| 缓存 Key | 用途 | 读写 |
|---|---|---|
| `yanshuqi:user:basic:{user_id}` | 用户基础展示信息 | BatchGetBasicProfiles 读；写路径失效 |
| `yanshuqi:user:profile:{user_id}` | 推荐侧资料快照 | BatchGetRecommendProfiles 读；写路径失效 |
| `yanshuqi:user:status:{user_id}` | 账号/资料状态 | BatchGetUserStatuses 读；写路径失效 |
| `yanshuqi:user:profile_view:{user_id}` | Swagger 资料视图（预留） | 写路径统一失效；读路径当前直查 DB |

**TTL**：默认 600 秒（`UserCacheProperties.profileTtlSeconds`）。

**降级**：`CacheSafeExecutor` 读失败视为 miss 回源 DB；写/删失败仅 warn，不回滚事务。

## 2. Redis Key 清单

| Key | TTL | 读写场景 | 失效时机 |
|---|---|---|---|
| `yanshuqi:user:basic:{id}` | 600s | 批量基础信息读 | 资料写、头像确认、绑图、首次 ResolveOrCreate |
| `yanshuqi:user:profile:{id}` | 600s | 批量推荐资料读 | 同上 |
| `yanshuqi:user:status:{id}` | 600s | 批量状态读 | 同上 |
| `yanshuqi:user:profile_view:{id}` | 600s（若写入） | 预留 | 同上 |

## 3. 写操作缓存失效表

| 写操作 | 失效 Key |
|---|---|
| ResolveOrCreate*（**仅 newlyCreated**） | basic / profile / status / profile_view |
| ResolveOrCreate*（复登） | 不失效 |
| UpsertOnboarding | 四类全删 |
| UpdateProfile | 四类全删 |
| ConfirmAvatarUpload | 四类全删 |
| BindUserPhoto | 四类全删 |
| 其他资料写操作 | 统一 `UserCacheInvalidationService.evictProfileCache` |

## 4. 为什么不新增 home_card 组合缓存

`yanshuqi:user:home_card:{self}:{target}` 在 target 资料更新时难以精准失效（self-target 笛卡尔积），本阶段不引入；`GetHomeCardProfile` 直查 DB。

## 5. Redis 降级策略

- `safeMultiGet` 异常 → 返回 null（全 miss）→ 回源 DB
- `safeSet` 异常 → 返回 false，主流程继续
- `safeDelete` / `safeDeleteAll` 异常 → warn 日志，不回滚
- 反序列化坏 JSON → 删坏 key + 回源 DB
- 禁止 `FLUSHDB` / `FLUSHALL`；`safeDeleteAll` 过滤危险 key 名

## 6. USER-09 错误码总表（节选）

### Auth（USER-09-1）

| 错误码 | gRPC Status |
|---|---|
| INVALID_DEVICE_ID | INVALID_ARGUMENT |
| INVALID_PLATFORM | INVALID_ARGUMENT |
| INVALID_PHONE | INVALID_ARGUMENT |
| INVALID_SMS_CODE | INVALID_ARGUMENT |
| INVALID_THIRD_PARTY_PLATFORM | INVALID_ARGUMENT |
| INVALID_THIRD_PARTY_IDENTITY | UNAUTHENTICATED |
| USER_CONCURRENT_CONFLICT | ABORTED |

### Profile（USER-09-2）

| 错误码 | gRPC Status |
|---|---|
| INVALID_NICKNAME / GENDER / BIRTHDAY / AGE / HEIGHT / BIO | INVALID_ARGUMENT |
| PROFILE_UPDATE_INVALID | INVALID_ARGUMENT |
| PROFILE_UPDATE_FAILED | ABORTED |
| PROFILE_NOT_FOUND | NOT_FOUND |

### Upload（USER-09-3）

| 错误码 | gRPC Status |
|---|---|
| INVALID_AVATAR_EXT / AVATAR_SIZE_EXCEEDED / INVALID_AVATAR_OBJECT_KEY | INVALID_ARGUMENT |
| AVATAR_OBJECT_NOT_BELONG_TO_USER | PERMISSION_DENIED |
| AVATAR_OBJECT_NOT_FOUND | NOT_FOUND |
| AVATAR_OBJECT_STAT_FAILED / AVATAR_PRESIGN_FAILED | UNAVAILABLE |
| AVATAR_CONFIRM_FAILED | ABORTED |

### HomeCard（USER-09-4）

| 错误码 | gRPC Status |
|---|---|
| INVALID_USER_ID / INVALID_TARGET_USER_ID | INVALID_ARGUMENT |
| TARGET_USER_NOT_FOUND | NOT_FOUND |
| TARGET_USER_UNAVAILABLE | PERMISSION_DENIED |
| HOME_CARD_QUERY_FAILED | ABORTED |

### 通用

| 错误码 | gRPC Status |
|---|---|
| USER_NOT_FOUND | NOT_FOUND |
| USER_DISABLED / USER_BANNED | PERMISSION_DENIED |
| INTERNAL_ERROR | INTERNAL |

完整映射见 `UserGrpcStatusMapper`。

## 7. gRPC Status 映射分层原则

- 参数非法 → `INVALID_ARGUMENT`
- 身份非法 → `UNAUTHENTICATED`
- 资源不存在 → `NOT_FOUND`
- 账号/对象不可用 → `PERMISSION_DENIED`
- 对象存储不可用 → `UNAVAILABLE`
- 并发/写冲突 → `ABORTED`
- 未知/系统错误 → `INTERNAL`

## 8. 日志脱敏字段清单

**禁止明文**：phone、smsCode、idToken、googleEmail、pushToken、deviceId、refreshToken、accessToken、presignedUrl、password、passwordHash、identityHash。

**允许打印**：user_id、identity_type、platform、masked_phone、masked_device_id、object_key（缩写）、error_code、grpc_method、cost_ms。

工具类：`SensitiveLogUtil`、`LogMaskUtil`、`IdentityHashService.maskDeviceIdForLog`。

## 9. 慢调用日志规则

- **阈值**：500ms（`UserCacheProperties.slowCallThresholdMs`）
- **覆盖 RPC**：ResolveOrCreateDeviceUser / Phone / ThirdParty、UpsertOnboarding、GetUserProfileView、UpdateProfile、PresignAvatarUpload、ConfirmAvatarUpload、GetHomeCardProfile 及原有注册/登录/绑图/批量查询
- **输出字段**：rpc_method、user_id（如有）、cost_ms、success、error_code（失败时）

## 10. 测试与验收命令

```bash
cd user-service
mvn clean test
mvn clean package -DskipTests

# live 验收（需服务 + grpcurl；Redis 检查需 redis-cli）
python scripts/run_user_09_5_governance_verify.py

# USER-09 分阶段回归
python scripts/run_user_09_1_auth_login_source_verify.py
python scripts/run_user_09_2_profile_onboarding_verify.py
python scripts/run_user_09_3_avatar_upload_verify.py
python scripts/run_user_09_4_home_card_verify.py
```

治理单元测试：`UserRedisKeyConstantsTest`、`UserCacheInvalidationPolicyTest`、`UserCacheSafeExecutorTest`、`UserGrpcStatusMapperSwaggerErrorTest`、`UserSensitiveLogMaskTest`、`UserSlowCallLoggerTest`、`User09RegressionVerifyTest`。
