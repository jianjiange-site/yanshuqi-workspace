# post-service

**Stage 06（当前）**：最终文档沉淀与交付收口。  
**Stage 01–05**：gRPC 契约 → 帖子主流程 → 点赞评论计数 → Feed 三路推荐 → 一键验收脚本。

## 交付文档

详见 [`docs/post-service/`](../docs/post-service/)：

- [项目地图](../docs/post-service/00-项目地图.md)
- [业务流程](../docs/post-service/01-业务流程.md)
- [技术架构](../docs/post-service/02-技术架构.md)
- [数据模型](../docs/post-service/03-数据模型.md)
- [接口与 gRPC 契约](../docs/post-service/04-接口与gRPC契约.md)
- [核心调用链](../docs/post-service/05-调用链.md)
- [Redis 与 Feed 设计](../docs/post-service/06-Redis与Feed设计.md)
- [幂等与一致性](../docs/post-service/07-幂等与一致性.md)
- [测试与验收](../docs/post-service/08-测试与验收.md)
- [面试复盘](../docs/post-service/09-面试复盘.md)
- [最终验收报告](../docs/post-service/10-最终验收报告.md)

## 已实现能力

| 链路 | RPC / 能力 |
|---|---|
| 帖子 | CreatePost / GetPostDetail / ListUserPosts / DeletePost |
| 互动 | ActionLike / CreateComment / ListComments / DeleteComment + Redis 计数刷盘 |
| Feed | GetRecommendFeed（recommend + timeline + cold_start 混排） |

## Package & Ports

- Package：`com.dating.post`
- REST：`8084` | gRPC：`9094`

## 一键验收（推荐）

在**仓库根目录**执行，不依赖 PostgreSQL / Redis 启动：

```bash
bash scripts/verify-post-service.sh
```

```powershell
.\scripts\verify-post-service.ps1
```

说明：`.sh` 适用于 Linux/macOS（Git Bash/WSL 亦可）；`.ps1` 在 Windows PowerShell 5.1 下使用英文 PASS/FAIL 标签以避免编码问题，检查项与 `.sh` 一致。

## 本地测试

```bash
cd post-service
mvn test
mvn package -DskipTests
```

## 启动与健康检查

```bash
cd post-service
mvn spring-boot:run
curl http://localhost:8084/health
```

## Debug REST（dev/test，无 gateway）

```bash
# 发帖
curl -X POST http://localhost:8084/internal/debug/post \
  -H "Content-Type: application/json" -H "x-user-id: 10001" \
  -d '{"content":"hello","imageKeys":["post/10001/a.jpg"]}'

# Feed
curl "http://localhost:8084/internal/debug/post/feed?pageSize=10&cursor=0:0" -H "x-user-id: 10001"
```

更多 debug 接口见历史阶段注释；gRPC 需 Metadata `x-user-id`。

## 数据与 Redis

Flyway（`post_center`）：`posts` / `post_images` / `post_stats`（V001），`post_likes` / `post_comments`（V002）。

Redis 前缀 `yanshuqi:` — 详情缓存、计数 delta、Feed 池、timeline、已读 Set（TTL 7 天）。
