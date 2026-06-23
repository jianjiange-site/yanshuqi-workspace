#!/usr/bin/env bash
# GW-3 Match 联调验收脚本（需 mobile-gateway :8080 + match-service :9092 已启动）
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"

echo "==> 1. login-device"
LOGIN_RESP=$(curl -s -X POST "${BASE_URL}/api/v1/auth/login-device" \
  -H "Content-Type: application/json" \
  -d '{"deviceId":"gw3-device-001","platform":3,"deviceModel":"Chrome","osVersion":"Windows","appVersion":"0.1.0"}')
ACCESS_TOKEN=$(echo "$LOGIN_RESP" | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')
if [ -z "$ACCESS_TOKEN" ]; then
  echo "登录失败: $LOGIN_RESP"
  exit 1
fi
echo "accessToken 获取成功"

AUTH_HEADER="Authorization: Bearer ${ACCESS_TOKEN}"

echo "==> 2. quota"
curl -s "${BASE_URL}/api/v1/match/quota" -H "${AUTH_HEADER}" | head -c 500
echo

echo "==> 3. feed"
curl -s "${BASE_URL}/api/v1/match/feed?count=5" -H "${AUTH_HEADER}" | head -c 500
echo

echo "==> 4. swipe"
curl -s -X POST "${BASE_URL}/api/v1/match/swipe" \
  -H "Content-Type: application/json" -H "${AUTH_HEADER}" \
  -d '{"targetUserId":10002,"direction":"RIGHT"}' | head -c 500
echo

echo "==> 5. super-hi"
curl -s -X POST "${BASE_URL}/api/v1/match/super-hi" \
  -H "Content-Type: application/json" -H "${AUTH_HEADER}" \
  -d '{"targetUserId":10003,"clientRequestId":"gw3-superhi-001"}' | head -c 500
echo

echo "==> 6. matches"
curl -s "${BASE_URL}/api/v1/match/matches?pageSize=20" -H "${AUTH_HEADER}" | head -c 500
echo

echo "==> 7. visit"
curl -s -X POST "${BASE_URL}/api/v1/match/visit/10002" -H "${AUTH_HEADER}" | head -c 500
echo

echo "==> 8. visits"
curl -s "${BASE_URL}/api/v1/match/visits?pageSize=20" -H "${AUTH_HEADER}" | head -c 500
echo

echo "GW-3 Match smoke 完成"
