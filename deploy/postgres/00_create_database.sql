-- Create personal dev database for yanshuqi (run with superuser or authorized role).
-- Usage: psql -h ${POSTGRES_HOST} -p ${POSTGRES_PORT} -U ${POSTGRES_USERNAME} -d postgres -f 00_create_database.sql

CREATE DATABASE dating_dev_yanshuqi
    WITH ENCODING 'UTF8'
         LC_COLLATE 'en_US.utf8'
         LC_CTYPE 'en_US.utf8'
         TEMPLATE template0;

COMMENT ON DATABASE dating_dev_yanshuqi IS 'yanshuqi personal dev database';
