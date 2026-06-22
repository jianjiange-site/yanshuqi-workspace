#!/usr/bin/env bash
# MatchService 阶段 9 最终验收脚本
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
MATCH_DIR="$ROOT/match-service"
GATEWAY_DIR="$ROOT/mobile-gateway"
DOCS_DIR="$ROOT/docs/match-service"
PASS=0
FAIL=0

pass() { echo "[PASS] $1"; PASS=$((PASS + 1)); }
fail() { echo "[FAIL] $1"; FAIL=$((FAIL + 1)); }
warn() { echo "[WARN] $1"; }

echo "=== MatchService Final Verification (Stage 9) ==="

echo "--- 1. match-service mvn clean test ---"
if (cd "$MATCH_DIR" && mvn -q clean test); then
  pass "match-service tests"
else
  fail "match-service tests"
fi

echo "--- 2. match-service mvn clean compile ---"
if (cd "$MATCH_DIR" && mvn -q clean compile); then
  pass "match-service compile"
else
  fail "match-service compile"
fi

echo "--- 3. mobile-gateway mvn clean test ---"
if (cd "$GATEWAY_DIR" && mvn -q clean test); then
  pass "mobile-gateway tests"
else
  fail "mobile-gateway tests"
fi

echo "--- 4. mobile-gateway mvn clean compile ---"
if (cd "$GATEWAY_DIR" && mvn -q clean compile); then
  pass "mobile-gateway compile"
else
  fail "mobile-gateway compile"
fi

echo "--- 5. proto/match/match_service.proto exists ---"
if [ -f "$ROOT/proto/match/match_service.proto" ]; then
  pass "match proto exists"
else
  fail "match proto missing"
fi

echo "--- 6. no forbidden cross-service imports ---"
BAD_IMPORTS=$(rg -n "import com\.dating\.(user|payment|im)\." "$MATCH_DIR/src/main/java" \
  | rg -v "grpc\.proto" || true)
if [ -z "$BAD_IMPORTS" ]; then
  pass "no forbidden cross-service imports"
else
  echo "$BAD_IMPORTS"
  fail "forbidden cross-service imports"
fi

echo "--- 7. gateway has no Match business logic keywords ---"
if [ -d "$GATEWAY_DIR/src/main/java" ] && rg -q "QuotaService|SwipeHistoryManager|MatchCreationService|D1Generator" "$GATEWAY_DIR/src/main/java" 2>/dev/null; then
  fail "gateway contains Match business logic keywords"
else
  pass "gateway has no Match business logic keywords"
fi

echo "--- 8. Redis key prefix yanshuqi ---"
REDIS_CONST="$MATCH_DIR/src/main/java/com/dating/match/constant/RedisKeyConstants.java"
if grep -q "yanshuqi:match:" "$REDIS_CONST" && grep -q "yanshuqi:lock:match:" "$REDIS_CONST"; then
  pass "Redis key prefix yanshuqi"
else
  fail "Redis key prefix check"
fi

echo "--- 9. docs/match-service/ files complete ---"
REQUIRED_DOCS=(
  "00-项目地图.md"
  "01-业务流程.md"
  "02-技术架构.md"
  "03-数据模型.md"
  "04-接口地图.md"
  "05-调用链.md"
  "06-技术决策.md"
  "07-问题与方案.md"
  "08-验收清单.md"
  "09-阶段复盘.md"
  "10-面试复盘.md"
)
MISSING=0
for doc in "${REQUIRED_DOCS[@]}"; do
  if [ ! -f "$DOCS_DIR/$doc" ]; then
    echo "  missing: $doc"
    MISSING=1
  fi
done
if [ "$MISSING" -eq 0 ]; then
  pass "match-service docs complete (${#REQUIRED_DOCS[@]} files)"
else
  fail "match-service docs incomplete"
fi

echo "--- 10. scan suspected secrets in match-service & gateway sources ---"
SECRET_HITS=$(rg -n -i "(password=|token=|secret=|\bak=|\bsk=)" \
  "$MATCH_DIR/src" "$GATEWAY_DIR/src" \
  --glob '!**/application*.yml' --glob '!**/.env*' 2>/dev/null || true)
if [ -z "$SECRET_HITS" ]; then
  pass "no suspected hardcoded secrets in sources"
else
  echo "$SECRET_HITS"
  fail "suspected secrets found in sources"
fi

warn "PostgreSQL Testcontainers 集成测试需 Docker 环境补跑（MatchManagerIntegrationTest）"

echo "=== Summary: PASS=$PASS FAIL=$FAIL ==="
if [ "$FAIL" -gt 0 ]; then
  exit 1
fi
