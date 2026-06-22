# PostService 接口与 gRPC 契约

契约文件：`proto/post/post_service.proto`  
Java 包：`com.dating.post.grpc.proto`  
服务名：`com.dating.post.v1.PostService`

## 身份传递约定

- **callerUserId 不在 request body**，统一由 Metadata / Header `x-user-id` 传入。
- gRPC 拦截器：`GrpcUserContextInterceptor` → `GrpcUserContext.requireUserId()` / `getUserId()`。

---

## 9 个 RPC

| RPC | 需 x-user-id | 用途 | 关键入参 | 关键出参 |
|-----|:------------:|------|----------|----------|
| CreatePost | ✅ 必填 | 发帖 | content, image_keys | post_id |
| GetPostDetail | 可选 | 详情 | post_id | PostInfo（含 is_liked） |
| ListUserPosts | 可选 | 用户帖列表 | user_id, cursor, page_size | items, next_cursor, has_more |
| ActionLike | ✅ 必填 | 点赞/取消 | post_id, like | success |
| CreateComment | ✅ 必填 | 发评论 | post_id, content | comment_id |
| ListComments | ❌ | 评论列表 | post_id, cursor, page_size | items, next_cursor, has_more |
| DeleteComment | ✅ 必填 | 删评论 | comment_id | success |
| DeletePost | ✅ 必填 | 删帖 | post_id | success |
| GetRecommendFeed | ✅ 必填 | 推荐 Feed | cursor, page_size | items, next_cursor, has_more |

### PostInfo 字段

`post_id`, `user_id`, `content`, `image_keys[]`, `like_count`, `comment_count`, `is_liked`, `created_at_seconds`

### 统一响应壳

各 Response 含 `code`（0=成功）与 `message`（"OK"）。

---

## Debug REST（dev/test）

Base：`http://localhost:8084/internal/debug/post`

| 方法 | 路径 | x-user-id | 说明 |
|------|------|:---------:|------|
| POST | `/` | ✅ | 发帖 |
| GET | `/{postId}` | 可选 | 详情 |
| GET | `/user/{userId}` | 可选 | 用户列表 |
| DELETE | `/{postId}` | ✅ | 删帖 |
| POST | `/{postId}/like` | ✅ | 点赞 |
| DELETE | `/{postId}/like` | ✅ | 取消赞 |
| POST | `/{postId}/comment` | ✅ | 发评论 |
| GET | `/{postId}/comment` | — | 评论列表 |
| DELETE | `/comment/{commentId}` | ✅ | 删评论 |
| GET | `/feed` | ✅ | 推荐 Feed |
| POST | `/feed/rebuild` | — | 手动重建热门池 |
| GET | `/mock-feed` | — | 历史 mock 入口（已废弃提示） |

---

## 错误码

| 枚举 | code | 典型场景 |
|------|------|----------|
| INVALID_ARGUMENT | INVALID_ARGUMENT | postId 非法、content 空、cursor 格式错 |
| UNAUTHORIZED | UNAUTHORIZED | 缺少 x-user-id |
| FORBIDDEN | FORBIDDEN | 非作者删帖/删评论 |
| POST_NOT_FOUND | POST_NOT_FOUND | 帖不存在或已删 |
| COMMENT_NOT_FOUND | COMMENT_NOT_FOUND | 评论不存在或已删 |
| INTERNAL_ERROR | INTERNAL_ERROR | 发帖后回读失败等 |

gRPC 状态映射见 `PostGrpcStatusMapper`。

---

## grpcurl 示例

```bash
# 发帖
grpcurl -plaintext -H "x-user-id: 10001" \
  -d '{"content":"hello","image_keys":["post/1/a.jpg"]}' \
  localhost:9094 com.dating.post.v1.PostService/CreatePost

# Feed
grpcurl -plaintext -H "x-user-id: 10001" \
  -d '{"page_size":10,"cursor":"0:0"}' \
  localhost:9094 com.dating.post.v1.PostService/GetRecommendFeed
```

---

## 与 gateway 的关系（当前未实现）

预期 gateway 将：

1. 校验 JWT，解析 callerUserId
2. 注入 gRPC Metadata `x-user-id`
3. 暴露 App REST `/api/v1/post/**`

当前验收不经过 gateway，直接使用 Debug REST 或 grpcurl。
