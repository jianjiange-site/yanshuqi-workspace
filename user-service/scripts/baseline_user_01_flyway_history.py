#!/usr/bin/env python3
"""将已手动落库的 USER-01 migration 标记写入 flyway_history_user（不重复执行 DDL）。"""

import os
import sys
import zlib
from datetime import datetime, timezone
from pathlib import Path

import psycopg2

MIGRATION_DIR = Path(__file__).resolve().parents[1] / "src/main/resources/db/migration"

ENTRIES = (
    (2, "20260616.001", "create user core tables", "V20260616_001__create_user_core_tables.sql"),
    (3, "20260616.002", "create user indexes", "V20260616_002__create_user_indexes.sql"),
)


def flyway_checksum(path: Path) -> int:
    content = path.read_text(encoding="utf-8")
    content = content.replace("\r\n", "\n").replace("\r", "\n")
    if content.startswith("\ufeff"):
        content = content[1:]
    value = zlib.crc32(content.encode("utf-8")) & 0xFFFFFFFF
    return value if value < 2**31 else value - 2**32


def main() -> int:
    password = os.environ.get("POSTGRES_PASSWORD") or os.environ.get("PGPASSWORD")
    if not password:
        print("ERROR: POSTGRES_PASSWORD 未设置")
        return 2

    conn = psycopg2.connect(
        host=os.environ["POSTGRES_HOST"],
        port=os.environ["POSTGRES_PORT"],
        user=os.environ["POSTGRES_USERNAME"],
        password=password,
        dbname=os.environ.get("POSTGRES_DATABASE", "dating_dev_yanshuqi"),
    )
    cur = conn.cursor()
    for rank, version, description, script in ENTRIES:
        cur.execute(
            "SELECT 1 FROM user_center.flyway_history_user WHERE version = %s",
            (version,),
        )
        if cur.fetchone():
            print(f"SKIP {version} (already in history)")
            continue
        checksum = flyway_checksum(MIGRATION_DIR / script)
        cur.execute(
            """
            INSERT INTO user_center.flyway_history_user
            (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
            VALUES (%s, %s, %s, 'SQL', %s, %s, 'user01-baseline', %s, 0, true)
            """,
            (rank, version, description, script, checksum, datetime.now(timezone.utc)),
        )
        print(f"INSERT {version} checksum={checksum}")
    conn.commit()
    conn.close()
    print("DONE")
    return 0


if __name__ == "__main__":
    sys.exit(main())
