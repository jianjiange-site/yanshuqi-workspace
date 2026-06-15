#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
echo "=== Stage 00-A Verification ==="

required_dirs=(
  "$ROOT/ai-chat"
  "$ROOT/proto"
  "$ROOT/deploy"
  "$ROOT/scripts"
  "$ROOT/docs"
  "$ROOT/mobile-gateway"
  "$ROOT/user-service"
  "$ROOT/match-service"
  "$ROOT/im-service"
  "$ROOT/post-service"
  "$ROOT/payment-service"
  "$ROOT/example-service"
)

for dir in "${required_dirs[@]}"; do
  [[ -d "$dir" ]] || { echo "Missing directory: $dir"; exit 1; }
  echo "[OK] $dir"
done

if [[ -d "$ROOT/dating-server" ]]; then
  if find "$ROOT/dating-server" -name pom.xml | grep -q .; then
    echo "Legacy directory dating-server/ still contains Maven modules"
    exit 1
  fi
  echo "[WARN] Empty legacy dating-server/ directory remains (likely file lock). Delete manually after closing IDE/terminals."
else
  echo "[OK] dating-server/ not present"
fi

if rg -q "com\.chatvibe|com\.dating\.yanshuqi" "$ROOT/mobile-gateway" "$ROOT/user-service" "$ROOT/match-service" "$ROOT/im-service" "$ROOT/post-service" "$ROOT/payment-service" "$ROOT/example-service" --glob "*.java"; then
  echo "Forbidden Java package found"
  exit 1
fi
echo "[OK] Java package naming check passed"

services=(
  "mobile-gateway:8080"
  "user-service:8081"
  "match-service:8082"
  "im-service:8083"
  "post-service:8084"
  "payment-service:8085"
  "example-service:8086"
)

for item in "${services[@]}"; do
  name="${item%%:*}"
  port="${item##*:}"
  file=$(find "$ROOT/$name" -name HealthController.java | head -n 1)
  [[ -n "$file" ]] || { echo "Missing HealthController for $name"; exit 1; }
  echo "[OK] $name HealthController exists (port $port)"
done

[[ -f "$ROOT/ai-chat/app/main.py" ]] || { echo "Missing ai-chat main.py"; exit 1; }
echo "[OK] ai-chat skeleton exists"

[[ -f "$ROOT/proto/README.md" ]] || { echo "Missing proto README"; exit 1; }
echo "[OK] proto README exists"

echo "=== Stage 00-A structure verification passed ==="
