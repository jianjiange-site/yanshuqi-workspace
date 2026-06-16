#!/usr/bin/env python3
"""
USER-03 登录后三表一致性验证脚本。

用法示例：
  python user-service/scripts/run_user_03_login_verify.py --user-id 1234567890 --device-fingerprint device-fp-test-001

依赖环境变量（与 deploy/.env 一致）：
  POSTGRES_HOST, POSTGRES_PORT, POSTGRES_DATABASE, POSTGRES_USERNAME, POSTGRES_PASSWORD
"""

from __future__ import annotations

import argparse
import os
import sys

try:
    import psycopg2
except ImportError:
    print("FAIL: 请先安装 psycopg2-binary", file=sys.stderr)
    sys.exit(1)


def load_env() -> None:
    env_path = os.path.join(os.path.dirname(__file__), "..", "..", "deploy", ".env")
    if not os.path.exists(env_path):
        return
    with open(env_path, encoding="utf-8") as handle:
        for line in handle:
            line = line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            key, value = line.split("=", 1)
            os.environ.setdefault(key.strip(), value.strip())


def get_connection():
    return psycopg2.connect(
        host=os.getenv("POSTGRES_HOST", "127.0.0.1"),
        port=os.getenv("POSTGRES_PORT", "5432"),
        dbname=os.getenv("POSTGRES_DATABASE", "dating_dev_yanshuqi"),
        user=os.getenv("POSTGRES_USERNAME", "postgres"),
        password=os.getenv("POSTGRES_PASSWORD", ""),
        options="-c search_path=user_center",
    )


def main() -> int:
    parser = argparse.ArgumentParser(description="验证 USER-03 登录后 users/auth/devices 数据")
    parser.add_argument("--user-id", type=int, required=True, help="登录返回的 user_id")
    parser.add_argument("--device-fingerprint", required=True, help="登录使用的 device_fingerprint")
    args = parser.parse_args()

    load_env()
    conn = get_connection()
    try:
        with conn.cursor() as cur:
            cur.execute(
                "SELECT user_id, last_login_at FROM users WHERE user_id = %s",
                (args.user_id,),
            )
            user = cur.fetchone()
            cur.execute(
                "SELECT user_id, last_login_at FROM user_auth_identities WHERE user_id = %s",
                (args.user_id,),
            )
            auth = cur.fetchone()
            cur.execute(
                """
                SELECT COUNT(*), MAX(last_seen_at)
                FROM user_devices
                WHERE user_id = %s AND device_fingerprint = %s
                """,
                (args.user_id, args.device_fingerprint),
            )
            device_count, last_seen_at = cur.fetchone()

            if user is None or auth is None:
                print("FAIL: users 或 user_auth_identities 记录缺失")
                return 1
            if user[1] is None:
                print("FAIL: users.last_login_at 为空")
                return 1
            if auth[1] is None:
                print("FAIL: user_auth_identities.last_login_at 为空")
                return 1
            if device_count != 1:
                print(f"FAIL: 同设备记录数={device_count}，期望 1")
                return 1
            if last_seen_at is None:
                print("FAIL: user_devices.last_seen_at 为空")
                return 1

            print(f"OK: user_id={args.user_id} 登录数据验证通过")
            print(f"OK: users.last_login_at={user[1]}")
            print(f"OK: auth.last_login_at={auth[1]}")
            print(f"OK: device count=1, last_seen_at={last_seen_at}")
            return 0
    finally:
        conn.close()


if __name__ == "__main__":
    sys.exit(main())
