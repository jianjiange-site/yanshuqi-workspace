#!/usr/bin/env python3
"""
USER-04 资料更新后验证脚本。

用法示例：
  python user-service/scripts/run_user_04_profile_verify.py --user-id 1234567890

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
    parser = argparse.ArgumentParser(description="验证 USER-04 资料更新结果")
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
                """
                SELECT nickname, profile_score, profile_completed, avatar_key
                FROM user_profiles WHERE user_id = %s
                """,
                (args.user_id,),
            )
            profile = cur.fetchone()
            cur.execute(
                "SELECT COUNT(*) FROM user_photos WHERE user_id = %s",
                (args.user_id,),
            )
            photo_count = cur.fetchone()[0]

            if user is None or profile is None:
                print("FAIL: users 或 user_profiles 记录缺失")
                return 1

            nickname, score, completed, avatar_key = profile
            profile_status = user[1]
            print(f"users.profile_status={profile_status}")
            print(f"profile: nickname={nickname}, score={score}, completed={completed}, avatar_key={avatar_key or '(null)'}")
            print(f"user_photos count={photo_count}")

            if score is None:
                print("FAIL: profile_score 为空")
                return 1
            if completed is None:
                print("FAIL: profile_completed 为空")
                return 1
            if photo_count != 0:
                print("WARN: user_photos 存在记录，请确认非本阶段写入")

            print(f"OK: user_id={args.user_id} 资料验证通过")
            return 0
    finally:
        conn.close()


if __name__ == "__main__":
    sys.exit(main())
