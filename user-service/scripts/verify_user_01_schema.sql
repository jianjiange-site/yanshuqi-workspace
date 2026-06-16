-- USER-01 表结构验收脚本
-- 用法：psql -h ${POSTGRES_HOST} -p ${POSTGRES_PORT} -U ${POSTGRES_USERNAME} -d dating_dev_yanshuqi -f verify_user_01_schema.sql

\set ON_ERROR_STOP on

\echo '=== 1. 检查 user_center schema ==='
SELECT schema_name
FROM information_schema.schemata
WHERE schema_name = 'user_center';

\echo '=== 2. 检查 6 张核心表是否存在 ==='
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'user_center'
  AND table_name IN (
      'users',
      'user_auth_identities',
      'user_profiles',
      'user_photos',
      'user_devices',
      'user_settings'
  )
ORDER BY table_name;

\echo '=== 3. 检查核心字段是否存在（users） ==='
SELECT column_name, data_type, udt_name
FROM information_schema.columns
WHERE table_schema = 'user_center'
  AND table_name = 'users'
  AND column_name IN (
      'id', 'user_id', 'user_type', 'account_status', 'profile_status',
      'register_source', 'token_version', 'last_login_at',
      'created_at', 'updated_at', 'deleted'
  )
ORDER BY column_name;

\echo '=== 4. 检查 TIMESTAMPTZ 字段类型 ==='
SELECT table_name, column_name, udt_name
FROM information_schema.columns
WHERE table_schema = 'user_center'
  AND table_name IN (
      'users', 'user_auth_identities', 'user_profiles',
      'user_photos', 'user_devices', 'user_settings'
  )
  AND column_name IN ('created_at', 'updated_at', 'last_login_at', 'verified_at', 'last_seen_at')
ORDER BY table_name, column_name;

\echo '=== 5. 检查表 COMMENT ==='
SELECT c.relname AS table_name, obj_description(c.oid) AS table_comment
FROM pg_class c
JOIN pg_namespace n ON n.oid = c.relnamespace
WHERE n.nspname = 'user_center'
  AND c.relkind = 'r'
  AND c.relname IN (
      'users', 'user_auth_identities', 'user_profiles',
      'user_photos', 'user_devices', 'user_settings'
  )
ORDER BY c.relname;

\echo '=== 6. 检查字段 COMMENT 缺失（应为 0 行） ==='
SELECT c.table_name, c.column_name
FROM information_schema.columns c
LEFT JOIN pg_catalog.pg_statio_all_tables st
    ON st.schemaname = c.table_schema AND st.relname = c.table_name
LEFT JOIN pg_catalog.pg_description pgd
    ON pgd.objoid = st.relid AND pgd.objsubid = c.ordinal_position
WHERE c.table_schema = 'user_center'
  AND c.table_name IN (
      'users', 'user_auth_identities', 'user_profiles',
      'user_photos', 'user_devices', 'user_settings'
  )
  AND pgd.description IS NULL
ORDER BY c.table_name, c.ordinal_position;

\echo '=== 7. 检查唯一索引 ==='
SELECT indexname, indexdef
FROM pg_indexes
WHERE schemaname = 'user_center'
  AND indexname IN (
      'uk_users_user_id',
      'uk_user_auth_auth_id',
      'uk_user_auth_identity',
      'uk_user_profiles_profile_id',
      'uk_user_profiles_user_id',
      'uk_user_photos_photo_id',
      'uk_user_photos_user_object',
      'uk_user_devices_device_id',
      'uk_user_devices_user_fp',
      'uk_user_settings_setting_id',
      'uk_user_settings_user_id'
  )
ORDER BY indexname;

\echo '=== 8. 检查普通索引 ==='
SELECT indexname, indexdef
FROM pg_indexes
WHERE schemaname = 'user_center'
  AND indexname IN (
      'idx_users_account_status',
      'idx_users_user_type',
      'idx_users_profile_status',
      'idx_user_profiles_gender',
      'idx_user_profiles_country_city',
      'idx_user_profiles_completed',
      'idx_user_photos_user_type',
      'idx_user_photos_review'
  )
ORDER BY indexname;

\echo '=== 9. 汇总：表数量应为 6 ==='
SELECT COUNT(*) AS core_table_count
FROM information_schema.tables
WHERE table_schema = 'user_center'
  AND table_name IN (
      'users', 'user_auth_identities', 'user_profiles',
      'user_photos', 'user_devices', 'user_settings'
  );

\echo '=== USER-01 验收脚本执行完毕 ==='
