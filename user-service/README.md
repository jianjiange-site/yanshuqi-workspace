# user-service

ChatVibe / Dating App **用户域服务**（gRPC + HTTP 健康检查）。

**当前状态**：已完成 USER-01～USER-07 业务开发与 USER-08 模块验收文档；9 个 gRPC RPC 可交付。

## 包名与端口（dev）

| 项 | 值 |
|---|---|
| Package | `com.dating.user` |
| HTTP | 8081 |
| gRPC | 9091 |
| DB Schema | `user_center` |

## 文档索引

| 文档 | 说明 |
|---|---|
| [最终验收报告.md](docs/最终验收报告.md) | 最终验收报告 |
| [gRPC接口清单.md](docs/gRPC接口清单.md) | gRPC 接口清单 |
| [核心调用链复盘.md](docs/核心调用链复盘.md) | 核心调用链 |
| [数据模型复盘.md](docs/数据模型复盘.md) | 数据模型复盘 |
| [缓存异常与日志复盘.md](docs/缓存异常与日志复盘.md) | 缓存 / 异常 / 日志 |
| [模块边界说明.md](docs/模块边界说明.md) | 模块边界 |
| [学习复盘.md](docs/学习复盘.md) | 学习复盘 |
| [面试问答材料.md](docs/面试问答材料.md) | 面试问答 |
| [代码阅读指南.md](docs/代码阅读指南.md) | 代码阅读指南 |

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
```

## Health

- GET http://localhost:8081/health
- GET http://localhost:8081/actuator/health
