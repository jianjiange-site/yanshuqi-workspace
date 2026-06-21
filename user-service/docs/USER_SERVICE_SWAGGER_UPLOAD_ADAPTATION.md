# USER-09-3：Avatar / Upload 适配

> 模块：`user-service`  
> 范围：gRPC presign/confirm + ObjectStorageService 抽象，不含 gateway REST、图片裁剪

## 1. 本阶段实现了什么

| gRPC RPC | 对应 Swagger REST |
|---|---|
| `PresignAvatarUpload` | `POST /api/v1/upload/presign` |
| `ConfirmAvatarUpload` | `POST /api/v1/upload/confirm` |

独立 gRPC service：`UserAvatarService`（定义于 `user_profile_service.proto`，与 Profile RPC 同文件但 service 分离，Upload 边界清晰）。

## 2. Presign vs Confirm

| 项 | PresignAvatarUpload | ConfirmAvatarUpload |
|---|---|---|
| 写库 | 否 | 是（user_photos + avatar_key） |
| statObject | 否 | 是 |
| 删缓存 | 否 | 是 |
| 返回 | presignedUrl / objectKey / expiresAtMs | AvatarVO |

**为什么 presign 不落库**：客户端尚未上传，objectKey 仅预生成；confirm 时 statObject 验证后才绑定。

## 3. objectKey 规则

```text
avatar/{user_id}/{yyyyMM}/{uuid}.{ext}
```

- ext 白名单：jpg/jpeg/png/webp（小写）
- 服务端生成，客户端不可自定义完整 key
- confirm 校验 `avatar/{user_id}/` 前缀，拒绝 `..` 路径穿越

## 4. ext / size 校验

- `expected_size_bytes` > 0 且 ≤ 10MB
- confirm 时 statObject.size 同样 ≤ 10MB
- contentType 限制 image/jpeg、image/png、image/webp

## 5. ObjectStorageService 抽象

| 方法 | 作用 |
|---|---|
| `presignPutObject` | 签发 PUT URL |
| `statObject` | 确认对象存在 |

| 实现 | 模式 | 说明 |
|---|---|---|
| `MockObjectStorageService` | `object.storage.mode=mock`（默认） | presign 预注册对象，dev/test 可重复验证 |
| `MinioObjectStorageService` | `object.storage.mode=minio` | 生产 MinIO SDK |

业务 Service 只依赖接口，不直接耦合 SDK。

## 6. AvatarVO 多规格（过渡）

本阶段**无图片裁剪/缩略图服务**：

```text
originalKey = minKey = midKey = object_key
width/height = statObject 值，缺失时为 0
```

GetUserProfileView 读 avatar_key 时 width/height 仍为 0（未持久化尺寸）。

## 7. 数据库

**方案 A**：复用 `user_photos.object_key`，不新增多规格列。

## 8. 缓存失效（仅 confirm）

- `yanshuqi:user:basic:{user_id}`
- `yanshuqi:user:profile:{user_id}`
- `yanshuqi:user:status:{user_id}`
- `yanshuqi:user:profile_view:{user_id}`

Redis 失败不回滚事务。

## 9. 配置项

```yaml
avatar.upload.allowed-ext=jpg,jpeg,png,webp
avatar.upload.max-size-bytes=10485760
avatar.upload.presign-expire-seconds=600
avatar.upload.object-key-prefix=avatar
object.storage.mode=mock
object.storage.bucket=dating-yanshuqi
object.storage.public-base-url=
```

## 10. 测试与验收

```bash
cd user-service
mvn clean test
mvn clean package -DskipTests
python scripts/run_user_09_3_avatar_upload_verify.py
```
