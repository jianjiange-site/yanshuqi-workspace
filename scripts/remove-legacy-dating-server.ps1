# 手动清理 legacy 目录

若 `dating-server/` 空目录因文件锁无法删除，请：

1. 关闭占用该目录的 IDE / 终端 / Java 进程
2. 确认 `user-service/` 已在 workspace 根目录且可构建
3. 执行：

```powershell
Remove-Item -Recurse -Force .\dating-server
```

该目录不应包含任何 `pom.xml` 或服务源码。
