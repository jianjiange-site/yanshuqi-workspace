# yanshuqi-workspace 工程约束

## 1. 根目录结构

```text
yanshuqi-workspace/
├── ai-chat/
├── mobile-gateway/
├── user-service/
├── match-service/
├── im-service/
├── post-service/
├── payment-service/
├── example-service/
├── proto/
├── deploy/
├── scripts/
├── docs/
└── study-docs/
```

说明：

- 所有 Java 服务位于 workspace 根目录，**不使用** `dating-server/` 包裹层。
- 每个 Java 服务是**独立 Maven 工程**，无根 parent pom。
- 全量构建使用 `scripts/build-all.ps1` 或 `scripts/build-all.sh`。

## 2. Cursor 开发铁律

1. 每次只允许修改当前阶段指定服务。
2. 不允许跨服务直接 import Java 类。
3. 不允许服务访问其他服务 schema。
4. 不允许 gateway 写业务逻辑。
5. 不允许 ai-chat 直接访问 Java 服务数据库。
6. 不允许业务服务之间 HTTP 互调，跨服务只能走 gRPC。
7. 不允许创建未在阶段方案中声明的业务表。
8. 不允许写无 `yanshuqi` 前缀的 Redis key。
9. 不允许提交真实密码、Token、AK、SK、Secret。

## 3. 服务边界

| 服务 | 职责 |
|---|---|
| mobile-gateway | App REST 入口，鉴权、参数校验、路由、聚合，不写业务 |
| user-service | 用户资料、用户状态、用户基础信息 |
| match-service | 推荐、划卡、匹配、配额 |
| im-service | OpenIM / LiveKit 能力封装和 IM 编排 |
| post-service | Feed、发帖、点赞、评论 |
| payment-service | 金币、订阅、支付流水 |
| ai-chat | Python Agent、LangGraph、LLM 调用，不写 Java 业务逻辑 |
| proto | 跨服务契约唯一来源 |

## 4. 共享资源命名

| 资源 | 命名 |
|---|---|
| personal name | `yanshuqi` |
| PostgreSQL database | `dating_dev_yanshuqi` |
| Redis key prefix | `yanshuqi:<service>:<domain>:<id>` |
| Nacos namespace | `yanshuqi-dev` |
| MinIO bucket | `dating-yanshuqi` |
| RocketMQ topic/group prefix | `yanshuqi_dev` |
| OpenIM userID prefix | `yanshuqi_` |
| Java proto 坐标 | `com.dating.yanshuqi.proto:<service>-proto:0.1.0` |
| Python proto 包 | `dating-proto-yanshuqi-<service>==0.1.0` |

## 5. Java 包名

必须使用：

```text
com.dating.gateway
com.dating.user
com.dating.match
com.dating.im
com.dating.post
com.dating.payment
com.dating.example
```

禁止：

```text
com.chatvibe.*
com.dating.yanshuqi.*
```
