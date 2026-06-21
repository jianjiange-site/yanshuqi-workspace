# user-service gRPC 接口清单

> **阅读顺序**：3 / 9  
> **最后更新**：USER-09-6（17 个 RPC）

本文档描述 user-service 当前已实现的全部 gRPC 接口。proto 定义见 `proto/user/`。

---

## UserAuthService

### Register

| 项 | 说明 |
|---|---|
| **用途** | BH 真人用户注册，初始化四表基础数据 |
| **入参** | `identity_type`, `identity_value`, `password`, `user_type`, `register_source`, `device_info`（可选） |
| **出参** | `user_id`, `account_status`, `profile_status`, `token_version` |
| **读库** | 查 `user_auth_identities`（幂等） |
| **写库** | 写 `users`, `user_auth_identities`, `user_profiles`, `user_settings` |
| **读 Redis** | 否 |
| **写 Redis** | 否 |
| **删 Redis** | 否 |
| **主要错误码** | `USER_REQUEST_INVALID`, `IDENTITY_ALREADY_EXISTS`, `USER_CONCURRENT_CONFLICT`, `PASSWORD_INVALID` |
| **不负责** | JWT 签发、短信验证码、OAuth、写 `user_devices` |
| **典型调用方** | mobile-gateway（注册流程） |

### VerifyLogin

| 项 | 说明 |
|---|---|
| **用途** | 校验凭证与密码，更新登录时间与设备 |
| **入参** | `identity_type`, `identity_value`, `password`, `device_info` |
| **出参** | `user_id`, `account_status`, `profile_status`, `token_version`, `last_login_at` |
| **读库** | `user_auth_identities`, `users`, `user_devices` |
| **写库** | upsert `user_devices`；更新 `users.last_login_at`, `user_auth_identities.last_login_at` |
| **读 Redis** | 否 |
| **写 Redis** | 否 |
| **删 Redis** | 否 |
| **主要错误码** | `IDENTITY_NOT_FOUND`, `PASSWORD_INVALID`, `USER_NOT_FOUND`, `USER_DISABLED`, `USER_BANNED`, `USER_DELETED`, `USER_CONCURRENT_CONFLICT` |
| **不负责** | JWT 签发、登录态 Redis |
| **典型调用方** | mobile-gateway（登录流程） |

### ResolveOrCreateDeviceUser

| 项 | 说明 |
|---|---|
| **用途** | 设备匿名登录：按 deviceId+platform 解析或首次创建用户（Swagger login-device） |
| **入参** | `device_id`, `platform`, `app_version`, `push_token`（可选） |
| **出参** | `user_id`, `newly_created`, `pending`, `account_status`, `profile_status`, `token_version`, `last_login_at` |
| **读库** | `user_auth_identities`, `users`, `user_devices`, `user_profiles` |
| **写库** | 首次：四表创建；复登：upsert `user_devices`，更新 `last_login_at` |
| **读 Redis** | 否 |
| **写 Redis** | 否 |
| **删 Redis** | 是（**仅 newly_created** 时删 basic/profile/status/profile_view） |
| **主要错误码** | `INVALID_DEVICE_ID`, `INVALID_PLATFORM`, `USER_CONCURRENT_CONFLICT`, `USER_BANNED`, `USER_NOT_FOUND` |
| **不负责** | JWT、accessToken/refreshToken |
| **典型调用方** | mobile-gateway |

### ResolveOrCreatePhoneUser

| 项 | 说明 |
|---|---|
| **用途** | 手机号 + 验证码登录解析/创建（Swagger login-phone；验证码 mock） |
| **入参** | `phone`, `sms_code`, `device_id`, `platform`, … |
| **出参** | 同 ResolveOrCreateDeviceUser |
| **读库** | 同设备登录（identity_type=PHONE） |
| **写库** | 同设备登录 |
| **读/写/删 Redis** | 同设备登录 |
| **主要错误码** | `INVALID_PHONE`, `INVALID_SMS_CODE`, `USER_CONCURRENT_CONFLICT`, … |
| **不负责** | 真实短信发送、JWT |
| **典型调用方** | mobile-gateway |

### ResolveOrCreateThirdPartyUser

| 项 | 说明 |
|---|---|
| **用途** | 三方登录解析/创建（Swagger login-third-party；idToken mock hash） |
| **入参** | `third_party_platform`, `id_token`, `device_id`, `platform`, … |
| **出参** | 同 ResolveOrCreateDeviceUser |
| **读库** | identity_type=GOOGLE/APPLE/FACEBOOK 等 |
| **写库** | 同设备登录 |
| **读/写/删 Redis** | 同设备登录 |
| **主要错误码** | `INVALID_THIRD_PARTY_PLATFORM`, `INVALID_THIRD_PARTY_IDENTITY`, … |
| **不负责** | 真实 OAuth verify、JWT |
| **典型调用方** | mobile-gateway |

---

## UserProfileService

### GetSelfProfile

| 项 | 说明 |
|---|---|
| **用途** | 查询本人完整资料（含 PENDING 头像 key） |
| **入参** | `user_id` |
| **出参** | `UserProfileDetail` |
| **读库** | `users`, `user_profiles` |
| **写库** | 否 |
| **读 Redis** | 否 |
| **写 Redis** | 否 |
| **删 Redis** | 否 |
| **主要错误码** | `USER_REQUEST_INVALID`, `USER_NOT_FOUND`, `PROFILE_NOT_FOUND` |
| **不负责** | 批量查询、外显头像审核过滤 |
| **典型调用方** | mobile-gateway（个人中心） |

### UpdateProfile

| 项 | 说明 |
|---|---|
| **用途** | 更新昵称、性别、地区等基础资料，计算 profile_score / profile_status |
| **入参** | `user_id`, `nickname`, `gender`, `birthday`, `height`, `bio`, `occupation`, `education`, `location`, … |
| **出参** | `UserProfileDetail` 或 success 语义 |
| **读库** | `users`, `user_profiles` |
| **写库** | 更新 `user_profiles`, `users.profile_status` |
| **读 Redis** | 否 |
| **写 Redis** | 否 |
| **删 Redis** | 是（basic / profile / status / profile_view 四类） |
| **主要错误码** | `INVALID_NICKNAME`, `PROFILE_UPDATE_INVALID`, `PROFILE_UPDATE_FAILED`, `USER_BANNED` |
| **不负责** | 首次 onboarding 全量校验（用 UpsertOnboarding） |
| **典型调用方** | mobile-gateway（PATCH /profile） |

### UpsertOnboarding

| 项 | 说明 |
|---|---|
| **用途** | 首次登录后 onboarding 资料补齐（Swagger POST /profile/onboarding） |
| **入参** | `user_id`, `nickname`, `gender`, `birthday`, `height`, `bio`, `occupation`, `education`, `location` |
| **出参** | `UserProfileView`（含 pending、profile_score） |
| **读库** | `users`, `user_profiles` |
| **写库** | 更新 profiles + users.profile_status |
| **删 Redis** | 是（四类 key） |
| **主要错误码** | `INVALID_GENDER`, `INVALID_BIRTHDAY`, `INVALID_AGE`, `INVALID_HEIGHT`, `INVALID_BIO`, `PROFILE_NOT_FOUND` |
| **不负责** | JWT、gateway REST |
| **典型调用方** | mobile-gateway |

### GetUserProfileView

| 项 | 说明 |
|---|---|
| **用途** | 查询 Swagger 形态资料视图（含 AvatarVO、regulationStatus 等） |
| **入参** | `user_id` |
| **出参** | `UserProfileView` |
| **读库** | `users`, `user_profiles`（当前直查 DB，不读 profile_view 缓存） |
| **写库** | 否 |
| **删 Redis** | 否 |
| **主要错误码** | `USER_NOT_FOUND`, `PROFILE_NOT_FOUND` |
| **不负责** | HomeCard 聚合、match 判断 |
| **典型调用方** | mobile-gateway |

### BindUserPhoto

| 项 | 说明 |
|---|---|
| **用途** | 绑定头像或相册 MinIO object key（不上传文件） |
| **入参** | `user_id`, `photo_type`（AVATAR/ALBUM）, `object_key`, `sort_order` |
| **出参** | `UserPhoto`, `avatar_key`, `profile_status` |
| **读库** | `users`, `user_profiles`, `user_photos` |
| **写库** | 写/更新 `user_photos`；头像时更新 `user_profiles.avatar_key`, `users.profile_status` |
| **读 Redis** | 否 |
| **写 Redis** | 否 |
| **删 Redis** | 是（四类 key） |
| **主要错误码** | `PHOTO_OBJECT_KEY_INVALID`, `PHOTO_TYPE_INVALID`, `PHOTO_LIMIT_EXCEEDED`, `USER_BANNED` |
| **不负责** | MinIO 上传、presigned URL、审核通过 |
| **典型调用方** | mobile-gateway（客户端直传 MinIO 后回调绑定） |

### ListUserPhotos

| 项 | 说明 |
|---|---|
| **用途** | 查询本人照片列表（含审核状态） |
| **入参** | `user_id`, `photo_type`（可选） |
| **出参** | `repeated UserPhoto` |
| **读库** | `user_photos` |
| **写库** | 否 |
| **读 Redis** | 否 |
| **写 Redis** | 否 |
| **删 Redis** | 否 |
| **主要错误码** | `USER_REQUEST_INVALID`, `USER_NOT_FOUND` |
| **不负责** | 外显批量查询的头像过滤 |
| **典型调用方** | mobile-gateway |

### BatchGetBasicProfiles

| 项 | 说明 |
|---|---|
| **用途** | 批量查询用户基础展示资料（列表/卡片） |
| **入参** | `user_ids`（1～100）, `include_unavailable` |
| **出参** | `repeated BasicUserProfile` |
| **读库** | 未命中时读 `users`, `user_profiles`, `user_photos`（APPROVED 头像） |
| **写库** | 否 |
| **读 Redis** | 是（`basic` key） |
| **写 Redis** | 是（未命中回写，TTL 600s） |
| **删 Redis** | 否 |
| **主要错误码** | `USER_BATCH_SIZE_EXCEEDED`, `USER_REQUEST_INVALID` |
| **不负责** | 推荐排序、完整 URL、PENDING 头像外显 |
| **典型调用方** | match-service, im-service, post-service |

### BatchGetRecommendProfiles

| 项 | 说明 |
|---|---|
| **用途** | 批量查询推荐展示所需资料快照（不做推荐算法） |
| **入参** | 同 BatchGetBasicProfiles |
| **出参** | `repeated RecommendUserProfile` |
| **读库** | 未命中时读三表（同 USER-06） |
| **写库** | 否 |
| **读 Redis** | 是（`profile` key） |
| **写 Redis** | 是（TTL 600s） |
| **删 Redis** | 否 |
| **主要错误码** | 同 BatchGetBasicProfiles |
| **不负责** | 匹配分计算、地理距离、AI 画像 |
| **典型调用方** | match-service |

### CheckUserAvailable

| 项 | 说明 |
|---|---|
| **用途** | 批量检查用户是否可被业务使用 |
| **入参** | `user_ids`（1～100） |
| **出参** | `repeated UserAvailableResult` |
| **读库** | 未命中时读 `users` |
| **写库** | 否 |
| **读 Redis** | 是（`status` key） |
| **写 Redis** | 是（TTL 600s） |
| **删 Redis** | 否 |
| **主要错误码** | `USER_BATCH_SIZE_EXCEEDED` |
| **不负责** | 封禁/解封操作本身 |
| **典型调用方** | match-service, im-service |

### GetHomeCardProfile

| 项 | 说明 |
|---|---|
| **用途** | 主页卡片 target 用户资料（Swagger GET /home/card） |
| **入参** | `self_user_id`, `target_user_id` |
| **出参** | `target_profile`（UserProfileView 形态） |
| **读库** | `users`, `user_profiles`, `user_photos`（APPROVED 头像） |
| **写库** | 否 |
| **读/写 Redis** | 否（不引入 self-target 组合缓存） |
| **主要错误码** | `INVALID_USER_ID`, `INVALID_TARGET_USER_ID`, `TARGET_USER_NOT_FOUND`, `TARGET_USER_UNAVAILABLE`, `HOME_CARD_QUERY_FAILED` |
| **不负责** | match 关系、visit 记录、post/im 聚合 |
| **典型调用方** | mobile-gateway / match 编排 |

---

## UserAvatarService

### PresignAvatarUpload

| 项 | 说明 |
|---|---|
| **用途** | 生成头像上传 objectKey 与 presigned PUT URL（Swagger upload/presign） |
| **入参** | `user_id`, `ext`, `expected_size_bytes` |
| **出参** | `object_key`, `presigned_url`, `expire_at` |
| **读库** | 校验用户存在与状态 |
| **写库** | **否**（presign 不落库） |
| **读/写/删 Redis** | 否 |
| **主要错误码** | `INVALID_AVATAR_EXT`, `AVATAR_SIZE_EXCEEDED`, `AVATAR_PRESIGN_FAILED` |
| **不负责** | 图片二进制中转、gateway REST |
| **典型调用方** | mobile-gateway |

### ConfirmAvatarUpload

| 项 | 说明 |
|---|---|
| **用途** | statObject 校验后确认头像落库（Swagger upload/confirm） |
| **入参** | `user_id`, `object_key` |
| **出参** | `AvatarVO`（originalKey 等） |
| **读库** | `users`, `user_profiles`, `user_photos` |
| **写库** | 写/更新 `user_photos`，更新 `user_profiles.avatar_key` |
| **删 Redis** | 是（四类 key） |
| **主要错误码** | `INVALID_AVATAR_OBJECT_KEY`, `AVATAR_OBJECT_NOT_BELONG_TO_USER`, `AVATAR_OBJECT_NOT_FOUND`, `AVATAR_OBJECT_STAT_FAILED`, `AVATAR_CONFIRM_FAILED` |
| **不负责** | 裁剪 min/mid 多规格（当前临时映射） |
| **典型调用方** | mobile-gateway |

---

## 接口总数

| 服务 | RPC 数量 |
|---|---|
| UserAuthService | 5 |
| UserProfileService | 10 |
| UserAvatarService | 2 |
| **合计** | **17** |
