# ai-chat

Python AI Chat 服务（Stage 00-B：共享基建接入）。

## 端口

| 类型 | 端口 |
|---|---:|
| HTTP | 8090 |
| gRPC（健康检查预留） | 9190 |

## Schema

- Database: `dating_dev_yanshuqi`
- Schema: `ai_chat`（仅 ai-chat 使用，不访问 Java 服务 schema）

## 本地启动

```bash
cd ai-chat
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
cp .env.example .env
# 编辑 .env 填入共享基建凭证（不要提交 Git）
python -m app.main
```

## 健康检查

| 类型 | 地址 |
|---|---|
| 存活 | `GET http://localhost:8090/health` |
| 基建 | `GET http://localhost:8090/health/infra` |
| gRPC | `grpc.health.v1.Health/Check` on `:9190` |

## Redis 探活 key

`yanshuqi:ai:infra:ping`（TTL 60s，验证后删除）

## 说明

- Stage 00-B 不调用 LLM、不实现 Agent 业务。
- 不直接访问 Java 服务数据库，仅连接 `ai_chat` schema。
