#!/usr/bin/env bash
set -euo pipefail

WORKSPACE_ROOT="$(cd "$(dirname "$0")/.." && pwd)"

services=(
  "mobile-gateway"
  "user-service"
  "match-service"
  "im-service"
  "post-service"
  "payment-service"
  "example-service"
)

echo "=== Build all Java services (independent Maven projects) ==="

for service in "${services[@]}"; do
  service_dir="${WORKSPACE_ROOT}/${service}"
  if [[ ! -d "$service_dir" ]]; then
    echo "Service directory not found: $service_dir" >&2
    exit 1
  fi

  echo ""
  echo ">>> Building ${service} ..."
  (
    cd "$service_dir"
    mvn clean package -DskipTests
  )
  echo ">>> ${service} build succeeded"
done

echo ""
echo "=== All services built successfully ==="
