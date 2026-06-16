#!/usr/bin/env python3
"""
USER-06 批量资料查询验证脚本。

用法示例：
  python user-service/scripts/run_user_06_batch_profile_verify.py --user-id 1234567890

依赖环境变量（与 deploy/.env 一致）：
  POSTGRES_HOST, POSTGRES_PORT, POSTGRES_DATABASE, POSTGRES_USERNAME, POSTGRES_PASSWORD
"""

from __future__ import annotations

import argparse
import os
import sys
from pathlib import Path

try:
    import psycopg2
    import redis
except ImportError:
    print("FAIL: 请先安装 psycopg2-binary 和 redis", file=sys.stderr)
    sys.exit(1)


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


def main() -> int:
    parser = argparse.ArgumentParser(description="验证 USER-06 批量查询缓存与数据")
    parser.add_argument("--user-id", type=int, required=True, help="用户 business user_id")
    args = parser.parse_args()

    load_env()
    user_id = args.user_id
    conn = psycopg2.connect(
        host=os.getenv("POSTGRES_HOST", "127.0.0.1"),
        port=os.getenv("POSTGRES_PORT", "5432"),
        dbname=os.getenv("POSTGRES_DATABASE", "dating_dev_yanshuqi"),
        user=os.getenv("POSTGRES_USERNAME", "postgres"),
        password=os.getenv("POSTGRES_PASSWORD", ""),
        options="-c search_path=user_center",
    )
    r = redis.Redis(
        host=os.getenv("REDIS_HOST", "127.0.0.1"),
        port=int(os.getenv("REDIS_PORT", "6379")),
        password=os.getenv("REDIS_PASSWORD") or None,
        db=int(os.getenv("REDIS_DATABASE", "0")),
        decode_responses=True,
    )
    try:
        with conn.cursor() as cur:
            cur.execute("SELECT account_status, profile_status FROM users WHERE user_id = %s", (user_id,))
            user = cur.fetchone()
            cur.execute(
                """
                SELECT photo_type, object_key, review_status, enabled
                FROM user_photos WHERE user_id = %s AND photo_type = 'AVATAR'
                """,
                (user_id,),
            )
            avatars = cur.fetchall()

        print(f"users: account_status={user[0] if user else None}, profile_status={user[1] if user else None}")
        print(f"avatar rows: {avatars}")

        basic_key = f"yanshuqi:user:basic:{user_id}"
        profile_key = f"yanshuqi:user:profile:{user_id}"
        status_key = f"yanshuqi:user:status:{user_id}"
        ttl_basic = r.ttl(basic_key)
        ttl_profile = r.ttl(profile_key)
        ttl_status = r.ttl(status_key)
        print(f"redis basic exists={r.exists(basic_key)} ttl={ttl_basic}")
        print(f"redis profile exists={r.exists(profile_key)} ttl={ttl_profile}")
        print(f"redis status exists={r.exists(status_key)} ttl={ttl_status}")

        for key in (basic_key, profile_key, status_key):
            val = r.get(key) or ""
            if "http://" in val.lower() or "https://" in val.lower():
                print("FAIL: 缓存含完整 URL")
                return 1

        if user is None:
            print("FAIL: users 记录缺失")
            return 1
        if ttl_basic <= 0 and r.exists(basic_key):
            print("WARN: basic key 无 TTL")
        print(f"OK: user_id={user_id} 批量查询验证通过（请先调用 BatchGet* gRPC 写入缓存）")
        return 0
    finally:
        conn.close()


if __name__ == "__main__":
    sys.exit(main())
