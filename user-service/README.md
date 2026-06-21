# user-service

ChatVibe / Dating App **用户域服务**（gRPC + HTTP 健康检查）。

**当前状态**：已完成 USER-01～USER-09-4；Profile / Onboarding + Avatar Upload + HomeCard 支撑（17 个 gRPC RPC）。

## 包名与端口（dev）

| 项 | 值 |
|---|---|
| Package | `com.dating.user` |
| HTTP | 8081 |
| gRPC | 9091 |
| DB Schema | `user_center` |

## 文档索引（建议阅读顺序）

| 顺序 | 文档 | 说明 |
|:---:|---|---|
| 1 | [01-最终验收报告.md](docs/01-最终验收报告.md) | 模块总览、验收结论与测试结果 |
| 2 | [02-模块边界说明.md](docs/02-模块边界说明.md) | 负责 / 不负责什么 |
| 3 | [03-gRPC接口清单.md](docs/03-gRPC接口清单.md) | 17 个 RPC 契约与读写行为 |
| 4 | [04-核心调用链复盘.md](docs/04-核心调用链复盘.md) | 核心 RPC 真实代码路径 |
| 5 | [05-数据模型复盘.md](docs/05-数据模型复盘.md) | 6 张表与关键字段设计 |
| 6 | [06-缓存异常与日志复盘.md](docs/06-缓存异常与日志复盘.md) | Redis、异常映射、日志脱敏 |
| 7 | [07-学习复盘.md](docs/07-学习复盘.md) | 为什么这样设计 |
| 8 | [08-面试问答材料.md](docs/08-面试问答材料.md) | 口述演练 |
| 9 | [09-代码阅读指南.md](docs/09-代码阅读指南.md) | 源码阅读路径 |
| 10 | [USER_SERVICE_SWAGGER_AUTH_ADAPTATION.md](docs/USER_SERVICE_SWAGGER_AUTH_ADAPTATION.md) | USER-09-1 Auth 登录来源适配 |
| 11 | [USER_SERVICE_SWAGGER_PROFILE_ADAPTATION.md](docs/USER_SERVICE_SWAGGER_PROFILE_ADAPTATION.md) | USER-09-2 Profile / Onboarding 适配 |
| 12 | [USER_SERVICE_SWAGGER_UPLOAD_ADAPTATION.md](docs/USER_SERVICE_SWAGGER_UPLOAD_ADAPTATION.md) | USER-09-3 Avatar / Upload 适配 |
| 13 | [USER_SERVICE_SWAGGER_HOME_CARD_ADAPTATION.md](docs/USER_SERVICE_SWAGGER_HOME_CARD_ADAPTATION.md) | USER-09-4 ProfileView / HomeCard 支撑 |
| 14 | [USER_SERVICE_SWAGGER_DIFF.md](docs/USER_SERVICE_SWAGGER_DIFF.md) | Swagger 差异报告 |

## 常用命令

```bash
cd user-service

# 单元测试
mvn clean test

# 打包（跳过测试）
mvn clean package -DskipTests

# dev 启动
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 健康检查
curl http://localhost:8081/health

# 质量 / 最终验收（需服务已启动，替换 user_id）
python scripts/run_user_07_quality_verify.py --user-id 325259949544443904
python scripts/run_user_08_final_verify.py --user-id 325259949544443904
python scripts/run_user_09_1_auth_login_source_verify.py
python scripts/run_user_09_2_profile_onboarding_verify.py
python scripts/run_user_09_3_avatar_upload_verify.py
python scripts/run_user_09_4_home_card_verify.py
```

## Health

- GET http://localhost:8081/health
- GET http://localhost:8081/actuator/health
