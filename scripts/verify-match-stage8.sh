#!/usr/bin/env bash
# MatchService 阶段 8 联调前验收脚本
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
MATCH_DIR="$ROOT/match-service"
GATEWAY_DIR="$ROOT/mobile-gateway"
PASS=0
FAIL=0

pass() { echo "[PASS] $1"; PASS=$((PASS + 1)); }
fail() { echo "[FAIL] $1"; FAIL=$((FAIL + 1)); }

echo "=== MatchService Stage 8 Verification ==="

echo "--- 1. match-service compile ---"
if (cd "$MATCH_DIR" && mvn -q clean compile); then
  pass "match-service compile"
else
  fail "match-service compile"
fi

echo "--- 2. match-service tests ---"
if (cd "$MATCH_DIR" && mvn -q clean test); then
  pass "match-service tests"
else
  fail "match-service tests"
fi

echo "--- 3. default client-mode is mock ---"
TEST_YML="$MATCH_DIR/src/test/resources/application-test.yml"
DEV_YML="$MATCH_DIR/src/main/resources/application-dev.yml"
if grep -q "user-client-mode: mock" "$TEST_YML" && grep -q "user-client-mode: mock" "$DEV_YML"; then
  pass "default client-mode mock in dev/test"
else
  fail "default client-mode mock in dev/test"
fi

echo "--- 4. Redis key prefix yanshuqi ---"
REDIS_CONST="$MATCH_DIR/src/main/java/com/dating/match/constant/RedisKeyConstants.java"
if grep -q 'yanshuqi:match:' "$REDIS_CONST" && grep -q 'yanshuqi:lock:match:' "$REDIS_CONST"; then
  pass "Redis key prefix contains yanshuqi"
else
  fail "Redis key prefix contains yanshuqi"
fi

echo "--- 5. no cross-service Java imports (allow proto) ---"
BAD_IMPORTS=$(rg -n "import com\.dating\.(user|payment|im)\." "$MATCH_DIR/src/main/java" \
  | rg -v "grpc\.proto" || true)
if [ -z "$BAD_IMPORTS" ]; then
  pass "no forbidden cross-service imports"
else
  echo "$BAD_IMPORTS"
  fail "forbidden cross-service imports found"
fi

echo "--- 6. gateway has no Match business logic keywords ---"
GW_SRC="$GATEWAY_DIR/src/main/java"
if [ -d "$GW_SRC" ] && rg -q "QuotaService|SwipeHistoryManager|MatchCreationService" "$GW_SRC" 2>/dev/null; then
  fail "gateway contains Match business logic keywords"
else
  pass "gateway has no Match business logic keywords"
fi

echo "=== Summary: PASS=$PASS FAIL=$FAIL ==="
if [ "$FAIL" -gt 0 ]; then
  exit 1
fi
