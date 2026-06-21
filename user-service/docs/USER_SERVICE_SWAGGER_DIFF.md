# USER-09-0：Swagger 差异报告（源码复核版）

> 模块：`user-service`  
> 仓库：`jianjiange-site/yanshuqi-workspace`  
> Swagger：`Mobile Gateway API v1`  
> 本阶段目标：只做差异分析，不改业务代码，不改 proto，不改表结构。  
> 重要说明：本报告面向后续“只改 user-service”的适配工作，不设计 `mobile-gateway` 实现。

## 1. 总结结论

导师给的 Swagger 文档不是 `user-service` 的内部 gRPC 文档，而是 `Mobile Gateway API`：

```text
App 前端
→ mobile-gateway REST
→ user-service / match-service / post-service / payment-service / im-service gRPC
```

所以 `user-service` 后续不应该直接照着 Swagger 增加 REST Controller。正确改造目标是：

```text
补齐 user-service 内部用户域能力，
让未来 mobile-gateway 能按 Swagger 契约调用 user-service gRPC。
```

当前 user-service 已经完成注册、密码登录校验、资料维护、照片 object_key 绑定、批量资料查询、Redis 缓存、异常映射、日志脱敏等能力。但 Swagger 中的移动端契约比现有 user-service 多了三类关键能力：

1. **登录来源变化**：Swagger 是设备匿名登录、手机号验证码登录、三方登录；现有 user-service 主要是显式注册 + 密码登录校验。
2. **资料字段变化**：Swagger 的 profile 字段包含 `age / height / bio / occupation / education / location / avatar / pending / regulationStatus / lastOpenAtMs`；现有 user-service 的资料字段与返回模型不完全一致。
3. **头像上传变化**：Swagger 要求 `presign + confirm + AvatarVO(originalKey/minKey/midKey/width/height)`；现有 user-service 主要维护头像 / 相册 `object_key`，没有完整 presign、statObject、多规格头像模型。

## 2. 本次复核来源

本报告基于两类材料：

### 2.1 Swagger JSON

你上传的 Swagger 原始 JSON 可解析，关键信息如下：

| 项目 | 内容 |
|---|---|
| OpenAPI 版本 | `3.0.1` |
| 标题 | `Mobile Gateway API` |
| 描述 | `dating-server 移动端 BFF; REST→gRPC, 鉴权域并入网关 (JWT RS256 + refresh 轮换)` |
| paths 数量 | 39 |
| operation 数量 | 42 |
| schemas 数量 | 74 |
| `$ref` 引用 | 全部完整 |

### 2.2 user-service 源码结构

本次重点复核以下源码位置：

```text
proto/user/user_auth_service.proto
proto/user/user_profile_service.proto
user-service/src/main/java/.../service/impl/UserAuthServiceImpl.java
user-service/src/main/java/.../service/impl/UserProfileServiceImpl.java
user-service/src/main/java/.../service/impl/UserPhotoServiceImpl.java
user-service/src/main/java/.../service/impl/UserProfileQueryServiceImpl.java
user-service/src/main/resources/db/migration/
user-service/README.md
```

现有 proto 已暴露：

```text
UserAuthService:
- Register
- VerifyLogin

UserProfileService:
- GetSelfProfile
- UpdateProfile
- BindUserPhoto
- ListUserPhotos
- BatchGetBasicProfiles
- BatchGetRecommendProfiles
- CheckUserAvailable
```

现有 README / USER-08 收口说明中确认的核心表：

```text
users
user_auth_identities
user_profiles
user_photos
user_devices
user_settings
```

## 3. Swagger 与 user-service 职责边界差异

### 3.1 Swagger 覆盖范围

Swagger 包含 8 个主要分组：

| 分组 | 说明 | 是否属于 user-service |
|---|---|---:|
| Auth | 登录、刷新、登出 | 部分属于 |
| Profile | 用户资料 / onboarding | 属于 |
| Upload | 头像上传 BFF 代理 | 部分属于 |
| Home | 主页卡片聚合 | 部分属于 |
| Match | 划卡 / 匹配 / 配额 | 不属于 |
| Post | 动态 / 点赞 / 评论 / Feed | 不属于 |
| Payment | 支付 / 商品 / 余额 / 提现 | 不属于 |
| Health / callback / im-token | 健康检查、回调、IM token | 不属于或仅基础支撑 |

### 3.2 user-service 不应该做的内容

以下 Swagger 能力虽然出现在文档里，但不应该放进 user-service：

| Swagger 能力 | 原因 |
|---|---|
| REST Controller | Swagger 是 mobile-gateway 的 REST 契约 |
| `accessToken` JWT 签发 | 鉴权域在 gateway |
| `refreshToken` 轮换 | 网关会话能力 |
| logout 拉黑 access jti | 网关 token 黑名单能力 |
| `send-sms-code` 短信发送 | 可独立短信服务或 gateway 编排 |
| 真实三方 OAuth verify | 更适合 gateway 或 auth 适配层 |
| Match / Post / Payment / IM | 分别属于其他服务 |

## 4. Auth 差异分析

### 4.1 Swagger Auth 接口

| REST 接口 | 请求模型 | 返回模型 | user-service 关系 |
|---|---|---|---|
| `POST /api/v1/auth/login-device` | `LoginDeviceReq` | `LoginResultVO` | 需要支撑 |
| `POST /api/v1/auth/login-phone` | `LoginPhoneReq` | `LoginResultVO` | 需要支撑 |
| `POST /api/v1/auth/login-third-party` | `LoginThirdPartyReq` | `LoginResultVO` | 需要支撑 |
| `POST /api/v1/auth/send-sms-code` | `SendSmsCodeReq` | `SendSmsCodeVO` | 不建议 user-service 实现 |
| `POST /api/v1/auth/refresh` | `RefreshTokenReq` | `LoginResultVO` | 不属于 user-service |
| `POST /api/v1/auth/logout` | 无 | `ResultVoid` | 不属于 user-service |

### 4.2 现有 Auth 能力

现有 `UserAuthService` 主要是：

```text
Register
VerifyLogin
```

业务语义更接近：

```text
显式注册账号
→ 写 users / user_auth_identities / user_profiles / user_settings
→ 密码登录校验
→ 账号状态校验
→ user_devices upsert
→ 更新 last_login_at
```

这套能力对传统账号密码登录是完整的，但与 Swagger 中的移动端登录方式不完全匹配。

### 4.3 主要差异

| Swagger 要求 | 当前 user-service | 差异判断 |
|---|---|---|
| 设备匿名登录，首次自动注册 | 当前是显式 Register | 需要新增“解析或创建”能力 |
| 同一 `deviceId + platform` 复登命中既有账号 | 当前设备表用于登录记录 | 需要把设备作为登录身份来源之一 |
| 手机号 + 验证码登录 | 当前偏手机号/邮箱 + 密码 | 登录凭证类型不同 |
| 三方登录 Google / Apple / Facebook | 当前未覆盖 | 需要新增 identity_type |
| 返回 `newlyCreated` | 当前 VerifyLogin 不强调 | 需要补充返回语义 |
| 返回 `pending` | 可从 profile_status 推导 | 需要统一字段语义 |
| 返回 access/refresh token | 当前不做 | 继续不做，由 gateway 负责 |

### 4.4 user-service 适配方向

建议新增 gRPC 能力，避免直接沿用 `Login` 命名导致职责混乱：

```text
ResolveOrCreateDeviceUser
ResolveOrCreatePhoneUser
ResolveOrCreateThirdPartyUser
```

统一返回：

```text
user_id
newly_created
pending
account_status
profile_status
token_version
last_login_at
```

建议新增或扩展 identity 类型：

| identity_type | 来源 |
|---|---|
| `DEVICE` | 设备匿名登录 |
| `PHONE` | 手机号登录 |
| `GOOGLE` | Google |
| `APPLE` | Apple |
| `FACEBOOK` | Facebook |

注意：`smsCode`、`idToken` 是否在 user-service 校验，需要和导师确认。按 Swagger 描述，鉴权域并入 gateway，因此 user-service 更合理的定位是接收“已验证后的登录身份”，完成用户解析 / 创建。

## 5. Profile 差异分析

### 5.1 Swagger Profile 接口

| REST 接口 | 请求模型 | 返回模型 | 说明 |
|---|---|---|---|
| `POST /api/v1/profile/onboarding` | `UpsertOnboardingReq` | `ResultUserProfileVO` | 首次完善资料 |
| `PATCH /api/v1/profile` | `UpdateProfileReq` | `ResultBoolean` | 日常修改资料 |

### 5.2 Swagger Profile 字段

`UpsertOnboardingReq`：

| 字段 | 说明 |
|---|---|
| `nickname` | 昵称 |
| `gender` | 0=未指定，1=男，2=女 |
| `birthday` | 生日，兼容 `yyyy-MM-dd` 和 `yyyy/MM/dd` |
| `age` | 年龄，0 表示未填写 |
| `height` | 身高 cm |
| `bio` | 个人简介 |
| `occupation` | 职业 |
| `education` | 学历 |
| `location` | 城市 / 地区 |
| `defaultAvatarObjectKey` | 默认头像 object_key |

`UpdateProfileReq`：

| 字段 | 说明 |
|---|---|
| `nickname` | 昵称 |
| `age` | 年龄 |
| `height` | 身高 |
| `bio` | 简介 |
| `occupation` | 职业 |
| `education` | 学历 |
| `location` | 地区 |

`UserProfileVO`：

```text
userId
nickname
age
gender
height
bio
occupation
education
location
birthday
avatar
interests
pending
regulationStatus
lastOpenAtMs
```

### 5.3 现有 Profile 能力

现有 `UserProfileService` 已具备：

```text
GetSelfProfile
UpdateProfile
BindUserPhoto
ListUserPhotos
BatchGetBasicProfiles
BatchGetRecommendProfiles
CheckUserAvailable
```

现有 profile 设计偏用户基础资料维护和推荐 / 批量查询支撑，但和 Swagger 的 App 展示模型存在字段差异。

### 5.4 主要差异

| Swagger 字段 | 当前 user-service 状态 | 处理建议 |
|---|---|---|
| `age` | 可能由 birthday 推导，或未独立存储 | 建议不优先落库，优先由 birthday 推导；无 birthday 时兼容 age |
| `height` | 当前不确定已有字段 | 建议扩展 user_profiles |
| `bio` | 当前不确定已有字段 | 建议扩展 user_profiles |
| `occupation` | 当前不确定已有字段 | 建议扩展 user_profiles |
| `education` | 当前不确定已有字段 | 建议扩展 user_profiles |
| `location` | 当前可能为 region / area | 需要统一映射 |
| `avatar` | 当前多为 avatar_key / object_key | 需要映射成 AvatarVO |
| `pending` | 当前 profile_status / profile_completed | 从现有状态推导 |
| `regulationStatus` | 当前未确认 | 建议新增字段或默认值 |
| `lastOpenAtMs` | 当前未确认 | 可先由 last_login_at 近似或新增 last_open_at |

### 5.5 user-service 适配方向

建议新增：

```text
UpsertOnboarding
GetUserProfileView
```

并调整现有 `UpdateProfile` 的字段支持。

推荐模型：

```text
onboarding：负责首次关键资料补全，包括 gender / birthday / defaultAvatarObjectKey
updateProfile：负责日常资料编辑，不负责登录身份、不负责 token
profileView：负责输出贴近 Swagger UserProfileVO 的展示模型
```

## 6. Upload / Avatar 差异分析

### 6.1 Swagger Upload 接口

| REST 接口 | 请求模型 | 返回模型 | 说明 |
|---|---|---|---|
| `POST /api/v1/upload/presign` | `PresignAvatarReq` | `ResultPresignAvatarUploadVO` | 签发对象存储 PUT URL |
| `POST /api/v1/upload/confirm` | `ConfirmAvatarReq` | `ResultAvatarVO` | 确认上传并落头像 |

`PresignAvatarReq`：

```text
ext: jpg / jpeg / png / webp
expectedSizeBytes: <= 10MB
```

`PresignAvatarUploadVO`：

```text
presignedUrl
objectKey
expiresAtMs
```

`AvatarVO`：

```text
originalKey
minKey
midKey
width
height
```

### 6.2 现有头像能力

现有 `UserPhotoService` 已有：

```text
BindUserPhoto
ListUserPhotos
```

现有能力重点是“维护头像 / 相册 object_key 关系”，不是“签发上传 URL + 校验对象存储 + 生成多规格头像”。

### 6.3 主要差异

| Swagger 要求 | 当前 user-service | 差异判断 |
|---|---|---|
| presigned PUT URL | 当前未实现 | 需要新增 |
| confirm 时 statObject | 当前未实现 | 需要新增对象存储适配 |
| `custom_avatar` 语义 | 当前是 user_photos | 需要确认表语义是否复用 |
| `originalKey/minKey/midKey` | 当前多为单 object_key | 需要扩展 VO 或表 |
| `width/height` | 当前不确定 | 需要元数据来源或临时默认 |
| 图片大小 / 扩展名校验 | 当前不完整 | 需要新增 |

### 6.4 user-service 适配方向

建议新增独立 gRPC service：

```text
UserAvatarService
- PresignAvatarUpload
- ConfirmAvatarUpload
```

如果导师要求 proto 数量更少，也可以先放入 `UserProfileService`。

过渡实现建议：

```text
originalKey = objectKey
minKey = objectKey
midKey = objectKey
width = 0
height = 0
```

但必须在文档中说明：这是没有图片处理服务时的兼容方案，后续可接缩略图服务。

## 7. HomeCard 差异分析

### 7.1 Swagger Home 接口

```text
GET /api/v1/home/card?targetId=xxx
```

返回：

```text
HomeCardVO {
  selfUserId,
  target: UserProfileVO
}
```

### 7.2 当前 user-service 能力

现有有批量资料查询：

```text
BatchGetBasicProfiles
BatchGetRecommendProfiles
CheckUserAvailable
```

这些更适合服务间批量读，例如 match-service 拉推荐卡片时查用户基础资料。但 Swagger 的 `HomeCardVO` 更像 App 打开某个用户主页时的完整展示模型。

### 7.3 主要差异

| Swagger 要求 | 当前 user-service | 差异 |
|---|---|---|
| 单个 target 用户完整卡片 | 当前偏批量基础 / 推荐资料 | 需要新增 profile view 查询 |
| 包含 AvatarVO | 当前头像模型不一致 | 依赖 Avatar 适配 |
| 包含 pending/regulationStatus/lastOpenAtMs | 当前返回不一定覆盖 | 需要字段映射 |

### 7.4 user-service 适配方向

建议新增：

```text
GetUserProfileView
```

或者更贴近 Swagger：

```text
GetHomeCardProfile
```

返回目标用户的 `UserProfileVO` 等价结构即可。`selfUserId` 可以由 gateway 传入，user-service 只负责返回：

```text
self_user_id
target_profile
```

## 8. Proto 差异清单

### 8.1 建议新增 Auth RPC

```proto
rpc ResolveOrCreateDeviceUser(ResolveOrCreateDeviceUserRequest)
    returns (ResolveOrCreateLoginUserResponse);

rpc ResolveOrCreatePhoneUser(ResolveOrCreatePhoneUserRequest)
    returns (ResolveOrCreateLoginUserResponse);

rpc ResolveOrCreateThirdPartyUser(ResolveOrCreateThirdPartyUserRequest)
    returns (ResolveOrCreateLoginUserResponse);
```

### 8.2 建议新增 Profile RPC

```proto
rpc UpsertOnboarding(UpsertOnboardingRequest)
    returns (UserProfileViewResponse);

rpc GetUserProfileView(GetUserProfileViewRequest)
    returns (UserProfileViewResponse);
```

### 8.3 建议新增 Avatar RPC

```proto
rpc PresignAvatarUpload(PresignAvatarUploadRequest)
    returns (PresignAvatarUploadResponse);

rpc ConfirmAvatarUpload(ConfirmAvatarUploadRequest)
    returns (AvatarResponse);
```

### 8.4 不建议新增的 RPC

| RPC | 原因 |
|---|---|
| `IssueJwt` | gateway 职责 |
| `RefreshToken` | gateway 职责 |
| `Logout` | gateway 职责 |
| `SendSmsCode` | 不建议 user-service 直接承担短信通道 |
| `VerifyOAuthToken` | 不建议 user-service 直接接外部 OAuth |

## 9. 数据库差异清单

### 9.1 user_profiles 可能需要扩展

| 字段 | 建议 | 原因 |
|---|---|---|
| `height` | 新增 | Swagger 明确返回 |
| `bio` | 新增 | Swagger 明确返回 |
| `occupation` | 新增 | Swagger 明确返回 |
| `education` | 新增 | Swagger 明确返回 |
| `location` | 新增或映射 region | Swagger 字段名 |
| `regulation_status` | 新增或默认 | Swagger 返回 |
| `last_open_at` | 新增或用 last_login_at 近似 | Swagger 返回 |

`age` 建议谨慎落库：

| 方案 | 判断 |
|---|---|
| 由 `birthday` 推导 | 更合理，不会随时间变脏 |
| 单独落 `age` | 贴合 Swagger，但长期一致性差 |

推荐：

```text
优先 birthday；
兼容 age；
返回时有 birthday 则推导 age；
无 birthday 时使用用户填写 age。
```

### 9.2 user_auth_identities 需要扩展身份类型

| identity_type | 说明 |
|---|---|
| `DEVICE` | 匿名设备登录 |
| `PHONE` | 手机号登录 |
| `GOOGLE` | Google 登录 |
| `APPLE` | Apple 登录 |
| `FACEBOOK` | Facebook 登录 |

### 9.3 user_photos 需要评估扩展

| 字段 | 说明 |
|---|---|
| `original_key` | 原图 |
| `min_key` | 小图 |
| `mid_key` | 中图 |
| `width` | 原图宽度 |
| `height` | 原图高度 |
| `upload_status` | presign / uploaded / confirmed |

如果当前阶段控制风险，可以先不改表，用 `object_key` 兼容映射 AvatarVO。

## 10. 缓存差异清单

现有缓存：

```text
yanshuqi:user:basic:{user_id}
yanshuqi:user:profile:{user_id}
yanshuqi:user:status:{user_id}
```

Swagger 适配后建议新增或明确：

```text
yanshuqi:user:profile_view:{user_id}
```

可选：

```text
yanshuqi:user:home_card:{self_user_id}:{target_user_id}
```

写操作缓存失效：

| 写操作 | 删除缓存 |
|---|---|
| `ResolveOrCreateDeviceUser` 首次创建 | status/profile/basic |
| `ResolveOrCreatePhoneUser` 首次创建 | status/profile/basic |
| `ResolveOrCreateThirdPartyUser` 首次创建 | status/profile/basic |
| `UpsertOnboarding` | basic/profile/status/profile_view |
| `UpdateProfile` | basic/profile/profile_view |
| `ConfirmAvatarUpload` | basic/profile/profile_view |

## 11. 异常差异清单

建议新增错误码：

| 错误码 | gRPC Status | 场景 |
|---|---|---|
| `INVALID_DEVICE_ID` | `INVALID_ARGUMENT` | 设备 ID 为空或非法 |
| `INVALID_PLATFORM` | `INVALID_ARGUMENT` | platform 不在 1/2/3 |
| `INVALID_PHONE` | `INVALID_ARGUMENT` | 手机号非法 |
| `INVALID_SMS_CODE` | `INVALID_ARGUMENT` | 验证码非法 |
| `INVALID_THIRD_PARTY_PLATFORM` | `INVALID_ARGUMENT` | 三方平台非法 |
| `INVALID_THIRD_PARTY_IDENTITY` | `UNAUTHENTICATED` | 三方身份无效 |
| `INVALID_BIRTHDAY` | `INVALID_ARGUMENT` | 日期格式非法 |
| `INVALID_AVATAR_EXT` | `INVALID_ARGUMENT` | 图片扩展名非法 |
| `AVATAR_SIZE_EXCEEDED` | `INVALID_ARGUMENT` | 图片超过 10MB |
| `AVATAR_OBJECT_NOT_FOUND` | `NOT_FOUND` | confirm 时对象不存在 |
| `AVATAR_OBJECT_NOT_BELONG_TO_USER` | `PERMISSION_DENIED` | objectKey 不属于当前用户 |

## 12. 日志脱敏差异清单

新增敏感字段：

| 字段 | 处理方式 |
|---|---|
| `phone` | 脱敏 |
| `smsCode` | 禁止打印 |
| `idToken` | 禁止打印 |
| `googleEmail` | 脱敏 |
| `pushToken` | 禁止明文打印，可 hash |
| `presignedUrl` | 禁止打印完整 URL |

允许打印：

```text
user_id
identity_type
platform
device_id_hash
object_key
耗时
错误码
```

## 13. 后续阶段建议

只改 `user-service` 时，建议拆成以下阶段：

| 阶段 | 名称 | 目标 |
|---|---|---|
| USER-09-1 | Auth 登录来源适配 | 设备 / 手机号 / 三方 ResolveOrCreate |
| USER-09-2 | Profile / Onboarding 适配 | 补齐 Swagger profile 字段与展示模型 |
| USER-09-3 | Avatar / Upload 适配 | presign / confirm / AvatarVO |
| USER-09-4 | ProfileView / HomeCard 支撑 | 支撑 `/home/card` 的用户卡片数据 |
| USER-09-5 | 缓存、异常、日志统一 | 补错误码、缓存 key、脱敏 |
| USER-09-6 | 测试与文档验收 | 回归测试、差异文档、验收脚本 |

## 14. 下一阶段 USER-09-1 建议边界

下一步建议只做 Auth 登录来源适配，不要同时做 Profile / Upload。

允许：

```text
新增 Auth proto RPC
扩展 identity_type
新增 ResolveOrCreateDeviceUser
新增 ResolveOrCreatePhoneUser
新增 ResolveOrCreateThirdPartyUser
补 user_devices upsert
补 newly_created / pending 返回
补单元测试 / 集成测试
补文档
```

禁止：

```text
不实现 mobile-gateway
不暴露 REST Controller
不签发 JWT
不实现 refresh token 轮换
不实现 logout jti 黑名单
不实现真实短信通道
不实现真实 OAuth verify
不做头像 presign
不做 profile 字段扩表
不改 match/post/payment/im 服务
```

## 15. 最终判断

当前 user-service 已经是一个相对完整的用户域内部服务，但它与导师 Swagger 的核心差异在于：

```text
现有 user-service 偏“内部用户域 gRPC 能力”；
导师 Swagger 偏“移动端 BFF REST 契约”。
```

所以 USER-09 的正确方向不是把 user-service 改成 REST 服务，而是补齐未来 gateway 调用所需的内部能力：

1. 登录来源解析与自动建档。
2. onboarding 与 App 展示资料字段。
3. AvatarVO 与上传确认。
4. 用户卡片展示查询。
5. 与 Swagger 对齐的错误码、缓存、日志脱敏。

本阶段差异报告完成后，可以进入：

```text
USER-09-1：Auth 登录来源适配  ← 已完成
USER-09-2：Profile / Onboarding 适配  ← 已完成，见 docs/USER_SERVICE_SWAGGER_PROFILE_ADAPTATION.md
USER-09-3：Avatar / Upload 适配
```

