# proto

跨语言 gRPC 契约仓库（Stage 00-A 仅占位目录）。

## 目录结构

```text
proto/
├── common/
├── user/
├── match/
├── im/
├── post/
└── payment/
```

## 发布坐标（Stage 01 起使用）

### Java（Maven）

```xml
<groupId>com.dating.yanshuqi.proto</groupId>
<artifactId>user-proto</artifactId>
<version>0.1.0</version>
```

其他模块：`match-proto`、`im-proto`、`post-proto`、`payment-proto`。

### Python

```text
dating-proto-yanshuqi-user==0.1.0
dating-proto-yanshuqi-match==0.1.0
dating-proto-yanshuqi-im==0.1.0
dating-proto-yanshuqi-post==0.1.0
dating-proto-yanshuqi-payment==0.1.0
```

## Stage 00-A 限制

- 不定义正式业务 RPC。
- 不生成业务 stub。
- 正式 proto 从 Stage 01 开始设计。

## 规则

1. proto 只在 `proto/` 仓库维护，业务工程通过 Nexus 依赖引入。
2. 禁止在 Java / Python 业务工程中复制 `.proto` 文件。
3. 变更 proto 后需发布新版本 stub。
