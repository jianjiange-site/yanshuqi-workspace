# Stage 00 技术方案修正版：项目基线与共享基建接入

> **结构更新（2026-06-15）**：Java 服务已调整为 workspace 根目录布局（`mobile-gateway/`、`user-service/` 等），不再使用 `dating-server/` 包裹层。下文若仍出现 `dating-server/` 路径，请按根目录结构理解：`deploy/`、`scripts/`、各 Java 服务均在 workspace 根目录。

## 1. 修正原则

根据《Vibe 后端开发规范（学员版）》，本项目统一使用以下个人标识：

```text
个人标识：yanshuqi
workspace 根目录：yanshuqi-workspace
PostgreSQL database：dating_dev_yanshuqi
Redis key 前缀：yanshuqi:<service>:<domain>:<id>
Nacos namespace：yanshuqi-dev
MinIO bucket：dating-yanshuqi
RocketMQ topic / group 前缀：yanshuqi_dev
OpenIM userID 前缀：yanshuqi_
```

Java 源码包名不使用 `yanshuqi`，统一使用：

```text
com.dating.<service>
```

例如：

```text
com.dating.user
com.dating.match
com.dating.im
com.dating.post
com.dating.payment
com.dating.gateway
```

禁止使用：

```text
com.chatvibe.*
com.dating.yanshuqi.*
```

---

## 2. Stage 00 总目标

Stage 00 只完成工程基线和共享基建接入，不实现任何业务逻辑。

本阶段完成后，项目应具备：

```text
yanshuqi-workspace/
├── ai-chat/          # Python AI Chat 服务骨架
├── dating-server/    # Java 微服务 monorepo
└── proto/            # gRPC proto 契约目录
```

并满足：

```text
1. Java 服务工程骨架创建完成
2. Python ai-chat 工程骨架创建完成
3. proto 目录创建完成
4. 配置文件模板创建完成
5. PostgreSQL / Redis / Nacos / MinIO / OpenIM 配置占位完成
6. 每个服务可启动
7. 每个服务提供健康检查
8. 不写业务接口
9. 不写业务表
10. 不提交真实密码、Token、AK、SK、Secret
```

---

## 3. Stage 00 拆分

Stage 00 拆成两个小阶段：

```text
Stage 00-A：工程骨架
Stage 00-B：共享基建接入
```

### 3.1 Stage 00-A：工程骨架

目标：

```text
先建立 yanshuqi-workspace 项目结构，让所有服务具备最小启动能力。
```

本阶段只做：

```text
1. 创建 workspace 目录
2. 创建 ai-chat / dating-server / proto 三个顶层目录
3. 创建 Java 微服务骨架
4. 创建 Python ai-chat 骨架
5. 创建基础配置模板
6. 创建健康检查接口
7. 创建 README / 启动说明 / 验收脚本
8. 不连接真实共享基建
9. 不实现业务逻辑
```

### 3.2 Stage 00-B：共享基建接入

目标：

```text
在工程骨架稳定后，再接入共享 PostgreSQL、Redis、Nacos、MinIO，并预留 OpenIM / RocketMQ 配置。
```

本阶段只做：

```text
1. 接入 PostgreSQL：dating_dev_yanshuqi
2. 接入 Redis：使用 yanshuqi 前缀
3. 接入 Nacos：namespace 使用 yanshuqi-dev
4. 接入 MinIO：bucket 使用 dating-yanshuqi
5. 预留 OpenIM 配置：userID 前缀 yanshuqi_
6. 预留 RocketMQ 配置：topic / group 前缀 yanshuqi_dev
7. 增加基础设施健康检查
8. 不实现业务逻辑
```

---

## 4. 修正后的项目根目录

项目根目录必须是：

```text
yanshuqi-workspace/
├── README.md
├── .gitignore
├── ai-chat/
├── dating-server/
└── proto/
```

完整推荐结构：

```text
yanshuqi-workspace/
├── README.md
├── .gitignore
├── docs/
│   ├── 00_PROJECT_MAP.md
│   ├── 01_BUSINESS_FLOWS.md
│   ├── 02_SERVICE_BOUNDARY.md
│   ├── 03_STAGE_PLAN.md
│   └── stage/
│       └── 00_STAGE_BASELINE_INFRA_DESIGN.md
├── ai-chat/
│   ├── README.md
│   ├── requirements.txt
│   ├── pyproject.toml
│   ├── .env.example
│   ├── Dockerfile
│   ├── app/
│   │   ├── main.py
│   │   ├── config.py
│   │   ├── health.py
│   │   ├── grpc_server.py
│   │   ├── logging_config.py
│   │   ├── agents/
│   │   │   └── __init__.py
│   │   ├── graph/
│   │   │   └── __init__.py
│   │   ├── clients/
│   │   │   └── __init__.py
│   │   └── proto/
│   │       └── __init__.py
│   └── tests/
├── dating-server/
│   ├── mobile-gateway/
│   ├── user-service/
│   ├── im-service/
│   ├── match-service/
│   ├── post-service/
│   ├── payment-service/
│   ├── example-service/
│   ├── deploy/
│   │   ├── .env.example
│   │   ├── docker-compose.app.yml
│   │   ├── nacos/
│   │   ├── postgres/
│   │   ├── redis/
│   │   └── minio/
│   └── scripts/
└── proto/
    ├── README.md
    ├── common/
    ├── user/
    ├── match/
    ├── im/
    ├── post/
    └── payment/
```

注意：

```text
1. Java 微服务必须放在 dating-server/ 下
2. Python ai-chat 必须放在 ai-chat/ 下
3. proto 必须放在 proto/ 下
4. 不再使用 chatvibe-backend/ 作为根目录
5. 不再使用 services/ 包一层 Java 服务
```

---

## 5. Java 服务目录修正

每个 Java 服务是 `dating-server` 下的独立 Maven 模块。

示例：

```text
dating-server/user-service/
├── pom.xml
├── Dockerfile
├── README.md
├── src/main/java/com/dating/user/
│   ├── UserServiceApplication.java
│   ├── controller/
│   │   └── HealthController.java
│   ├── grpc/
│   ├── service/
│   │   └── impl/
│   ├── manager/
│   ├── mapper/
│   ├── entity/
│   ├── dto/
│   ├── vo/
│   ├── client/
│   ├── config/
│   ├── constant/
│   └── exception/
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   ├── bootstrap.yml
│   ├── logback-spring.xml
│   └── db/migration/
│       └── V000__baseline.sql
└── src/test/java/com/dating/user/
```

各服务包名如下：

| 服务              | Java 包名              |
| --------------- | -------------------- |
| mobile-gateway  | `com.dating.gateway` |
| user-service    | `com.dating.user`    |
| match-service   | `com.dating.match`   |
| im-service      | `com.dating.im`      |
| post-service    | `com.dating.post`    |
| payment-service | `com.dating.payment` |
| example-service | `com.dating.example` |

禁止包名：

```text
com.chatvibe.*
com.dating.yanshuqi.*
com.dating.chatvibe.*
```

---

## 6. 服务端口规划

| 服务              | REST 端口 | gRPC 端口 | Nacos 服务名         |
| --------------- | ------: | ------: | ----------------- |
| mobile-gateway  |    8080 |       无 | `mobile-gateway`  |
| user-service    |    8081 |    9091 | `user-service`    |
| match-service   |    8082 |    9092 | `match-service`   |
| im-service      |    8083 |    9093 | `im-service`      |
| post-service    |    8084 |    9094 | `post-service`    |
| payment-service |    8085 |    9095 | `payment-service` |
| ai-chat         |    8090 |    9190 | `ai-chat`         |

Stage 00 只要求：

```text
1. REST 健康检查可访问
2. gRPC 端口可以预留
3. 不要求正式业务 RPC 可用
```

---

## 7. PostgreSQL 修正

### 7.1 数据库命名

统一使用：

```text
dating_dev_yanshuqi
```

如果数据库不存在，需要人工或通过初始化脚本创建：

```sql
CREATE DATABASE dating_dev_yanshuqi;
```

如果共享 PostgreSQL 账号没有建库权限，需要找管理员创建。

### 7.2 schema 命名

在 `dating_dev_yanshuqi` 中按服务建立 schema：

```sql
CREATE SCHEMA IF NOT EXISTS gateway;
CREATE SCHEMA IF NOT EXISTS user_center;
CREATE SCHEMA IF NOT EXISTS match_center;
CREATE SCHEMA IF NOT EXISTS im_center;
CREATE SCHEMA IF NOT EXISTS post_center;
CREATE SCHEMA IF NOT EXISTS payment_center;
CREATE SCHEMA IF NOT EXISTS ai_chat;
```

### 7.3 服务与 schema 映射

| 服务              | database              | schema           |
| --------------- | --------------------- | ---------------- |
| mobile-gateway  | `dating_dev_yanshuqi` | `gateway`        |
| user-service    | `dating_dev_yanshuqi` | `user_center`    |
| match-service   | `dating_dev_yanshuqi` | `match_center`   |
| im-service      | `dating_dev_yanshuqi` | `im_center`      |
| post-service    | `dating_dev_yanshuqi` | `post_center`    |
| payment-service | `dating_dev_yanshuqi` | `payment_center` |
| ai-chat         | `dating_dev_yanshuqi` | `ai_chat`        |

### 7.4 Flyway history 表

每个服务必须使用独立 Flyway history 表：

| 服务              | Flyway history           |
| --------------- | ------------------------ |
| mobile-gateway  | `flyway_history_gateway` |
| user-service    | `flyway_history_user`    |
| match-service   | `flyway_history_match`   |
| im-service      | `flyway_history_im`      |
| post-service    | `flyway_history_post`    |
| payment-service | `flyway_history_payment` |
| ai-chat         | `flyway_history_ai_chat` |

Stage 00 只允许创建：

```text
1. database
2. schema
3. flyway history
4. baseline migration
```

禁止创建：

```text
1. users 表
2. matches 表
3. messages 表
4. posts 表
5. orders 表
6. 任何业务表
```

---

## 8. Redis 修正

### 8.1 Redis key 统一前缀

所有 Redis key 必须以 `yanshuqi` 开头：

```text
yanshuqi:<service>:<domain>:<id>
```

服务前缀如下：

| 服务              | key prefix          |
| --------------- | ------------------- |
| mobile-gateway  | `yanshuqi:gateway:` |
| user-service    | `yanshuqi:user:`    |
| match-service   | `yanshuqi:match:`   |
| im-service      | `yanshuqi:im:`      |
| post-service    | `yanshuqi:post:`    |
| payment-service | `yanshuqi:payment:` |
| ai-chat         | `yanshuqi:ai:`      |

### 8.2 Stage 00 测试 key

Stage 00 只允许使用基础设施探活 key：

```text
yanshuqi:{service}:infra:ping
```

示例：

```text
yanshuqi:user:infra:ping
yanshuqi:match:infra:ping
yanshuqi:im:infra:ping
```

要求：

```text
1. 必须设置 TTL
2. TTL 不超过 60 秒
3. 验证后删除
4. 不允许写业务 key
5. 不允许无前缀 key
6. 禁止 FLUSHDB
7. 禁止 FLUSHALL
```

---

## 9. Nacos 修正

### 9.1 namespace

统一使用：

```text
yanshuqi-dev
```

要求：

```text
1. Config 和 Discovery 必须使用同一个 namespace
2. 禁止使用 public namespace
3. 禁止使用其他学员 namespace
```

### 9.2 group

建议使用规范默认：

```text
DEFAULT_GROUP
```

如导师或项目已有统一 group，再按项目要求调整。

### 9.3 服务注册名

| 服务              | Nacos service name |
| --------------- | ------------------ |
| mobile-gateway  | `mobile-gateway`   |
| user-service    | `user-service`     |
| match-service   | `match-service`    |
| im-service      | `im-service`       |
| post-service    | `post-service`     |
| payment-service | `payment-service`  |
| ai-chat         | `ai-chat`          |

### 9.4 配置 Data ID

| 服务              | Data ID                    |
| --------------- | -------------------------- |
| mobile-gateway  | `mobile-gateway-dev.yaml`  |
| user-service    | `user-service-dev.yaml`    |
| match-service   | `match-service-dev.yaml`   |
| im-service      | `im-service-dev.yaml`      |
| post-service    | `post-service-dev.yaml`    |
| payment-service | `payment-service-dev.yaml` |
| ai-chat         | `ai-chat-dev.yaml`         |

### 9.5 bootstrap.yml 示例

每个 Java 服务的 `bootstrap.yml`：

```yaml
spring:
  application:
    name: ${APP_NAME}
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER_ADDR}
        namespace: ${NACOS_NAMESPACE:yanshuqi-dev}
        group: ${NACOS_GROUP:DEFAULT_GROUP}
        username: ${NACOS_USERNAME}
        password: ${NACOS_PASSWORD}
      config:
        server-addr: ${NACOS_SERVER_ADDR}
        namespace: ${NACOS_NAMESPACE:yanshuqi-dev}
        group: ${NACOS_GROUP:DEFAULT_GROUP}
        username: ${NACOS_USERNAME}
        password: ${NACOS_PASSWORD}
        file-extension: yaml
```

---

## 10. MinIO 修正

### 10.1 bucket

统一使用：

```text
dating-yanshuqi
```

如果 bucket 不存在，需要人工在 MinIO 控制台创建。

### 10.2 object key 格式

统一格式：

```text
<category>/<owner_id>/<yyyymm>/<uuid>.<ext>
```

示例：

```text
avatar/yanshuqi_100001/202606/uuid.jpg
post-image/post_100001/202606/uuid.jpg
attachment/conversation_100001/202606/uuid.png
tmp/yanshuqi_100001/202606/uuid.jpg
```

### 10.3 Stage 00 限制

Stage 00 只做：

```text
1. MinIO endpoint 配置
2. access key / secret key 占位
3. bucket 名配置
4. path-style-access 配置
5. bucket 是否存在检查
```

Stage 00 不做：

```text
1. 头像上传
2. 帖子图片上传
3. 聊天附件上传
4. presigned URL 签发
5. 真实业务文件写入
```

---

## 11. RocketMQ 修正

Stage 00 不强制接入 RocketMQ，只预留配置。

如后续使用，topic / group 必须使用：

```text
yanshuqi_dev_<domain>
```

示例：

```text
yanshuqi_dev_match_delay_topic
yanshuqi_dev_payment_event_topic
yanshuqi_dev_post_counter_topic
```

producer group：

```text
yanshuqi_dev_<service>_producer
```

consumer group：

```text
yanshuqi_dev_<service>_consumer
```

Stage 00 禁止：

```text
1. 创建真实业务 topic
2. 发送业务消息
3. 消费业务消息
```

---

## 12. OpenIM 修正

OpenIM userID 必须使用：

```text
yanshuqi_<name>
```

示例：

```text
yanshuqi_bh_100001
yanshuqi_dh_100001
yanshuqi_test_alice
```

Stage 00 只在 `im-service` 中预留配置：

```yaml
openim:
  api-base-url: ${OPENIM_API_BASE_URL:}
  ws-url: ${OPENIM_WS_URL:}
  admin-user-id: ${OPENIM_ADMIN_USER_ID:}
  admin-secret: ${OPENIM_ADMIN_SECRET:}
  callback-secret: ${OPENIM_CALLBACK_SECRET:}
  user-id-prefix: ${OPENIM_USER_ID_PREFIX:yanshuqi_}
```

Stage 00 禁止：

```text
1. 注册 OpenIM 用户
2. 获取 OpenIM token
3. 调用 OpenIM 管理接口
4. 发送 OpenIM 消息
5. 处理 OpenIM 回调
```

---

## 13. proto 修正

### 13.1 proto 目录

必须放在 workspace 根目录：

```text
yanshuqi-workspace/proto/
├── common/
├── user/
├── match/
├── im/
├── post/
└── payment/
```

### 13.2 Java proto 坐标

必须带 `yanshuqi`：

```xml
<groupId>com.dating.yanshuqi.proto</groupId>
<artifactId>user-proto</artifactId>
<version>0.1.0</version>
```

其他服务示例：

```xml
<groupId>com.dating.yanshuqi.proto</groupId>
<artifactId>match-proto</artifactId>
<version>0.1.0</version>

<groupId>com.dating.yanshuqi.proto</groupId>
<artifactId>im-proto</artifactId>
<version>0.1.0</version>
```

### 13.3 Python proto 包

必须带 `yanshuqi`：

```text
dating-proto-yanshuqi-user==0.1.0
dating-proto-yanshuqi-match==0.1.0
dating-proto-yanshuqi-im==0.1.0
dating-proto-yanshuqi-post==0.1.0
dating-proto-yanshuqi-payment==0.1.0
```

### 13.4 Stage 00 限制

Stage 00 可以创建 proto 目录，但不定义正式业务接口。

只允许：

```text
1. 创建 proto/README.md
2. 创建 common/ 目录
3. 创建各业务域空目录
4. 说明后续 proto 发布规则
```

不允许：

```text
1. 定义 UserService 正式 RPC
2. 定义 MatchService 正式 RPC
3. 定义 ImService 正式 RPC
4. 定义 PaymentService 正式 RPC
5. 生成业务 stub
```

正式 proto 从 Stage 01 开始设计。

---

## 14. .env.example 修正

文件位置：

```text
yanshuqi-workspace/dating-server/deploy/.env.example
```

内容：

```env
# Personal
PERSONAL_NAME=yanshuqi
APP_ENV=dev
TZ=UTC

# PostgreSQL
POSTGRES_HOST=38.76.188.242
POSTGRES_PORT=5433
POSTGRES_DATABASE=dating_dev_yanshuqi
POSTGRES_USERNAME=your-postgres-username
POSTGRES_PASSWORD=your-postgres-password

# Redis
REDIS_HOST=38.76.188.242
REDIS_PORT=6380
REDIS_PASSWORD=your-redis-password
REDIS_DATABASE=1
REDIS_KEY_PREFIX=yanshuqi

# Nacos
NACOS_SERVER_ADDR=38.76.188.242:8848
NACOS_NAMESPACE=yanshuqi-dev
NACOS_GROUP=DEFAULT_GROUP
NACOS_USERNAME=your-nacos-username
NACOS_PASSWORD=your-nacos-password

# MinIO
MINIO_ENDPOINT=https://minio-api.jianjiange.site
MINIO_ACCESS_KEY=your-minio-access-key
MINIO_SECRET_KEY=your-minio-secret-key
MINIO_BUCKET=dating-yanshuqi
MINIO_REGION=us-east-1
MINIO_PATH_STYLE_ACCESS=true

# OpenIM
OPENIM_API_BASE_URL=your-openim-api-base-url
OPENIM_WS_URL=your-openim-ws-url
OPENIM_ADMIN_USER_ID=your-openim-admin-user-id
OPENIM_ADMIN_SECRET=your-openim-admin-secret
OPENIM_CALLBACK_SECRET=your-openim-callback-secret
OPENIM_USER_ID_PREFIX=yanshuqi_

# RocketMQ
ROCKETMQ_NAME_SERVER=38.76.188.242:9876
ROCKETMQ_ACCESS_KEY=your-rocketmq-access-key
ROCKETMQ_SECRET_KEY=your-rocketmq-secret-key
ROCKETMQ_TOPIC_PREFIX=yanshuqi_dev

# ai-chat
AI_CHAT_HTTP_PORT=8090
AI_CHAT_GRPC_PORT=9190
LLM_PROVIDER=disabled
LLM_API_KEY=your-llm-api-key
```

注意：

```text
1. .env.example 可以进 Git
2. .env 不允许进 Git
3. 真实密码不允许进入 application-dev.yml
4. 真实密码不允许进入 README
5. 真实密码不允许进入提交记录
```

---

## 15. application-dev.yml 修正示例

以 `user-service` 为例：

```yaml
server:
  port: 8081

spring:
  application:
    name: user-service

  datasource:
    url: jdbc:postgresql://${POSTGRES_HOST}:${POSTGRES_PORT}/${POSTGRES_DATABASE}?currentSchema=user_center&stringtype=unspecified
    username: ${POSTGRES_USERNAME}
    password: ${POSTGRES_PASSWORD}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 8
      minimum-idle: 2
      connection-init-sql: SET TIME ZONE 'UTC'

  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}
      password: ${REDIS_PASSWORD}
      database: ${REDIS_DATABASE:1}
      timeout: 3s

  flyway:
    enabled: true
    schemas: user_center
    table: flyway_history_user
    baseline-on-migrate: true

app:
  cache:
    key-prefix: ${REDIS_KEY_PREFIX:yanshuqi}
  service:
    name: user-service

management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: always
```

其他服务替换：

```text
server.port
spring.application.name
currentSchema
flyway.schemas
flyway.table
app.service.name
```

---

## 16. Stage 00-A：Cursor 执行说明

请 Cursor 执行以下内容。

### 16.1 任务目标

创建 `yanshuqi-workspace` 工程骨架。

只做工程结构和健康检查，不连接真实共享基建，不写业务逻辑。

### 16.2 必须创建的顶层目录

```text
yanshuqi-workspace/
├── ai-chat/
├── dating-server/
└── proto/
```

### 16.3 dating-server 下必须创建

```text
dating-server/
├── mobile-gateway/
├── user-service/
├── im-service/
├── match-service/
├── post-service/
├── payment-service/
└── example-service/
```

### 16.4 Java 包名要求

每个服务使用：

```text
com.dating.<service>
```

具体：

```text
mobile-gateway → com.dating.gateway
user-service → com.dating.user
match-service → com.dating.match
im-service → com.dating.im
post-service → com.dating.post
payment-service → com.dating.payment
example-service → com.dating.example
```

禁止：

```text
com.chatvibe.*
com.dating.yanshuqi.*
```

### 16.5 Stage 00-A 验收标准

```text
1. yanshuqi-workspace 目录存在
2. ai-chat / dating-server / proto 三个顶层目录存在
3. Java 服务目录完整
4. Java 包名符合 com.dating.<service>
5. 每个 Java 服务有启动类
6. 每个 Java 服务有 /health
7. 每个 Java 服务有 README
8. ai-chat 有 Python 启动骨架
9. proto 有 README 和目录占位
10. 没有任何业务接口
11. 没有任何业务表
12. 没有真实密钥
```

---

## 17. Stage 00-B：Cursor 执行说明

请 Cursor 在 Stage 00-A 验收通过后，再执行 Stage 00-B。

### 17.1 任务目标

接入共享基建配置模板和基础设施健康检查。

### 17.2 PostgreSQL

使用：

```text
database = dating_dev_yanshuqi
```

schema：

```text
gateway
user_center
match_center
im_center
post_center
payment_center
ai_chat
```

生成：

```text
dating-server/deploy/postgres/
├── 00_create_database.sql
├── 01_create_schemas.sql
└── 02_check_connection.sql
```

### 17.3 Redis

使用：

```text
REDIS_KEY_PREFIX=yanshuqi
```

测试 key：

```text
yanshuqi:{service}:infra:ping
```

### 17.4 Nacos

使用：

```text
NACOS_NAMESPACE=yanshuqi-dev
NACOS_GROUP=DEFAULT_GROUP
```

生成配置模板：

```text
dating-server/deploy/nacos/
├── mobile-gateway-dev.yaml
├── user-service-dev.yaml
├── match-service-dev.yaml
├── im-service-dev.yaml
├── post-service-dev.yaml
├── payment-service-dev.yaml
└── ai-chat-dev.yaml
```

### 17.5 MinIO

使用：

```text
MINIO_BUCKET=dating-yanshuqi
```

生成说明：

```text
dating-server/deploy/minio/create_buckets.md
```

### 17.6 OpenIM

只在 `im-service` 中预留：

```text
OPENIM_USER_ID_PREFIX=yanshuqi_
```

不调用 OpenIM。

### 17.7 RocketMQ

只预留：

```text
ROCKETMQ_TOPIC_PREFIX=yanshuqi_dev
```

不创建 topic，不收发消息。

### 17.8 Stage 00-B 验收标准

```text
1. PostgreSQL 配置指向 dating_dev_yanshuqi
2. 每个服务使用自己的 schema
3. Redis key 前缀是 yanshuqi
4. Nacos namespace 是 yanshuqi-dev
5. MinIO bucket 是 dating-yanshuqi
6. OpenIM userID 前缀是 yanshuqi_
7. RocketMQ topic / group 前缀是 yanshuqi_dev
8. 每个服务能读取配置
9. 每个服务能健康检查
10. 不实现业务逻辑
11. 不创建业务表
12. 不写业务 Redis key
13. 不提交真实密钥
```

---

## 18. 最终禁止事项

Stage 00 全阶段禁止：

```text
1. 禁止使用 chatvibe-backend 作为根目录
2. 禁止使用 services/ 包 Java 微服务
3. 禁止 Java 包名使用 com.chatvibe.*
4. 禁止 Java 包名使用 com.dating.yanshuqi.*
5. 禁止 Redis key 不带 yanshuqi 前缀
6. 禁止使用 public Nacos namespace
7. 禁止使用其他学员 namespace
8. 禁止 MinIO bucket 使用公共 bucket
9. 禁止 proto 坐标不带 yanshuqi
10. 禁止创建业务表
11. 禁止实现业务接口
12. 禁止提交真实密码、Token、AK、SK、Secret
```

---

## 19. Stage 00 完成后必须输出

Cursor 完成后必须输出：

```text
1. 当前执行的是 Stage 00-A 还是 Stage 00-B
2. 完成内容
3. 变更文件清单
4. 项目目录结构
5. Java 包名检查结果
6. 每个服务启动命令
7. 每个服务健康检查地址
8. PostgreSQL database / schema 说明
9. Redis key 前缀说明
10. Nacos namespace / Data ID 说明
11. MinIO bucket 说明
12. OpenIM userID 前缀说明
13. RocketMQ topic / group 前缀说明
14. proto 坐标说明
15. 人工必须配置的内容
16. 本阶段没有实现的内容
17. 风险项
18. 是否满足进入下一阶段条件
```
