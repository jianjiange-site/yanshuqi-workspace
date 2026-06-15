# Redis Key 规范 (yanshuqi)

## 统一前缀

所有 Redis key 必须以 `yanshuqi` 开头：

```text
yanshuqi:<service>:<domain>:<id>
```

环境变量：`REDIS_KEY_PREFIX=yanshuqi`

## 各服务 key 前缀

| 服务 | key prefix |
|---|---|
| mobile-gateway | `yanshuqi:gateway:` |
| user-service | `yanshuqi:user:` |
| match-service | `yanshuqi:match:` |
| im-service | `yanshuqi:im:` |
| post-service | `yanshuqi:post:` |
| payment-service | `yanshuqi:payment:` |
| ai-chat | `yanshuqi:ai:` |
| example-service | `yanshuqi:example:` |

## Stage 00-B 基础设施探活 key

格式：

```text
yanshuqi:{service}:infra:ping
```

| 服务 | 探活 key | TTL |
|---|---|---:|
| mobile-gateway | `yanshuqi:gateway:infra:ping` | 60s |
| user-service | `yanshuqi:user:infra:ping` | 60s |
| match-service | `yanshuqi:match:infra:ping` | 60s |
| im-service | `yanshuqi:im:infra:ping` | 60s |
| post-service | `yanshuqi:post:infra:ping` | 60s |
| payment-service | `yanshuqi:payment:infra:ping` | 60s |
| ai-chat | `yanshuqi:ai:infra:ping` | 60s |
| example-service | `yanshuqi:example:infra:ping` | 60s |

## 规则

1. 探活 key 必须设置 TTL（不超过 60 秒）。
2. 写入验证后立即删除。
3. Stage 00-B 禁止写入业务 key。
4. 禁止无前缀 key。
5. 禁止 `FLUSHDB` / `FLUSHALL`。
