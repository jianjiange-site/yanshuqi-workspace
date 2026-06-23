#!/usr/bin/env bash
# mobile-gateway GW-6 最终验收脚本（Linux/macOS/Git Bash）
# 从仓库根目录执行：bash scripts/verify-gateway-final.sh
# 不依赖 Postgres/Redis/Nacos 启动，不执行 curl。
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
GW_DIR="$ROOT/mobile-gateway"
DOCS_DIR="$ROOT/docs/mobile-gateway"
PASS=0
FAIL=0

pass() { echo "[PASS] $1"; PASS=$((PASS + 1)); }
fail() { echo "[FAIL] $1"; FAIL=$((FAIL + 1)); }

# 自动 JAVA_HOME（Windows Git Bash 友好）
if [ -z "${JAVA_HOME:-}" ]; then
  if [ -n "${MSYSTEM:-}" ] || [ "${OSTYPE:-}" = "msys" ]; then
    export JAVA_HOME="C:/Program Files/Eclipse Adoptium/jdk-21.0.11.10-hotspot"
  else
    for candidate in \
      "/usr/lib/jvm/java-21-openjdk" \
      "/usr/lib/jvm/java-17-openjdk"; do
      if [ -d "$candidate" ]; then
        export JAVA_HOME="$candidate"
        break
      fi
    done
  fi
fi

echo "=== mobile-gateway Final Verification (GW-6) ==="
echo "Root: $ROOT"

echo "--- 1. mobile-gateway directory ---"
if [ -d "$GW_DIR" ] && [ -f "$GW_DIR/pom.xml" ]; then
  pass "mobile-gateway dir and pom.xml"
else
  fail "mobile-gateway dir or pom.xml missing"
fi

echo "--- 2. key Controllers ---"
CONTROLLERS=(
  "AuthController.java"
  "ProfileController.java"
  "UploadController.java"
  "HomeController.java"
  "MatchController.java"
  "PostController.java"
  "PaymentController.java"
  "ImTokenController.java"
  "OpenImCallbackController.java"
)
CTRL_DIR="$GW_DIR/src/main/java/com/dating/gateway/controller"
missing_ctrl=0
for c in "${CONTROLLERS[@]}"; do
  if [ ! -f "$CTRL_DIR/$c" ]; then
    echo "  missing: $c"
    missing_ctrl=1
  fi
done
if [ "$missing_ctrl" -eq 0 ]; then pass "9 key Controllers"; else fail "Controllers incomplete"; fi

echo "--- 3. key security classes ---"
SEC_CLASSES=(
  "security/JwtAuthFilter.java"
  "security/JwtIssuer.java"
  "security/JwtVerifier.java"
  "resolver/JwtCallerUserResolver.java"
)
missing_sec=0
BASE="$GW_DIR/src/main/java/com/dating/gateway"
for s in "${SEC_CLASSES[@]}"; do
  if [ ! -f "$BASE/$s" ]; then
    echo "  missing: $s"
    missing_sec=1
  fi
done
if [ "$missing_sec" -eq 0 ]; then pass "security/resolver classes"; else fail "security classes missing"; fi

echo "--- 4. GatewayGrpcMetadataSupport ---"
if [ -f "$BASE/support/GatewayGrpcMetadataSupport.java" ]; then
  pass "GatewayGrpcMetadataSupport"
else
  fail "GatewayGrpcMetadataSupport missing"
fi

echo "--- 5. Flyway auth migration ---"
FLYWAY="$GW_DIR/src/main/resources/db/migration/V20260623_001__create_gateway_auth_tables.sql"
if [ -f "$FLYWAY" ] && grep -q "auth_device" "$FLYWAY" && grep -q "auth_refresh_token" "$FLYWAY"; then
  pass "Flyway auth tables script"
else
  fail "Flyway auth script missing or incomplete"
fi

echo "--- 6. docs/mobile-gateway (12 files) ---"
REQUIRED_DOCS=(
  "00-项目地图.md"
  "01-业务流程.md"
  "02-技术架构.md"
  "03-鉴权与安全设计.md"
  "04-接口地图.md"
  "05-REST到gRPC调用链.md"
  "06-数据模型与RedisKey.md"
  "07-跨服务适配说明.md"
  "08-异常码与边界处理.md"
  "09-配置启动与部署说明.md"
  "10-测试与最终验收清单.md"
  "11-阶段复盘.md"
  "12-面试复盘.md"
)
missing_doc=0
for d in "${REQUIRED_DOCS[@]}"; do
  if [ ! -f "$DOCS_DIR/$d" ]; then
    echo "  missing: $d"
    missing_doc=1
  fi
done
if [ "$missing_doc" -eq 0 ]; then pass "docs/mobile-gateway complete (${#REQUIRED_DOCS[@]} files)"; else fail "docs incomplete"; fi

echo "--- 7. root README MobileGateway section ---"
if grep -q "MobileGateway 开发交付文档" "$ROOT/README.md" 2>/dev/null; then
  pass "root README MobileGateway index"
else
  fail "root README missing MobileGateway section"
fi

echo "--- 8. mobile-gateway/README not skeleton-only ---"
GW_README="$GW_DIR/README.md"
if [ -f "$GW_README" ] && grep -q "BFF" "$GW_README" && ! grep -q "Stage 00-A skeleton service" "$GW_README"; then
  pass "mobile-gateway/README updated"
else
  fail "mobile-gateway/README still skeleton or missing BFF description"
fi

echo "--- 9. Payment/IM not ready documented ---"
PAY_DOC="$DOCS_DIR/04-接口地图.md"
if [ -f "$PAY_DOC" ] && grep -q "10701" "$PAY_DOC" && grep -q "mock/test" "$PAY_DOC"; then
  pass "Payment/IM not ready documented"
else
  fail "Payment/IM boundary not in docs"
fi

echo "--- 10. proto user/match/post exist ---"
proto_ok=1
for p in "proto/user/user_auth_service.proto" "proto/match/match_service.proto" "proto/post/post_service.proto"; do
  if [ ! -f "$ROOT/$p" ]; then
    proto_ok=0
    echo "  missing: $p"
  fi
done
if [ "$proto_ok" -eq 1 ]; then pass "user/match/post proto exist"; else fail "proto missing"; fi

echo "--- 11. no obvious hardcoded secrets in mobile-gateway ---"
SECRET_HITS=$(rg -n "BEGIN PRIVATE KEY|AKIA[0-9A-Z]{16}|secretKey\s*=\s*['\"][^'\"]+['\"]" "$GW_DIR/src" 2>/dev/null || true)
if [ -z "$SECRET_HITS" ]; then
  pass "no obvious secrets in src"
else
  echo "$SECRET_HITS"
  fail "possible hardcoded secrets"
fi

echo "--- 12. GatewayErrorCode payment/im codes ---"
ERR="$BASE/exception/GatewayErrorCode.java"
if [ -f "$ERR" ] && grep -q "PAYMENT_SERVICE_NOT_READY" "$ERR" && grep -q "IM_SERVICE_NOT_READY" "$ERR"; then
  pass "GatewayErrorCode 107xx/108xx"
else
  fail "GatewayErrorCode missing payment/im codes"
fi

echo "--- 13. mobile-gateway mvn test ---"
if (cd "$GW_DIR" && mvn -B -ntp -q test); then
  pass "mobile-gateway mvn test"
else
  fail "mobile-gateway mvn test"
fi

echo "--- 14. verify script self exists (ps1) ---"
if [ -f "$ROOT/scripts/verify-gateway-final.ps1" ]; then
  pass "verify-gateway-final.ps1 exists"
else
  fail "verify-gateway-final.ps1 missing"
fi

echo ""
echo "=== Summary: PASS=$PASS FAIL=$FAIL ==="
if [ "$FAIL" -gt 0 ]; then
  exit 1
fi
exit 0
