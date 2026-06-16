# USER-01 验收清单

## 数据库

- [ ] Flyway 执行成功，无 migration 冲突
- [ ] `user_center` schema 存在
- [ ] 6 张核心表均已创建
- [ ] 每张表有物理主键 `id` 和业务主键
- [ ] 每张表有 `created_at`、`updated_at`、`deleted`
- [ ] 时间字段均为 `TIMESTAMPTZ`
- [ ] 表和字段 COMMENT 完整
- [ ] 唯一索引和普通索引符合技术方案

## 代码

- [ ] 包名为 `com.dating.user`
- [ ] 6 个 Entity 与表结构 1:1 映射
- [ ] 6 个 Mapper 各对应单表，无 JOIN
- [ ] 6 个 Manager 仅封装单表访问，无注册登录流程
- [ ] 枚举和 Redis 前缀常量已定义
- [ ] 基础异常结构已建立
- [ ] 无业务 REST / gRPC 接口
- [ ] 无 Redis 读写逻辑
- [ ] 未修改其他服务

## 验证命令

```bash
cd user-service
mvn clean compile test
psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_USERNAME -d dating_dev_yanshuqi -f scripts/verify_user_01_schema.sql
```
