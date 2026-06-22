# yanshuqi-workspace

Dating App 后端 monorepo（学员标识：`yanshuqi`）。

## 目录结构

```text
yanshuqi-workspace/
├── README.md
├── ai-chat/
├── mobile-gateway/
├── user-service/
├── match-service/
├── im-service/
├── post-service/
├── payment-service/
├── example-service/
├── proto/
├── deploy/
├── scripts/
├── docs/
└── study-docs/
```

## 当前阶段

**Stage 00-A：工程骨架**（结构已按导师要求调整为根目录服务布局）

- 七个 Java 微服务独立 Maven 工程 + 健康检查
- Python ai-chat 骨架
- proto 目录占位
- 配置模板位于 `deploy/`

下一阶段：**Stage 00-B：共享基建接入**

## 工程约束

详见 [docs/ENGINEERING_RULES.md](docs/ENGINEERING_RULES.md)

## Java 服务

| 服务 | 包名 | REST 端口 |
|---|---|---:|
| mobile-gateway | `com.dating.gateway` | 8080 |
| user-service | `com.dating.user` | 8081 |
| match-service | `com.dating.match` | 8082 |
| im-service | `com.dating.im` | 8083 |
| post-service | `com.dating.post` | 8084 |
| payment-service | `com.dating.payment` | 8085 |
| example-service | `com.dating.example` | 8086 |

## Java 服务启动说明

### 全量构建

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot"  # 示例，需 JDK 17+
.\scripts\build-all.ps1
```

```bash
bash scripts/build-all.sh
```

### 单服务启动（示例：user-service）

```bash
cd user-service
mvn spring-boot:run
curl http://localhost:8081/health
```

### ai-chat

```bash
cd ai-chat
python -m venv .venv
.venv\Scripts\activate   # Windows
pip install -r requirements.txt
cp .env.example .env
python -m app.main
curl http://localhost:8090/health
```

## 配置模板

- 共享环境变量模板：`deploy/.env.example`
- ai-chat 环境变量模板：`ai-chat/.env.example`

## 验收

```powershell
.\scripts\verify-stage-00a.ps1
```

```bash
bash scripts/verify-stage-00a.sh
```

## 文档

- 工程约束：`docs/ENGINEERING_RULES.md`
- 阶段设计：`study-docs/stage/00_STAGE_BASELINE_INFRA_DESIGN.md.md`
- 阶段计划：`study-docs/03生产级开发阶段计划.md`

## MatchService 开发交付文档

- [docs/match-service/00-项目地图.md](docs/match-service/00-项目地图.md) — 项目地图与模块边界
- [docs/match-service/01-业务流程.md](docs/match-service/01-业务流程.md) — 业务流程（Feed/Swipe/Match/D1/Visit）
- [docs/match-service/02-技术架构.md](docs/match-service/02-技术架构.md) — 技术架构与 Mermaid 图
- [docs/match-service/03-数据模型.md](docs/match-service/03-数据模型.md) — PostgreSQL 四表 + Redis Key
- [docs/match-service/04-接口地图.md](docs/match-service/04-接口地图.md) — REST + gRPC 接口地图
- [docs/match-service/05-调用链.md](docs/match-service/05-调用链.md) — 核心调用链
- [docs/match-service/06-技术决策.md](docs/match-service/06-技术决策.md) — 技术决策与取舍
- [docs/match-service/07-问题与方案.md](docs/match-service/07-问题与方案.md) — 问题与方案
- [docs/match-service/08-验收清单.md](docs/match-service/08-验收清单.md) — 最终验收清单
- [docs/match-service/09-阶段复盘.md](docs/match-service/09-阶段复盘.md) — 分阶段复盘（0–9）
- [docs/match-service/10-面试复盘.md](docs/match-service/10-面试复盘.md) — 面试复盘材料
- [docs/match-service/11-阶段8外部集成清单.md](docs/match-service/11-阶段8外部集成清单.md) — 阶段 8 外部集成清单

MatchService 一键验收：

```powershell
.\scripts\verify-match-final.ps1
```

```bash
bash scripts/verify-match-final.sh
```

## 红线

- Java 包名使用 `com.dating.<service>`，禁止 `com.chatvibe.*` / `com.dating.yanshuqi.*`
- 禁止提交真实密码、Token、AK、SK、Secret
- Stage 00-A 不实现业务接口、不创建业务表、不连接真实共享基建
