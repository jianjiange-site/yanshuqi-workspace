# USER-09-1：Auth 登录来源适配

> 模块：`user-service`  
> 阶段：USER-09-1  
> 范围：仅 user-service gRPC 内部能力，不含 mobile-gateway、JWT、短信、OAuth verify

## 1. 本阶段实现了什么

补齐 Swagger Auth 三种登录来源在 user-service 侧的内部支撑能力，供未来 mobile-gateway 调用：

| gRPC RPC | 对应 Swagger REST |
|---|---|
| `ResolveOrCreateDeviceUser` | `POST /api/v1/auth/login-device` |
| `ResolveOrCreatePhoneUser` | `POST /api/v1/auth/login-phone` |
| `ResolveOrCreateThirdPartyUser` | `POST /api/v1/auth/login-third-party` |

统一返回 `ResolveOrCreateLoginUserResponse`：

```text
user_id
newly_created
pending
account_status
profile_status
token_version
last_login_at
```

## 2. user-service 负责 vs 不负责

### user-service 负责

| 字段 / 能力 | 说明 |
|---|---|
| deviceId + platform | 设备匿名身份解析 / 创建 |
| phone | 手机号归一化、identity_hash、用户解析 / 创建 |
| thirdPartyPlatform + idToken | 三方身份解析 / 创建（idToken 哈希） |
| device 信息 | user_devices upsert（platform / device_fingerprint / push_token_hash / app_version） |
| last_login_at | users + user_auth_identities 更新 |
| newly_created | 首次自动建档 true，复登 false |
| pending | 由 profile_status / profile_completed 推导 |
| account_status / token_version | 供 gateway 签发 JWT 前校验 |

### user-service 不负责

| 字段 / 能力 | 负责方 |
|---|---|
| accessToken / refreshToken | mobile-gateway |
| JWT RS256 签发 | mobile-gateway |
| refresh token 轮换 | mobile-gateway |
| logout jti 黑名单 | mobile-gateway |
| send-sms-code 短信发送 | gateway 或独立短信服务 |
| 真实 OAuth idToken verify | gateway 或 auth 适配层 |
| REST Controller | mobile-gateway |

## 3. 三种登录来源调用链

### 3.1 设备匿名登录

```text
mobile-gateway POST /api/v1/auth/login-device
→ gRPC ResolveOrCreateDeviceUser
→ ResolveOrCreateDeviceUserCommand
→ UserAuthServiceImpl.resolveOrCreateDeviceUser
→ IdentityHashService.normalizeDeviceLoginIdentity(platform, deviceId)
→ identity_hash = hash("DEVICE", "IOS:deviceId")
→ UserAuthIdentityManager.findByIdentityTypeAndHash
→ [不存在] 事务创建 users / user_auth_identities / user_profiles / user_settings
→ UserDeviceManager upsert
→ UserManager.updateLastLoginAt
→ LoginPendingCalculator.computePending
→ ResolveOrCreateLoginUserResponse
```

### 3.2 手机号登录

```text
mobile-gateway POST /api/v1/auth/login-phone
→ gRPC ResolveOrCreatePhoneUser
→ SmsCodeValidator.validate（本阶段仅非空校验）
→ phone 归一化 + identity_hash
→ resolveOrCreateIdentityUser（同上）
→ Response
```

### 3.3 三方登录

```text
mobile-gateway POST /api/v1/auth/login-third-party
→ gRPC ResolveOrCreateThirdPartyUser
→ ThirdPartyPlatform.fromPlatformCode(1/2/3)
→ idToken SHA-256 作为 normalized identity
→ identity_type = GOOGLE / APPLE / FACEBOOK
→ resolveOrCreateIdentityUser（同上）
→ Response
```

## 4. identity_type 设计

| identity_type | 来源 | identity_hash 输入 |
|---|---|---|
| `DEVICE` | 设备匿名登录 | `DEVICE:{platform}:{deviceId}` |
| `PHONE` | 手机号登录 | `PHONE:{normalizedPhone}` |
| `GOOGLE` | thirdPartyPlatform=1 | `GOOGLE:{sha256(idToken)}` |
| `APPLE` | thirdPartyPlatform=2 | `APPLE:{sha256(idToken)}` |
| `FACEBOOK` | thirdPartyPlatform=3 | `FACEBOOK:{sha256(idToken)}` |

- 查询主键一律使用 `identity_hash`，禁止 phone / deviceId / idToken 明文查询。
- `user_auth_identities.identity_value` 存脱敏值。
- 登录来源创建的身份 `password_hash = NULL`，`verified = 1`。

## 5. newly_created / pending 语义

### newly_created

| 场景 | 值 |
|---|---|
| 首次命中 identity_hash 不存在，事务创建用户 | `true` |
| identity 已存在，复登 | `false` |
| 并发创建唯一索引冲突后回读已有 identity | `false` |

### pending

```text
profile_status == COMPLETED → pending = false
否则 pending = true（结合 profile_completed == 0 兜底）
```

新用户默认 `profile_status = INIT`，`profile_completed = 0`，故 `pending = true`。

## 6. 为什么 user-service 不签发 JWT

Swagger 描述 mobile-gateway 为 BFF，鉴权域（JWT RS256 + refresh 轮换 + logout 黑名单）并入网关。user-service 只负责：

1. 解析 / 创建用户身份  
2. 校验 account_status  
3. 返回 token_version 供 gateway 写入 JWT claims  
4. 更新 last_login_at 与设备记录  

这与现有 `VerifyLogin` / `Register` 边界一致。

## 7. 异常码

| 错误码 | gRPC Status |
|---|---|
| INVALID_DEVICE_ID | INVALID_ARGUMENT |
| INVALID_PLATFORM | INVALID_ARGUMENT |
| INVALID_PHONE | INVALID_ARGUMENT |
| INVALID_SMS_CODE | INVALID_ARGUMENT |
| INVALID_THIRD_PARTY_PLATFORM | INVALID_ARGUMENT |
| INVALID_THIRD_PARTY_IDENTITY | UNAUTHENTICATED |
| USER_DISABLED / USER_BANNED | PERMISSION_DENIED |
| USER_CONCURRENT_CONFLICT | ABORTED |

## 8. 安全与日志

| 敏感字段 | 处理方式 |
|---|---|
| phone | 归一化后 hash；日志不打印明文 |
| smsCode | 不打印 |
| idToken | SHA-256 后入库；日志不打印 |
| pushToken | hash 后入 push_token_hash；日志不打印 |
| deviceId | 日志使用 maskDeviceIdForLog |

## 9. 测试与验收

```bash
cd user-service
mvn clean test
mvn clean package -DskipTests
```

gRPC 冒烟（需服务已启动 + grpcurl）：

```bash
python user-service/scripts/run_user_09_1_auth_login_source_verify.py
```

新增测试类：

- `UserAuthResolveOrCreateDeviceUserTest`
- `UserAuthResolveOrCreatePhoneUserTest`
- `UserAuthResolveOrCreateThirdPartyUserTest`
- `UserAuthLoginSourceValidationTest`
- `UserAuthLoginSourceGrpcTest`

## 10. 数据库变更

本阶段 **未新增 Flyway migration**。现有 `user_auth_identities.identity_type VARCHAR(32)` 已支持 DEVICE / PHONE / GOOGLE / APPLE / FACEBOOK 常量扩展。
