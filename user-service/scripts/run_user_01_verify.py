#!/usr/bin/env python3
"""USER-01 PostgreSQL 验收脚本（Python 版，等价 verify_user_01_schema.sql）。"""

import os
import sys

try:
    import psycopg2
except ImportError:
    print("ERROR: 请先安装 psycopg2-binary: pip install psycopg2-binary")
    sys.exit(1)

PASSWORD = os.environ.get("POSTGRES_PASSWORD") or os.environ.get("PGPASSWORD")
if not PASSWORD:
    print("ERROR: 未设置 POSTGRES_PASSWORD 或 PGPASSWORD 环境变量")
    sys.exit(2)

CFG = {
    "host": os.environ.get("POSTGRES_HOST", "38.76.188.242"),
    "port": os.environ.get("POSTGRES_PORT", "5433"),
    "user": os.environ.get("POSTGRES_USERNAME", "jianjian_test"),
    "dbname": os.environ.get("POSTGRES_DATABASE", "dating_dev_yanshuqi"),
    "password": PASSWORD,
    "connect_timeout": 10,
}

CORE_TABLES = (
    "users",
    "user_auth_identities",
    "user_profiles",
    "user_photos",
    "user_devices",
    "user_settings",
)

UNIQUE_INDEXES = (
    "uk_users_user_id",
    "uk_user_auth_auth_id",
    "uk_user_auth_identity",
    "uk_user_profiles_profile_id",
    "uk_user_profiles_user_id",
    "uk_user_photos_photo_id",
    "uk_user_photos_user_object",
    "uk_user_devices_device_id",
    "uk_user_devices_user_fp",
    "uk_user_settings_setting_id",
    "uk_user_settings_user_id",
)

NORMAL_INDEXES = (
    "idx_users_account_status",
    "idx_users_user_type",
    "idx_users_profile_status",
    "idx_user_profiles_gender",
    "idx_user_profiles_country_city",
    "idx_user_profiles_completed",
    "idx_user_photos_user_type",
    "idx_user_photos_review",
)


def section(title: str) -> None:
    print(f"\n=== {title} ===")


def main() -> int:
    passed = True
    conn = psycopg2.connect(**CFG)
    cur = conn.cursor()

    section("1. 检查 user_center schema")
    cur.execute("SELECT schema_name FROM information_schema.schemata WHERE schema_name = 'user_center'")
    rows = cur.fetchall()
    print(rows)
    if not rows:
        passed = False

    section("2. 检查 6 张核心表是否存在")
    cur.execute(
        """
        SELECT table_name FROM information_schema.tables
        WHERE table_schema = 'user_center'
          AND table_name = ANY(%s)
        ORDER BY table_name
        """,
        (list(CORE_TABLES),),
    )
    tables = [r[0] for r in cur.fetchall()]
    print(tables)
    if len(tables) != 6:
        passed = False

    section("3. 检查 users 核心字段")
    cur.execute(
        """
        SELECT column_name, data_type, udt_name
        FROM information_schema.columns
        WHERE table_schema = 'user_center' AND table_name = 'users'
          AND column_name = ANY(%s)
        ORDER BY column_name
        """,
        (
            [
                "id",
                "user_id",
                "user_type",
                "account_status",
                "profile_status",
                "register_source",
                "token_version",
                "last_login_at",
                "created_at",
                "updated_at",
                "deleted",
            ],
        ),
    )
    user_cols = cur.fetchall()
    for row in user_cols:
        print(row)
    if len(user_cols) != 11:
        passed = False

    section("4. 检查 TIMESTAMPTZ 字段类型")
    cur.execute(
        """
        SELECT table_name, column_name, udt_name
        FROM information_schema.columns
        WHERE table_schema = 'user_center'
          AND table_name = ANY(%s)
          AND column_name = ANY(%s)
        ORDER BY table_name, column_name
        """,
        (
            list(CORE_TABLES),
            ["created_at", "updated_at", "last_login_at", "verified_at", "last_seen_at"],
        ),
    )
    tz_rows = cur.fetchall()
    for row in tz_rows:
        print(row)
        if row[2] != "timestamptz":
            passed = False

    section("5. 检查表 COMMENT")
    cur.execute(
        """
        SELECT c.relname, obj_description(c.oid)
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE n.nspname = 'user_center' AND c.relkind = 'r'
          AND c.relname = ANY(%s)
        ORDER BY c.relname
        """,
        (list(CORE_TABLES),),
    )
    comments = cur.fetchall()
    for row in comments:
        print(row[0], "=>", (row[1] or "")[:40] + ("..." if row[1] and len(row[1]) > 40 else ""))
    if len(comments) != 6 or any(not r[1] for r in comments):
        passed = False

    section("6. 检查字段 COMMENT 缺失（应为 0 行）")
    cur.execute(
        """
        SELECT c.table_name, c.column_name
        FROM information_schema.columns c
        LEFT JOIN pg_catalog.pg_statio_all_tables st
            ON st.schemaname = c.table_schema AND st.relname = c.table_name
        LEFT JOIN pg_catalog.pg_description pgd
            ON pgd.objoid = st.relid AND pgd.objsubid = c.ordinal_position
        WHERE c.table_schema = 'user_center'
          AND c.table_name = ANY(%s)
          AND pgd.description IS NULL
        ORDER BY c.table_name, c.ordinal_position
        """,
        (list(CORE_TABLES),),
    )
    missing = cur.fetchall()
    print(f"缺失行数: {len(missing)}")
    for row in missing[:20]:
        print(row)
    if missing:
        passed = False

    section("7. 检查唯一索引")
    cur.execute(
        """
        SELECT indexname FROM pg_indexes
        WHERE schemaname = 'user_center' AND indexname = ANY(%s)
        ORDER BY indexname
        """,
        (list(UNIQUE_INDEXES),),
    )
    uk = [r[0] for r in cur.fetchall()]
    print(uk)
    if len(uk) != len(UNIQUE_INDEXES):
        passed = False

    section("8. 检查普通索引")
    cur.execute(
        """
        SELECT indexname FROM pg_indexes
        WHERE schemaname = 'user_center' AND indexname = ANY(%s)
        ORDER BY indexname
        """,
        (list(NORMAL_INDEXES),),
    )
    idx = [r[0] for r in cur.fetchall()]
    print(idx)
    if len(idx) != len(NORMAL_INDEXES):
        passed = False

    section("9. 汇总：表数量应为 6")
    cur.execute(
        """
        SELECT COUNT(*) FROM information_schema.tables
        WHERE table_schema = 'user_center' AND table_name = ANY(%s)
        """,
        (list(CORE_TABLES),),
    )
    count = cur.fetchone()[0]
    print("core_table_count =", count)
    if count != 6:
        passed = False

    section("10. 检查 Flyway 历史（user_center.flyway_history_user）")
    cur.execute(
        """
        SELECT EXISTS (
            SELECT 1 FROM information_schema.tables
            WHERE table_schema = 'user_center' AND table_name = 'flyway_history_user'
        )
        """
    )
    has_flyway = cur.fetchone()[0]
    print("flyway_history_user exists:", has_flyway)
    if has_flyway:
        cur.execute(
            """
            SELECT installed_rank, version, description, success
            FROM user_center.flyway_history_user
            ORDER BY installed_rank
            """
        )
        for row in cur.fetchall():
            print(row)

    conn.close()
    section("USER-01 验收结果")
    print("PASS" if passed else "FAIL")
    return 0 if passed else 1


if __name__ == "__main__":
    try:
        sys.exit(main())
    except psycopg2.Error as exc:
        print(f"ERROR: 数据库连接或查询失败: {exc}")
        sys.exit(3)
