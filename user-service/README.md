# user-service

ChatVibe / Dating App **用户域服务**（gRPC + HTTP 健康检查）。

**当前状态**：**USER-01～USER-09 已完成**（含 Swagger 适配 Auth / Profile / Upload / HomeCard + 缓存异常日志治理）。

Swagger 契约归属 **mobile-gateway REST**；user-service 提供 **17 个 gRPC** 内部支撑，不暴露 REST、不签发 JWT。

## 包名与端口（dev）

| 项 | 值 |
|---|---|
| Package | `com.dating.user` |
| HTTP | 8081 |
| gRPC | 9091 |
| DB Schema | `user_center` |

## 文档索引

### 中文最终文档（建议主读）

| 顺序 | 文档 | 说明 |
|:---:|---|---|
| 1 | [01-最终验收报告.md](docs/01-最终验收报告.md) | 模块总览、USER-09 验收结论 |
| 2 | [02-模块边界说明.md](docs/02-模块边界说明.md) | 负责 / 不负责 |
| 3 | [03-gRPC接口清单.md](docs/03-gRPC接口清单.md) | 17 个 RPC |
| 4 | [04-核心调用链复盘.md](docs/04-核心调用链复盘.md) | 调用链 |
| 5 | [05-数据模型复盘.md](docs/05-数据模型复盘.md) | 表与字段 |
| 6 | [06-缓存异常与日志复盘.md](docs/06-缓存异常与日志复盘.md) | Redis / 异常 / 日志 |
| 7 | [07-学习复盘.md](docs/07-学习复盘.md) | 学习总结 |
| 8 | [08-面试问答材料.md](docs/08-面试问答材料.md) | 面试口述 |
| 9 | [09-代码阅读指南.md](docs/09-代码阅读指南.md) | 源码路径 |

### Swagger 阶段过程文档（参考，不再扩写）

| 文档 | 说明 |
|---|---|
| [USER_SERVICE_SWAGGER_DIFF.md](docs/USER_SERVICE_SWAGGER_DIFF.md) | Swagger 差异总报告 |
| [USER_SERVICE_SWAGGER_AUTH_ADAPTATION.md](docs/USER_SERVICE_SWAGGER_AUTH_ADAPTATION.md) | USER-09-1 |
| [USER_SERVICE_SWAGGER_PROFILE_ADAPTATION.md](docs/USER_SERVICE_SWAGGER_PROFILE_ADAPTATION.md) | USER-09-2 |
| [USER_SERVICE_SWAGGER_UPLOAD_ADAPTATION.md](docs/USER_SERVICE_SWAGGER_UPLOAD_ADAPTATION.md) | USER-09-3 |
| [USER_SERVICE_SWAGGER_HOME_CARD_ADAPTATION.md](docs/USER_SERVICE_SWAGGER_HOME_CARD_ADAPTATION.md) | USER-09-4 |
| [USER_SERVICE_SWAGGER_GOVERNANCE.md](docs/USER_SERVICE_SWAGGER_GOVERNANCE.md) | USER-09-5 治理 |

## 常用命令

```bash
cd user-service

mvn clean test
mvn clean package -DskipTests

# 需 deploy/.env 环境变量
mvn spring-boot:run -Dspring-boot.run.profiles=dev

curl http://localhost:8081/actuator/health
grpcurl -plaintext localhost:9091 grpc.health.v1.Health/Check

python scripts/run_user_09_5_governance_verify.py
python scripts/run_user_09_final_verify.py
```

## 当前不包含

- mobile-gateway REST / Swagger 对外暴露
- JWT 签发 / refresh / logout
- 真实短信 / OAuth verify
- 图片裁剪 / 多规格缩略图 pipeline
- match / post / payment / im 聚合

## Health

- GET http://localhost:8081/health
- GET http://localhost:8081/actuator/health
