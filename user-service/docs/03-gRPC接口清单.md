# user-service gRPC 接口清单

> **阅读顺序**：3 / 9

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
| **入参** | `user_id`, `nickname`, `gender`, `birth_date`, `country_code`, `city_code`, `language_codes`, `bio`, `interests`（无 `avatar_key`） |
| **出参** | `UserProfileDetail` |
| **读库** | `users`, `user_profiles` |
| **写库** | 更新 `user_profiles`, `users.profile_status` |
| **读 Redis** | 否 |
| **写 Redis** | 否 |
| **删 Redis** | 是（`basic` / `profile` / `status` 三 key） |
| **主要错误码** | `PROFILE_UPDATE_INVALID`, `USER_NOT_FOUND`, `USER_BANNED`, `USER_DISABLED` |
| **不负责** | 头像绑定、图片上传 |
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
| **删 Redis** | 是（三 key） |
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

---

## 接口总数

| 服务 | RPC 数量 |
|---|---|
| UserAuthService | 2 |
| UserProfileService | 7 |
| **合计** | **9** |
