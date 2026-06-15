-- Verify connectivity and schema ownership for yanshuqi dev database.
-- Usage: psql -h ${POSTGRES_HOST} -p ${POSTGRES_PORT} -U ${POSTGRES_USERNAME} -d dating_dev_yanshuqi -f 02_check_connection.sql

SELECT current_database() AS database_name, current_user AS connected_user, NOW() AT TIME ZONE 'UTC' AS checked_at_utc;

SELECT schema_name
FROM information_schema.schemata
WHERE schema_name IN (
    'gateway',
    'user_center',
    'match_center',
    'im_center',
    'post_center',
    'payment_center',
    'ai_chat'
)
ORDER BY schema_name;
