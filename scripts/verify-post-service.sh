#!/usr/bin/env bash
# PostService 阶段 5 一键验收脚本（Linux/macOS）
# 从仓库根目录执行：bash scripts/verify-post-service.sh
# 不依赖 PostgreSQL / Redis 启动，不启动 post-service 进程。
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
POST_DIR="$ROOT/post-service"
PROTO_FILE="$ROOT/proto/post/post_service.proto"
PASS=0
FAIL=0

# 若未设置 JAVA_HOME，尝试常见 JDK 路径
if [ -z "${JAVA_HOME:-}" ]; then
  if [ -n "${MSYSTEM:-}" ] || [ "${OSTYPE:-}" = "msys" ]; then
    # Git Bash / MSYS：Windows mvn 需要 Windows 风格 JAVA_HOME
    export JAVA_HOME="C:/Program Files/Eclipse Adoptium/jdk-21.0.11.10-hotspot"
  else
    for candidate in \
      "/c/Program Files/Eclipse Adoptium/jdk-21.0.11.10-hotspot" \
      "/usr/lib/jvm/java-21-openjdk" \
      "/usr/lib/jvm/java-17-openjdk"; do
      if [ -d "$candidate" ]; then
        export JAVA_HOME="$candidate"
        break
      fi
    done
  fi
  if [ -n "${JAVA_HOME:-}" ] && [ -d "$JAVA_HOME" ]; then
    echo "[提示] 已自动设置 JAVA_HOME=$JAVA_HOME"
  fi
fi

run_mvn() {
  local mvn_args="$*"
  # WSL 或 Git Bash 下通过 cmd 调用 Windows Maven，避免 JAVA_HOME 不兼容
  if grep -qi microsoft /proc/version 2>/dev/null || [ -n "${MSYSTEM:-}" ] || [ "${OSTYPE:-}" = "msys" ]; then
    local win_java="C:\\Program Files\\Eclipse Adoptium\\jdk-21.0.11.10-hotspot"
    (cd "$POST_DIR" && cmd.exe /c "set JAVA_HOME=$win_java&& mvn -q $mvn_args")
  else
    (cd "$POST_DIR" && mvn -q $mvn_args)
  fi
}

pass() { echo "[通过] $1"; PASS=$((PASS + 1)); }
fail() { echo "[失败] $1"; FAIL=$((FAIL + 1)); }

# 关键检查失败时立即退出
check_fail() {
  if [ "$FAIL" -gt 0 ]; then
    echo "=== 验收中止：关键检查未通过 ==="
    exit 1
  fi
}

echo "=== PostService 一键验收（阶段 5）==="
echo "工作目录：$ROOT"

# 前置：确认在 monorepo 根目录
if [ ! -d "$POST_DIR" ] || [ ! -d "$ROOT/proto" ]; then
  echo "[失败] 请在仓库根目录运行，且需包含 post-service 与 proto 目录"
  exit 1
fi

echo ""
echo "[1] 检查 post-service 目录存在"
if [ -d "$POST_DIR" ]; then
  pass "post-service 目录存在"
else
  fail "post-service 目录不存在"
  check_fail
fi

echo ""
echo "[2] 检查 proto/post/post_service.proto 存在"
if [ -f "$PROTO_FILE" ]; then
  pass "post_service.proto 存在"
else
  fail "post_service.proto 不存在"
  check_fail
fi

echo ""
echo "[3] 检查 proto 中 9 个 RPC 均存在"
RPCS=(
  "CreatePost"
  "GetPostDetail"
  "ListUserPosts"
  "ActionLike"
  "CreateComment"
  "ListComments"
  "DeleteComment"
  "DeletePost"
  "GetRecommendFeed"
)
RPC_MISSING=0
for rpc in "${RPCS[@]}"; do
  if ! grep -q "rpc ${rpc}(" "$PROTO_FILE"; then
    echo "  缺少 RPC：${rpc}"
    RPC_MISSING=1
  fi
done
if [ "$RPC_MISSING" -eq 0 ]; then
  pass "9 个 RPC 均已定义"
else
  fail "proto RPC 不完整"
  check_fail
fi

echo ""
echo "[4] 检查 Flyway V001/V002 存在"
V001="$POST_DIR/src/main/resources/db/migration/V001__create_post_core_tables.sql"
V002="$POST_DIR/src/main/resources/db/migration/V002__create_post_interaction_tables.sql"
if [ -f "$V001" ] && [ -f "$V002" ]; then
  pass "Flyway V001/V002 迁移脚本存在"
else
  fail "Flyway 迁移脚本缺失"
  check_fail
fi

echo ""
echo "[5] 检查 migration 中包含核心表名"
MIGRATION_CONTENT="$(cat "$V001" "$V002" 2>/dev/null || true)"
TABLES=(posts post_images post_stats post_likes post_comments)
TABLE_MISSING=0
for table in "${TABLES[@]}"; do
  if ! echo "$MIGRATION_CONTENT" | grep -q "${table}"; then
    echo "  缺少表名：${table}"
    TABLE_MISSING=1
  fi
done
if [ "$TABLE_MISSING" -eq 0 ]; then
  pass "5 张核心表名均出现在 migration"
else
  fail "migration 表名检查未通过"
  check_fail
fi

echo ""
echo "[6] 检查 PostGrpcService 存在"
GRPC_SERVICE="$POST_DIR/src/main/java/com/dating/post/grpc/PostGrpcService.java"
if [ -f "$GRPC_SERVICE" ]; then
  pass "PostGrpcService 源码存在"
else
  fail "PostGrpcService 缺失"
  check_fail
fi

echo ""
echo "[7] 检查核心 Service 存在"
SERVICES=(
  "PostWriteService"
  "PostReadService"
  "PostLikeService"
  "PostCommentService"
  "FeedService"
)
SVC_MISSING=0
for svc in "${SERVICES[@]}"; do
  if [ ! -f "$POST_DIR/src/main/java/com/dating/post/service/${svc}.java" ]; then
    echo "  缺少 Service：${svc}"
    SVC_MISSING=1
  fi
done
if [ "$SVC_MISSING" -eq 0 ]; then
  pass "5 个核心 Service 均存在"
else
  fail "核心 Service 检查未通过"
  check_fail
fi

echo ""
echo "[8] 检查核心 Job 存在"
JOBS=(
  "LikeFlushJob"
  "CommentFlushJob"
  "FeedScoreJob"
)
JOB_MISSING=0
for job in "${JOBS[@]}"; do
  if [ ! -f "$POST_DIR/src/main/java/com/dating/post/job/${job}.java" ]; then
    echo "  缺少 Job：${job}"
    JOB_MISSING=1
  fi
done
if [ "$JOB_MISSING" -eq 0 ]; then
  pass "3 个核心 Job 均存在"
else
  fail "核心 Job 检查未通过"
  check_fail
fi

echo ""
echo "[9] 检查 Redis key 前缀 yanshuqi"
REDIS_KEYS="$POST_DIR/src/main/java/com/dating/post/constant/PostRedisKeys.java"
if [ -f "$REDIS_KEYS" ] && grep -q "yanshuqi" "$REDIS_KEYS"; then
  pass "PostRedisKeys 使用 yanshuqi 前缀"
else
  fail "Redis key 前缀检查未通过"
  check_fail
fi

echo ""
echo "[10] 检查 post-service 阶段未修改 mobile-gateway"
if command -v git >/dev/null 2>&1 && [ -d "$ROOT/.git" ]; then
  # 只检查 post-service 阶段相关路径的变更集合，避免其他模块既有脏数据误伤验收
  POST_SCOPE=(
    post-service/
    proto/post/
    scripts/verify-post-service.sh
    scripts/verify-post-service.ps1
    README.md
    post-service/README.md
  )
  GW_TOUCHED=0
  for scope in "${POST_SCOPE[@]}"; do
    if git -C "$ROOT" status --porcelain -- "$scope" 2>/dev/null | awk '{print $2}' | grep -q '^mobile-gateway/'; then
      GW_TOUCHED=1
    fi
    if git -C "$ROOT" diff --name-only HEAD -- "$scope" 2>/dev/null | grep -q '^mobile-gateway/'; then
      GW_TOUCHED=1
    fi
  done
  if [ "$GW_TOUCHED" -eq 0 ]; then
    pass "post-service 阶段未修改 mobile-gateway"
  else
    fail "mobile-gateway 出现在 post-service 阶段变更中"
    check_fail
  fi
else
  echo "[跳过] 非 git 仓库或未安装 git，跳过 gateway 变更检查"
fi

echo ""
echo "[11] 执行 post-service mvn test"
if run_mvn test; then
  pass "mvn test 通过"
else
  fail "mvn test 失败"
  check_fail
fi

echo ""
echo "[12] 执行 post-service mvn package -DskipTests"
if run_mvn package -DskipTests; then
  pass "mvn package -DskipTests 通过"
else
  fail "mvn package 失败"
  check_fail
fi

echo ""
echo "=== 验收汇总：通过=${PASS} 失败=${FAIL} ==="
if [ "$FAIL" -gt 0 ]; then
  exit 1
fi
echo "PostService 一键验收全部通过。"
