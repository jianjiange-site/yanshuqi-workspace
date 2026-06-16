#!/usr/bin/env python3
"""
USER-05 照片绑定后验证脚本。

用法示例：
  python user-service/scripts/run_user_05_photo_verify.py --user-id 1234567890

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
except ImportError:
    print("FAIL: 请先安装 psycopg2-binary", file=sys.stderr)
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
    parser = argparse.ArgumentParser(description="验证 USER-05 照片绑定结果")
    parser.add_argument("--user-id", type=int, required=True, help="用户 business user_id")
    args = parser.parse_args()

    load_env()
    conn = psycopg2.connect(
        host=os.getenv("POSTGRES_HOST", "127.0.0.1"),
        port=os.getenv("POSTGRES_PORT", "5432"),
        dbname=os.getenv("POSTGRES_DATABASE", "dating_dev_yanshuqi"),
        user=os.getenv("POSTGRES_USERNAME", "postgres"),
        password=os.getenv("POSTGRES_PASSWORD", ""),
        options="-c search_path=user_center",
    )
    try:
        with conn.cursor() as cur:
            cur.execute(
                "SELECT user_id, profile_status FROM users WHERE user_id = %s",
                (args.user_id,),
            )
            user = cur.fetchone()
            cur.execute(
                "SELECT avatar_key FROM user_profiles WHERE user_id = %s",
                (args.user_id,),
            )
            avatar_key = cur.fetchone()
            cur.execute(
                """
                SELECT photo_type, object_key, enabled, sort_order
                FROM user_photos WHERE user_id = %s
                ORDER BY sort_order
                """,
                (args.user_id,),
            )
            photos = cur.fetchall()

            if user is None:
                print("FAIL: users 记录缺失")
                return 1

            print(f"users.profile_status={user[1]}")
            print(f"user_profiles.avatar_key={avatar_key[0] if avatar_key else '(null)'}")
            print(f"user_photos count={len(photos)}")
            for row in photos:
                print(f"  photo: type={row[0]}, key={row[1]}, enabled={row[2]}, sort={row[3]}")

            avatar_rows = [p for p in photos if p[0] == "AVATAR" and p[2] == 1]
            if not avatar_rows:
                print("WARN: 无启用中的 AVATAR 记录")
            elif len(avatar_rows) > 1:
                print("FAIL: 启用中的 AVATAR 超过 1 条")
                return 1

            for _, key, _, _ in photos:
                if key.startswith("http://") or key.startswith("https://"):
                    print("FAIL: object_key 含完整 URL")
                    return 1

            if user[1] == "PHOTO_DONE":
                print("OK: profile_status=PHOTO_DONE")
            else:
                print(f"WARN: profile_status={user[1]}，请确认基础资料是否完整")

            print(f"OK: user_id={args.user_id} 照片绑定验证通过")
            return 0
    finally:
        conn.close()


if __name__ == "__main__":
    sys.exit(main())
