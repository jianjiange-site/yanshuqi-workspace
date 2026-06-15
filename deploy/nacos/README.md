# Nacos Config Templates (yanshuqi-dev)

## Namespace / Group

| 项 | 值 |
|---|---|
| Namespace | `yanshuqi-dev` |
| Group | `DEFAULT_GROUP` |

## Data ID 列表

| 服务 | Data ID |
|---|---|
| mobile-gateway | `mobile-gateway-dev.yaml` |
| user-service | `user-service-dev.yaml` |
| match-service | `match-service-dev.yaml` |
| im-service | `im-service-dev.yaml` |
| post-service | `post-service-dev.yaml` |
| payment-service | `payment-service-dev.yaml` |
| ai-chat | `ai-chat-dev.yaml` |

## 导入步骤

1. 登录 Nacos 控制台，切换到 namespace `yanshuqi-dev`。
2. 在 **配置管理** 中新建配置，Group 使用 `DEFAULT_GROUP`。
3. 将本目录下对应 `{service}-dev.yaml` 内容粘贴到配置中。
4. 敏感字段（数据库密码、Redis 密码、MinIO AK/SK）在 Nacos 中使用占位符或 Nacos 加密配置，**不要写入 Git 仓库**。
5. 启动 Java 服务前，本地仍需通过环境变量或 `.env` 注入 `${POSTGRES_USERNAME}` 等占位符。

## 注意

- Config 与 Discovery 必须使用同一 namespace。
- 禁止使用 `public` namespace 或其他学员 namespace。
