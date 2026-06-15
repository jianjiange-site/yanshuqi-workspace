# MinIO Bucket Setup (yanshuqi)

## Bucket

| 项 | 值 |
|---|---|
| Bucket 名称 | `dating-yanshuqi` |
| 所属学员 | `yanshuqi` |
| Path Style Access | `true` |

## 创建步骤

1. 登录 MinIO 控制台（endpoint 见 `deploy/.env.example` 中 `MINIO_ENDPOINT`）。
2. 使用管理员或授权账号创建 bucket：`dating-yanshuqi`。
3. 确认 bucket 为 **私有**，禁止写入公共 bucket。
4. 在本地复制 `deploy/.env.example` 为 `.env`，填入占位符以外的真实 AK/SK（**不要提交到 Git**）。

## Object Key 规范（后续业务阶段使用）

```text
<category>/<owner_id>/<yyyymm>/<uuid>.<ext>
```

示例：

```text
avatar/yanshuqi_100001/202606/uuid.jpg
post-image/post_100001/202606/uuid.jpg
```

## Stage 00-B 限制

- 仅做 bucket 存在性检查（`HeadBucket` / `bucketExists`）。
- 禁止上传头像、帖子图片、聊天附件等业务文件。
- 禁止签发 presigned URL。

## 健康检查

各服务 `/health/infra` 中 `minio` 项会检查 `dating-yanshuqi` 是否存在。
