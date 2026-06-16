#!/usr/bin/env python3
"""
USER-08 最终非破坏性验收脚本。

用法：
  python user-service/scripts/run_user_08_final_verify.py --user-id 325259949544443904
"""

from __future__ import annotations

import argparse
import json
import os
import subprocess
import sys
import urllib.request
from pathlib import Path

try:
    import redis
except ImportError:
    print("FAIL: 请先安装 redis", file=sys.stderr)
    sys.exit(1)

PROFILE = "com.dating.user.profile.v1.UserProfileService"
GRPC = "localhost:9091"
BLACKLIST = [
    "password", "password_hash", "identity_hash", "identity_value",
    "device_fingerprint", "push_token", "presigned", "minio", "http://", "https://",
]


def load_env() -> None:
    p = Path(__file__).resolve().parents[2] / "deploy" / ".env"
    if not p.exists():
        return
    for line in p.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        k, v = line.split("=", 1)
        os.environ.setdefault(k.strip(), v.strip())


def grpc(method: str, data: dict) -> str:
    r = subprocess.run(
        ["grpcurl", "-plaintext", "-d", json.dumps(data), GRPC, f"{PROFILE}/{method}"],
        capture_output=True, text=True, encoding="utf-8",
    )
    return (r.stdout or "") + (r.stderr or "")


def main() -> int:
    parser = argparse.ArgumentParser(description="USER-08 最终验收")
    parser.add_argument("--user-id", type=int, required=True)
    args = parser.parse_args()
    load_env()
    uid = args.user_id

    # 1. HTTP health
    try:
        with urllib.request.urlopen("http://localhost:8081/actuator/health", timeout=8) as resp:
            health = json.loads(resp.read().decode("utf-8"))
        if health.get("status") != "UP":
            print("FAIL: HTTP health 非 UP")
            return 1
        print("PASS: HTTP health UP")
    except Exception as exc:
        print(f"FAIL: HTTP 不可达: {exc}")
        return 1

    # 2. BatchGetBasicProfiles
    basic = grpc("BatchGetBasicProfiles", {"user_ids": [uid], "include_unavailable": True})
    if "ERROR" in basic and "profiles" not in basic:
        print(f"FAIL: BatchGetBasicProfiles\n{basic}")
        return 1
    print("PASS: BatchGetBasicProfiles 可调用")

    # 3. CheckUserAvailable
    check = grpc("CheckUserAvailable", {"user_ids": [uid]})
    if "ERROR" in check and "results" not in check:
        print(f"FAIL: CheckUserAvailable\n{check}")
        return 1
    print("PASS: CheckUserAvailable 可调用")

    # 4. 敏感字段
    merged = (basic + check).lower()
    for word in BLACKLIST:
        if word in merged:
            print(f"FAIL: 响应含敏感关键词 {word}")
            return 1
    print("PASS: 响应无敏感字段/完整 URL")

    # 5. Redis basic TTL
    r = redis.Redis(
        host=os.getenv("REDIS_HOST", "127.0.0.1"),
        port=int(os.getenv("REDIS_PORT", "6379")),
        password=os.getenv("REDIS_PASSWORD") or None,
        db=int(os.getenv("REDIS_DATABASE", "0")),
        decode_responses=True,
    )
    key = f"yanshuqi:user:basic:{uid}"
    exists = bool(r.exists(key))
    ttl = r.ttl(key)
    print(f"INFO: redis basic exists={exists} ttl={ttl}")
    if exists and ttl <= 0:
        print("FAIL: basic key 无 TTL")
        return 1
    if not exists:
        print("WARN: basic key 不存在（可先调用 BatchGetBasicProfiles 写缓存）")
    else:
        print("PASS: Redis basic key TTL 正常")

    print(f"PASS: USER-08 最终验收通过, user_id={uid}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
