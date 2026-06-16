#!/usr/bin/env python3
"""
USER-07 工程质量验证脚本（非破坏性）。

用法：
  python user-service/scripts/run_user_07_quality_verify.py --user-id 325259949544443904
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

SENSITIVE_BLACKLIST = [
    "password_hash",
    "identity_hash",
    "identity_value",
    "device_fingerprint",
    "push_token",
    "presigned",
    "minio",
]


def load_env() -> None:
    env_path = Path(__file__).resolve().parents[2] / "deploy" / ".env"
    if not env_path.exists():
        return
    for line in env_path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        os.environ.setdefault(key.strip(), value.strip())


def grpc_call(method: str, data: dict) -> str:
    result = subprocess.run(
        ["grpcurl", "-plaintext", "-d", json.dumps(data), "localhost:9091",
         f"com.dating.user.profile.v1.UserProfileService/{method}"],
        capture_output=True,
        text=True,
        encoding="utf-8",
    )
    return (result.stdout or "") + (result.stderr or "")


def main() -> int:
    parser = argparse.ArgumentParser(description="USER-07 工程质量验证")
    parser.add_argument("--user-id", type=int, required=True)
    args = parser.parse_args()
    load_env()
    user_id = args.user_id

    # 1. HTTP 健康检查
    try:
        with urllib.request.urlopen("http://localhost:8081/actuator/health", timeout=5) as resp:
            health = json.loads(resp.read().decode("utf-8"))
        if health.get("status") != "UP":
            print("FAIL: HTTP health 非 UP")
            return 1
        print("OK: HTTP health UP")
    except Exception as exc:
        print(f"FAIL: HTTP health 不可达: {exc}")
        return 1

    # 2. gRPC 冒烟
    basic_resp = grpc_call("BatchGetBasicProfiles", {"user_ids": [user_id], "include_unavailable": True})
    if "profiles" not in basic_resp and "ERROR" in basic_resp:
        print(f"FAIL: BatchGetBasicProfiles 失败\n{basic_resp}")
        return 1
    print("OK: BatchGetBasicProfiles 可调用")

    # 3. 响应敏感字段检查
    lower = basic_resp.lower()
    for word in SENSITIVE_BLACKLIST:
        if word in lower:
            print(f"FAIL: 响应含敏感关键词 {word}")
            return 1
    if "http://" in lower or "https://" in lower:
        print("FAIL: 响应含完整 URL")
        return 1
    print("OK: 响应无敏感字段/完整 URL")

    # 4. Redis TTL 检查
    r = redis.Redis(
        host=os.getenv("REDIS_HOST", "127.0.0.1"),
        port=int(os.getenv("REDIS_PORT", "6379")),
        password=os.getenv("REDIS_PASSWORD") or None,
        db=int(os.getenv("REDIS_DATABASE", "0")),
        decode_responses=True,
    )
    keys = {
        "basic": f"yanshuqi:user:basic:{user_id}",
        "profile": f"yanshuqi:user:profile:{user_id}",
        "status": f"yanshuqi:user:status:{user_id}",
    }
    for name, key in keys.items():
        ttl = r.ttl(key)
        print(f"INFO: redis {name} key exists={bool(r.exists(key))} ttl={ttl}")
        if r.exists(key) and ttl <= 0:
            print(f"FAIL: {key} 无 TTL")
            return 1
    print("OK: Redis key TTL 检查完成（若 key 不存在请先调用 gRPC 写缓存）")

    print(f"OK: USER-07 工程质量验证通过, user_id={user_id}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
