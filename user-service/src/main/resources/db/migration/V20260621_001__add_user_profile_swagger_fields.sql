-- USER-09-2：扩展 user_profiles 以支撑 Swagger Profile / Onboarding 字段

ALTER TABLE user_center.user_profiles
    ADD COLUMN IF NOT EXISTS age                INTEGER      NULL,
    ADD COLUMN IF NOT EXISTS height             INTEGER      NULL,
    ADD COLUMN IF NOT EXISTS occupation         VARCHAR(128) NULL,
    ADD COLUMN IF NOT EXISTS education          VARCHAR(128) NULL,
    ADD COLUMN IF NOT EXISTS location           VARCHAR(256) NULL,
    ADD COLUMN IF NOT EXISTS regulation_status  INTEGER      NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS last_open_at       TIMESTAMPTZ  NULL;

COMMENT ON COLUMN user_center.user_profiles.age IS '年龄，birthday 缺失时可落库；有 birthday 时返回优先推导';
COMMENT ON COLUMN user_center.user_profiles.height IS '身高（厘米）';
COMMENT ON COLUMN user_center.user_profiles.occupation IS '职业';
COMMENT ON COLUMN user_center.user_profiles.education IS '学历';
COMMENT ON COLUMN user_center.user_profiles.location IS '展示用地区/位置文本，与 city_code 语义不同';
COMMENT ON COLUMN user_center.user_profiles.regulation_status IS '合规/审核状态，0=默认正常';
COMMENT ON COLUMN user_center.user_profiles.last_open_at IS '最近打开 App 时间，UTC；缺失时由 last_login_at 近似';
