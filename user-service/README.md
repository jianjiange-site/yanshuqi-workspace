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

## 文档索引（建议阅读顺序）

| 顺序 | 文档 | 说明 |
|:---:|---|---|
| 1 | [01-最终验收报告.md](docs/01-最终验收报告.md) | 模块总览、验收结论与测试结果 |
| 2 | [02-模块边界说明.md](docs/02-模块边界说明.md) | 负责 / 不负责什么 |
| 3 | [03-gRPC接口清单.md](docs/03-gRPC接口清单.md) | 9 个 RPC 契约与读写行为 |
| 4 | [04-核心调用链复盘.md](docs/04-核心调用链复盘.md) | 核心 RPC 真实代码路径 |
| 5 | [05-数据模型复盘.md](docs/05-数据模型复盘.md) | 6 张表与关键字段设计 |
| 6 | [06-缓存异常与日志复盘.md](docs/06-缓存异常与日志复盘.md) | Redis、异常映射、日志脱敏 |
| 7 | [07-学习复盘.md](docs/07-学习复盘.md) | 为什么这样设计 |
| 8 | [08-面试问答材料.md](docs/08-面试问答材料.md) | 口述演练 |
| 9 | [09-代码阅读指南.md](docs/09-代码阅读指南.md) | 源码阅读路径 |

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
