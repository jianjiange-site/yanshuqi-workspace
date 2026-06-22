# PostService Redis 与 Feed 设计

统一前缀：`yanshuqi:`，TTL 7 天（`PostRedisKeys.TTL_SECONDS`）。

## Key 全览

| 分类 | Key 模式 | 类型 | 作用 |
|------|----------|------|------|
| 详情缓存 | `yanshuqi:post:detail:{postId}` | String(JSON) | 帖子详情 fail-open 缓存 |
| 点赞增量 | `yanshuqi:post:stat:incr:{postId}:likes` | String 计数 | 未刷盘点赞 delta |
| 评论增量 | `yanshuqi:post:stat:incr:{postId}:comments` | String 计数 | 未刷盘评论 delta |
| 待刷盘 | `yanshuqi:post:updated_set` | Set | 有待刷盘增量的 postId |
| 评论窗口 | `yanshuqi:post:comments:{postId}` | ZSet | 最新 200 条 commentId |
| 热门池 | `yanshuqi:feed:pool:recommend:{male\|female}` | ZSet | 热度分排序 |
| 热门 tmp | `yanshuqi:feed:pool:recommend:{male\|female}:tmp` | ZSet | 重建中间态 |
| 冷启动 | `yanshuqi:feed:cold_start:pool:{male\|female}` | ZSet | 新帖，score=createdAt |
| 时间线 | `yanshuqi:user:timeline:{userId}` | ZSet | 好友帖，最多 100 条 |
| 已读 | `yanshuqi:user:read:posts:{userId}` | Set | Feed 已展示 postId |

常量上限见 `PostRedisKeys`：`COMMENT_WINDOW_SIZE=200`，`RECOMMEND_POOL_SIZE=3000`，`COLD_START_POOL_SIZE=10000`，`TIMELINE_SIZE=100`。

---

## 详情缓存

- **写**：发帖成功、cache miss 读详情后
- **删**：点赞/评论/删帖后 evict
- **策略**：Redis 失败 warn，回源 DB

---

## 计数 delta 模型

```text
用户可见 like_count = post_stats.like_count + Redis likes_delta
用户可见 comment_count = post_stats.comment_count + Redis comments_delta
```

- 写：仅状态真实变化时 INCR delta，并 SADD updated_set
- 刷盘：Lua 原子 GET+SET0，UPDATE post_stats，delta 归零后移出 set

---

## 评论 ZSet 窗口

- score = commentId（倒序即最新在前）
- 超出 200 条 removeRange 最旧
- ListComments 优先读窗口，不足回源 DB

---

## Feed 三路来源

| 来源 | 写入时机 | 读取用途 |
|------|----------|----------|
| recommend | FeedScoreJob 每 5 分钟 | 默认槽位 |
| cold_start | 发帖后按作者性别 | 第 6 位优先 |
| timeline | 发帖后异步写扩散到好友 | 第 3 位优先 |

### 性别分桶

- caller male → 读 female 池（异性推荐）
- 作者性别：`FallbackUserProfileClient` 用 `userId % 2`（待 user-service 替换）

### 混排规则（1-based 槽位）

| 槽位 | 优先级 |
|------|--------|
| 3 | timeline → recommend → cold_start |
| 6 | cold_start → recommend → timeline |
| 其他 | recommend → timeline → cold_start |

某路为空自动 fallback；同页 postId 去重。

### Cursor

格式：`recOffset:csOffset`，空或 `0:0` 表示首页。

### 热度公式

```text
score = (10 + like*1 + comment*3) / pow(hoursSinceCreated + 2, 1.5)
```

计数用 DB + Redis 实时值；近 3 天帖子；tmp 池写完后 **rename** 原子替换正式池。

---

## 已读去重

- **当前**：Redis Set `yanshuqi:user:read:posts:{userId}`
- **流程**：Feed 返回前 filterUnread；成功后 markRead
- **为何不用 BloomFilter**：阶段 4 避免引入 Redisson 依赖，Set 实现简单、零误判
- **后续替换**：可换 Redisson `RBloomFilter`，接口收敛在 `ReadHistoryService`，key 前缀不变

---

## Redis 失败降级

| 场景 | 行为 |
|------|------|
| 写 delta 失败 | warn，DB 仍正确，计数可能短暂滞后 |
| 读 delta 失败 | 当 0，显示 DB 基准值 |
| Feed 池读失败 | 返回空列表，依赖其他路或空 Feed |
| 热门池重建失败 | warn，不影响发帖/互动 |

DB 始终是主存储，Redis 不可反过来当事实源。
