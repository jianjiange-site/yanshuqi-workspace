#!/usr/bin/env python3
"""
USER-02 注册后四表一致性验证脚本。

用法示例：
  python user-service/scripts/run_user_02_register_verify.py --user-id 1234567890

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
    print("请先安装 psycopg2: pip install psycopg2-binary", file=sys.stderr)
    sys.exit(1)


def get_connection():
    return psycopg2.connect(
        host=os.getenv("POSTGRES_HOST", "127.0.0.1"),
        port=os.getenv("POSTGRES_PORT", "5432"),
        dbname=os.getenv("POSTGRES_DATABASE", "dating_dev_yanshuqi"),
        user=os.getenv("POSTGRES_USERNAME", "postgres"),
        password=os.getenv("POSTGRES_PASSWORD", ""),
        options="-c search_path=user_center",
    )


def fetch_one(cur, sql: str, user_id: int):
    cur.execute(sql, (user_id,))
    return cur.fetchone()


def main() -> int:
    parser = argparse.ArgumentParser(description="验证 USER-02 注册后四表 user_id 一致性")
    parser.add_argument("--user-id", type=int, required=True, help="注册返回的 user_id")
    args = parser.parse_args()

    user_id = args.user_id
    conn = get_connection()
    try:
        with conn.cursor() as cur:
            user = fetch_one(cur, "SELECT user_id, account_status, profile_status, token_version FROM users WHERE user_id = %s", user_id)
            auth = fetch_one(cur, "SELECT user_id, identity_type, identity_hash, password_hash FROM user_auth_identities WHERE user_id = %s", user_id)
            profile = fetch_one(cur, "SELECT user_id, profile_score, profile_completed FROM user_profiles WHERE user_id = %s", user_id)
            settings = fetch_one(cur, "SELECT user_id, discoverable FROM user_settings WHERE user_id = %s", user_id)

            if not all([user, auth, profile, settings]):
                print("FAIL: 四张表中存在缺失记录")
                return 1

            ids = {user[0], auth[0], profile[0], settings[0]}
            if len(ids) != 1 or user_id not in ids:
                print(f"FAIL: user_id 不一致 {ids}")
                return 1

            if user[1] != "ACTIVE" or user[2] != "INIT" or user[3] != 1:
                print(f"FAIL: users 默认值异常 {user}")
                return 1

            if profile[1] != 0 or profile[2] != 0:
                print(f"FAIL: user_profiles 默认值异常 {profile}")
                return 1

            if settings[1] != 1:
                print(f"FAIL: user_settings 默认值异常 {settings}")
                return 1

            if not auth[3] or auth[3].startswith("$2"):
                print("OK: password_hash 已写入且为 BCrypt 格式")
            else:
                print("WARN: password_hash 格式异常，请人工确认")

            print(f"OK: user_id={user_id} 四表一致，注册数据验证通过")
            return 0
    finally:
        conn.close()


if __name__ == "__main__":
    sys.exit(main())
