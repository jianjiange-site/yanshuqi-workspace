-- Create service schemas in dating_dev_yanshuqi.
-- Usage: psql -h ${POSTGRES_HOST} -p ${POSTGRES_PORT} -U ${POSTGRES_USERNAME} -d dating_dev_yanshuqi -f 01_create_schemas.sql

CREATE SCHEMA IF NOT EXISTS gateway;
CREATE SCHEMA IF NOT EXISTS user_center;
CREATE SCHEMA IF NOT EXISTS match_center;
CREATE SCHEMA IF NOT EXISTS im_center;
CREATE SCHEMA IF NOT EXISTS post_center;
CREATE SCHEMA IF NOT EXISTS payment_center;
CREATE SCHEMA IF NOT EXISTS ai_chat;

COMMENT ON SCHEMA gateway IS 'mobile-gateway schema (yanshuqi)';
COMMENT ON SCHEMA user_center IS 'user-service schema (yanshuqi)';
COMMENT ON SCHEMA match_center IS 'match-service schema (yanshuqi)';
COMMENT ON SCHEMA im_center IS 'im-service schema (yanshuqi)';
COMMENT ON SCHEMA post_center IS 'post-service schema (yanshuqi)';
COMMENT ON SCHEMA payment_center IS 'payment-service schema (yanshuqi)';
COMMENT ON SCHEMA ai_chat IS 'ai-chat schema (yanshuqi)';
