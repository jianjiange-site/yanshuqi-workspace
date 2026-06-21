# USER-09-2：Profile / Onboarding 适配

> 模块：`user-service`  
> 范围：仅 gRPC 内部 Profile / Onboarding 能力，不含 gateway、Upload、HomeCard

## 1. 本阶段实现了什么

| gRPC RPC | 对应 Swagger REST |
|---|---|
| `UpsertOnboarding` | `POST /api/v1/profile/onboarding` |
| `GetUserProfileView` | UserProfileVO 查询支撑 |
| `UpdateProfile`（扩展） | `PATCH /api/v1/profile` |

保留：`GetSelfProfile`、`UpdateProfile` 原有能力。

## 2. UpsertOnboarding vs UpdateProfile

| 项 | UpsertOnboarding | UpdateProfile |
|---|---|---|
| 场景 | 首次登录后 onboarding | 日常资料修改 |
| 可写 gender / birthday | 是 | 否（未传则保留） |
| defaultAvatarObjectKey | 是 | 否 |
| 返回 | UserProfileView | UserProfileDetail + success |

## 3. Swagger 字段映射

| Swagger 字段 | DB 字段 | 说明 |
|---|---|---|
| nickname | user_profiles.nickname | 直接映射 |
| gender | user_profiles.gender | MALE/FEMALE/OTHER/UNKNOWN |
| birthday | user_profiles.birth_date | yyyy-MM-dd / yyyy/MM/dd |
| age | user_profiles.age | 无 birthday 时落库；有 birthday 时返回推导值 |
| height | user_profiles.height | 厘米 |
| bio | user_profiles.bio | 已有字段 |
| occupation | user_profiles.occupation | 新增 |
| education | user_profiles.education | 新增 |
| location | user_profiles.location | 展示文本，与 city_code 不同 |
| avatar | user_profiles.avatar_key | 临时 AvatarVO 三档同 key |
| interests | user_profiles.interests | JSON 数组 |
| pending | 计算字段 | profile_completed=1 或 COMPLETED → false |
| regulationStatus | user_profiles.regulation_status | 默认 0 |
| lastOpenAtMs | last_open_at / users.last_login_at | 优先 last_open_at |

## 4. pending 计算

```text
profile_status == COMPLETED → pending = false
profile_completed == 1 → pending = false
否则 pending = true
```

## 5. age / birthday 规则

1. 有 `birth_date` 时，返回 age 由 birthday 推导。
2. 无 birthday 但有落库 age 时，返回 age。
3. Onboarding 同时传 birthday 与 age 时，以 birthday 为准并清空落库 age。

## 6. avatar 临时映射

本阶段不做 Upload。若存在 `avatar_key`：

```text
originalKey = minKey = midKey = avatar_key
width = height = 0
```

USER-09-3 将完善多规格 AvatarVO。

## 7. 为什么不做 Upload / Presign

本阶段不做 Upload / Presign；完整上传链路见 **USER-09-3**（`USER_SERVICE_SWAGGER_UPLOAD_ADAPTATION.md`）。

## 8. 缓存失效

更新资料后删除：

- `yanshuqi:user:profile:{user_id}`
- `yanshuqi:user:basic:{user_id}`
- `yanshuqi:user:status:{user_id}`
- `yanshuqi:user:profile_view:{user_id}`

Redis 失败仅打日志，不回滚事务。

## 9. 测试与验收

```bash
cd user-service
mvn clean test
mvn clean package -DskipTests
python scripts/run_user_09_2_profile_onboarding_verify.py
```
