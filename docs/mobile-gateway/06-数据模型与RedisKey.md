# mobile-gateway 数据模型与 Redis Key

gateway **仅持有鉴权域数据**，不持有 user/match/post 业务表。

## 1. auth_device

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGSERIAL PK | |
| user_id | BIGINT NOT NULL | 用户 ID |
| device_id | VARCHAR(128) | 设备 ID |
| platform | INTEGER | 平台 |
| device_model / os_version / app_version | VARCHAR | 设备信息 |
| push_token | VARCHAR(512) | 推送 token |
| last_login_at | TIMESTAMPTZ | 最近登录 |
| created_at / updated_at | TIMESTAMPTZ | |

**索引**

- UNIQUE `(user_id, device_id, platform)`
- INDEX `(device_id, platform)`
- INDEX `(user_id)`

## 2. auth_refresh_token

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGSERIAL PK | |
| user_id | BIGINT | |
| device_id | VARCHAR(128) | |
| platform | INTEGER | |
| token_hash | VARCHAR(64) UNIQUE | refresh SHA-256 |
| jti | VARCHAR(64) UNIQUE | refresh 会话 ID |
| expires_at | TIMESTAMPTZ | |
| used_at | TIMESTAMPTZ | 轮换标记 |
| revoked_at | TIMESTAMPTZ | 登出撤销 |
| replaced_by_jti | VARCHAR(64) | 轮换链 |
| created_at / updated_at | TIMESTAMPTZ | |

**索引**

- UNIQUE `token_hash`
- UNIQUE `jti`
- INDEX `(user_id, device_id)`
- INDEX `expires_at`

迁移脚本：`mobile-gateway/src/main/resources/db/migration/V20260623_001__create_gateway_auth_tables.sql`  
Schema：`gateway`（Flyway `flyway_history_gateway`）。

## 3. Redis Key（Auth 域）

| Key 模式 | 类型 | TTL | 说明 |
| --- | --- | --- | --- |
| `gateway:auth:sms:{phone}` | STRING | 300s（可配置） | 短信验证码 |
| `gateway:auth:sms:cooldown:{phone}` | STRING | 60s（可配置） | 发码冷却 |
| `gateway:auth:blacklist:{jti}` | STRING | 至 access 过期 | logout 黑名单 |
| `yanshuqi:gateway:infra:ping` | STRING | 60s | 基建探活（dev） |

定义类：`AuthRedisKeys.java`。

## 4. 不包含的内容

- user_profile、swipe、post、coin_ledger 等业务表 → 分别在 user/match/post/payment-service。
- Match/Post Redis 队列 → 见 `docs/match-service/03-数据模型.md`、`docs/post-service/06-Redis与Feed设计.md`。

## 5. JWT 密钥配置

非表数据，但属于 gateway 持久化配置：

- `gateway.jwt.private-key-base64` / `public-key-base64`（环境变量 `GATEWAY_JWT_*`）
- RS256 签发 access/refresh
