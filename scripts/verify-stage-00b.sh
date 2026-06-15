#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"

echo "=== Stage 00-B Verification ==="
bash "$(dirname "$0")/verify-stage-00a.sh"

required_files=(
  "$ROOT/deploy/postgres/00_create_database.sql"
  "$ROOT/deploy/postgres/01_create_schemas.sql"
  "$ROOT/deploy/postgres/02_check_connection.sql"
  "$ROOT/deploy/nacos/user-service-dev.yaml"
  "$ROOT/deploy/minio/create_buckets.md"
  "$ROOT/deploy/redis/REDIS_KEYS.md"
  "$ROOT/scripts/build-all.sh"
)

for file in "${required_files[@]}"; do
  [[ -f "$file" ]] || { echo "Missing required file: $file"; exit 1; }
  echo "[OK] $file"
done

services=(mobile-gateway user-service match-service im-service post-service payment-service example-service)
for svc in "${services[@]}"; do
  find "$ROOT/$svc" -name InfraHealthController.java | grep -q .
  appDev="$ROOT/$svc/src/main/resources/application-dev.yml"
  bootstrap="$ROOT/$svc/src/main/resources/bootstrap.yml"
  grep -q 'yanshuqi-dev' "$appDev" "$bootstrap"
  grep -q 'dating-yanshuqi' "$ROOT/$svc/src/main/resources/application-dev.yml"
  grep -q 'infra:ping' "$ROOT/$svc/src/main/resources/application-dev.yml"
  echo "[OK] $svc infra config template"
done

grep -q 'yanshuqi_' "$ROOT/im-service/src/main/resources/application-dev.yml"
echo "[OK] im-service OpenIM prefix configured"

[[ -f "$ROOT/ai-chat/app/infra_health.py" ]] || { echo "Missing ai-chat infra_health.py"; exit 1; }
echo "[OK] ai-chat infra health module exists"

echo "=== Stage 00-B structure verification passed ==="
