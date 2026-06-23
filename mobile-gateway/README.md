# mobile-gateway

移动端 **BFF（Backend For Frontend）**：REST API 入口，JWT 鉴权，gRPC 转发至 user / match / post 微服务。

包名：`com.dating.gateway`  
REST 端口：**8080**

## 服务定位

- 对外：App 使用的 REST + Swagger 契约。
- 对内：gRPC Client 调用下游；本地持有 Auth 表、JWT、短信 mock、logout 黑名单。
- **不是**纯路由网关，也**不是** Payment/IM 的真实业务实现方。

## 已实现能力（生产 profile 可用）

| 模块 | REST 前缀 | 下游 |
| --- | --- | --- |
| Auth | `/api/v1/auth/**` | user-service + gateway DB/Redis |
| Profile | `/api/v1/profile/**` | user-service |
| Upload | `/api/v1/upload/**` | user-service |
| Home | `/api/v1/home/**` | user-service |
| Match | `/api/v1/match/**` | match-service |
| Post | `/api/v1/post/**` | post-service |

## 契约占位（prod/dev 返回 not ready）

| 模块 | REST | prod/dev 行为 | mock/test |
| --- | --- | --- | --- |
| Payment | `/api/v1/payment/**`（10 个） | `10701` payment-service 尚未就绪 | MockPaymentBffServiceImpl |
| IM Token | `GET /api/v1/im/token` | `10801` | mock token |
| Call Token | `POST /api/v1/call/token` | `10802` | mock LiveKit token |
| OpenIM Callback | `POST /callback/openim/{cmd}` | CallbackResponse `10803`，**无 JWT** | 占位成功 |

**禁止**在 prod 返回 mock 支付/IM 数据。真实能力依赖 payment-service / im-service 后续 proto + gRPC。

## 启动

```bash
cd mobile-gateway
# JDK 21+
mvn spring-boot:run
```

依赖（dev 全功能）：PostgreSQL（schema `gateway`）、Redis、user/match/post gRPC 进程。

健康检查：

- `GET http://localhost:8080/health`
- `GET http://localhost:8080/actuator/health`

## Profile

| Profile | 说明 |
| --- | --- |
| dev | 默认；SMS mock；允许 `X-User-Id` 兜底 |
| test | 单测；Mock Payment/Im |
| prod | 禁止 X-User-Id；Payment/Im not ready |
| mock | Payment/Im 返回 mock 数据 |

## 测试

```bash
cd mobile-gateway
mvn -B -ntp clean test
```

仓库根目录最终验收：

```bash
bash scripts/verify-gateway-final.sh
```

```powershell
.\scripts\verify-gateway-final.ps1
```

## 主要接口数量

- Auth 6 + Profile 2 + Upload 2 + Home 1 + Match 7 + Post 10 = **28 个已接入**
- Payment 10 + IM/Call/Callback 3 = **13 个契约占位**

详见 [docs/mobile-gateway/04-接口地图.md](../docs/mobile-gateway/04-接口地图.md)。

## 文档

- [00-项目地图](../docs/mobile-gateway/00-项目地图.md)
- [01-业务流程](../docs/mobile-gateway/01-业务流程.md)
- [02-技术架构](../docs/mobile-gateway/02-技术架构.md)
- [03-鉴权与安全设计](../docs/mobile-gateway/03-鉴权与安全设计.md)
- [04-接口地图](../docs/mobile-gateway/04-接口地图.md)
- [05-REST到gRPC调用链](../docs/mobile-gateway/05-REST到gRPC调用链.md)
- [06-数据模型与RedisKey](../docs/mobile-gateway/06-数据模型与RedisKey.md)
- [07-跨服务适配说明](../docs/mobile-gateway/07-跨服务适配说明.md)
- [08-异常码与边界处理](../docs/mobile-gateway/08-异常码与边界处理.md)
- [09-配置启动与部署说明](../docs/mobile-gateway/09-配置启动与部署说明.md)
- [10-测试与最终验收清单](../docs/mobile-gateway/10-测试与最终验收清单.md)
- [11-阶段复盘](../docs/mobile-gateway/11-阶段复盘.md)
- [12-面试复盘](../docs/mobile-gateway/12-面试复盘.md)
