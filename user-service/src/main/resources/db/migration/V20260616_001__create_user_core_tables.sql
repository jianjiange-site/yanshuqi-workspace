-- USER-01：创建 user_center schema 及 6 张用户核心表

CREATE SCHEMA IF NOT EXISTS user_center;

COMMENT ON SCHEMA user_center IS 'user-service 用户域 schema（yanshuqi）';

-- A. users 用户主表
CREATE TABLE user_center.users (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    user_type       VARCHAR(16)  NOT NULL,
    account_status  VARCHAR(32)  NOT NULL,
    profile_status  VARCHAR(32)  NOT NULL,
    register_source VARCHAR(32)  NOT NULL,
    token_version   INTEGER      NOT NULL DEFAULT 1,
    last_login_at   TIMESTAMPTZ  NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted         INTEGER      NOT NULL DEFAULT 0,
    CONSTRAINT uk_users_user_id UNIQUE (user_id)
);

COMMENT ON TABLE user_center.users IS '用户主表：保存用户内部业务主键、用户类型、账号状态、资料状态等全局信息';
COMMENT ON COLUMN user_center.users.id IS '数据库自增主键，仅用于物理存储，不对外暴露';
COMMENT ON COLUMN user_center.users.user_id IS '用户业务主键，跨服务引用和接口返回都使用该字段';
COMMENT ON COLUMN user_center.users.user_type IS '用户类型：BH=真人用户，DH=数字人用户';
COMMENT ON COLUMN user_center.users.account_status IS '账号状态：ACTIVE=正常，DISABLED=停用，BANNED=封禁，DELETED=注销';
COMMENT ON COLUMN user_center.users.profile_status IS '资料状态：INIT=未完善，BASIC_DONE=基础资料完成，PHOTO_DONE=头像完成，COMPLETED=资料完整，BLOCKED=资料被阻断';
COMMENT ON COLUMN user_center.users.register_source IS '注册来源：PHONE、EMAIL、GOOGLE、APPLE、DEVICE、ADMIN';
COMMENT ON COLUMN user_center.users.token_version IS 'Token 版本号，用于用户登出、封禁、强制失效历史 token';
COMMENT ON COLUMN user_center.users.last_login_at IS '最近一次登录时间，统一 UTC';
COMMENT ON COLUMN user_center.users.created_at IS '创建时间，统一 UTC';
COMMENT ON COLUMN user_center.users.updated_at IS '更新时间，统一 UTC';
COMMENT ON COLUMN user_center.users.deleted IS '逻辑删除标记：0=未删除，1=已删除';

-- B. user_auth_identities 用户登录凭证表
CREATE TABLE user_center.user_auth_identities (
    id              BIGSERIAL PRIMARY KEY,
    auth_id         BIGINT       NOT NULL,
    user_id         BIGINT       NOT NULL,
    identity_type   VARCHAR(32)  NOT NULL,
    identity_value  VARCHAR(255) NOT NULL,
    identity_hash   VARCHAR(128) NOT NULL,
    password_hash   VARCHAR(255) NULL,
    verified        INTEGER      NOT NULL DEFAULT 0,
    verified_at     TIMESTAMPTZ  NULL,
    last_login_at   TIMESTAMPTZ  NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted         INTEGER      NOT NULL DEFAULT 0,
    CONSTRAINT uk_user_auth_auth_id UNIQUE (auth_id),
    CONSTRAINT uk_user_auth_identity UNIQUE (identity_type, identity_hash)
);

COMMENT ON TABLE user_center.user_auth_identities IS '用户登录凭证表：保存手机号、邮箱、第三方账号等身份凭证';
COMMENT ON COLUMN user_center.user_auth_identities.id IS '数据库自增主键，仅用于物理存储，不对外暴露';
COMMENT ON COLUMN user_center.user_auth_identities.auth_id IS '登录凭证业务主键';
COMMENT ON COLUMN user_center.user_auth_identities.user_id IS '用户业务主键，关联 users.user_id';
COMMENT ON COLUMN user_center.user_auth_identities.identity_type IS '凭证类型：PHONE、EMAIL、GOOGLE、APPLE、DEVICE';
COMMENT ON COLUMN user_center.user_auth_identities.identity_value IS '凭证明文或脱敏值，生产环境应加密或脱敏存储，禁止写入日志';
COMMENT ON COLUMN user_center.user_auth_identities.identity_hash IS '凭证归一化后的哈希值，用于唯一索引和登录查询';
COMMENT ON COLUMN user_center.user_auth_identities.password_hash IS '密码哈希，不保存明文密码，禁止写入日志';
COMMENT ON COLUMN user_center.user_auth_identities.verified IS '凭证是否已验证：0=未验证，1=已验证';
COMMENT ON COLUMN user_center.user_auth_identities.verified_at IS '凭证验证时间，统一 UTC';
COMMENT ON COLUMN user_center.user_auth_identities.last_login_at IS '该凭证最近一次登录时间，统一 UTC';
COMMENT ON COLUMN user_center.user_auth_identities.created_at IS '创建时间，统一 UTC';
COMMENT ON COLUMN user_center.user_auth_identities.updated_at IS '更新时间，统一 UTC';
COMMENT ON COLUMN user_center.user_auth_identities.deleted IS '逻辑删除标记：0=未删除，1=已删除';

-- C. user_profiles 用户资料表
CREATE TABLE user_center.user_profiles (
    id                BIGSERIAL PRIMARY KEY,
    profile_id        BIGINT       NOT NULL,
    user_id           BIGINT       NOT NULL,
    nickname          VARCHAR(64)  NULL,
    gender            VARCHAR(16)  NULL,
    birth_date        DATE         NULL,
    country_code      VARCHAR(16)  NULL,
    city_code         VARCHAR(64)  NULL,
    language_codes    JSONB        NULL,
    bio               VARCHAR(500) NULL,
    avatar_key        VARCHAR(512) NULL,
    interests         JSONB        NULL,
    profile_score     INTEGER      NOT NULL DEFAULT 0,
    profile_completed INTEGER      NOT NULL DEFAULT 0,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted           INTEGER      NOT NULL DEFAULT 0,
    CONSTRAINT uk_user_profiles_profile_id UNIQUE (profile_id),
    CONSTRAINT uk_user_profiles_user_id UNIQUE (user_id)
);

COMMENT ON TABLE user_center.user_profiles IS '用户资料表：保存昵称、性别、生日、地区、头像、兴趣标签等展示和推荐所需信息';
COMMENT ON COLUMN user_center.user_profiles.id IS '数据库自增主键，仅用于物理存储，不对外暴露';
COMMENT ON COLUMN user_center.user_profiles.profile_id IS '资料业务主键';
COMMENT ON COLUMN user_center.user_profiles.user_id IS '用户业务主键';
COMMENT ON COLUMN user_center.user_profiles.nickname IS '用户昵称，用于聊天、Feed、推荐展示';
COMMENT ON COLUMN user_center.user_profiles.gender IS '用户性别：MALE、FEMALE、OTHER、UNKNOWN';
COMMENT ON COLUMN user_center.user_profiles.birth_date IS '出生日期，用于计算年龄，接口不直接返回完整生日给无权限调用方';
COMMENT ON COLUMN user_center.user_profiles.country_code IS '国家或地区编码';
COMMENT ON COLUMN user_center.user_profiles.city_code IS '城市编码';
COMMENT ON COLUMN user_center.user_profiles.language_codes IS '用户语言列表，JSON 数组';
COMMENT ON COLUMN user_center.user_profiles.bio IS '个人简介';
COMMENT ON COLUMN user_center.user_profiles.avatar_key IS '头像 object key，只存 key，不存完整 URL';
COMMENT ON COLUMN user_center.user_profiles.interests IS '兴趣标签，JSON 数组';
COMMENT ON COLUMN user_center.user_profiles.profile_score IS '资料完整度分数';
COMMENT ON COLUMN user_center.user_profiles.profile_completed IS '资料是否完整：0=未完成，1=已完成';
COMMENT ON COLUMN user_center.user_profiles.created_at IS '创建时间，统一 UTC';
COMMENT ON COLUMN user_center.user_profiles.updated_at IS '更新时间，统一 UTC';
COMMENT ON COLUMN user_center.user_profiles.deleted IS '逻辑删除标记：0=未删除，1=已删除';

-- D. user_photos 用户照片表
CREATE TABLE user_center.user_photos (
    id            BIGSERIAL PRIMARY KEY,
    photo_id      BIGINT       NOT NULL,
    user_id       BIGINT       NOT NULL,
    photo_type    VARCHAR(32)  NOT NULL,
    object_key    VARCHAR(512) NOT NULL,
    sort_order    INTEGER      NOT NULL DEFAULT 0,
    review_status VARCHAR(32)  NOT NULL DEFAULT 'PENDING',
    enabled       INTEGER      NOT NULL DEFAULT 1,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted       INTEGER      NOT NULL DEFAULT 0,
    CONSTRAINT uk_user_photos_photo_id UNIQUE (photo_id),
    CONSTRAINT uk_user_photos_user_object UNIQUE (user_id, object_key)
);

COMMENT ON TABLE user_center.user_photos IS '用户照片表：保存头像和相册 object key，不保存完整 URL';
COMMENT ON COLUMN user_center.user_photos.id IS '数据库自增主键，仅用于物理存储，不对外暴露';
COMMENT ON COLUMN user_center.user_photos.photo_id IS '照片业务主键';
COMMENT ON COLUMN user_center.user_photos.user_id IS '用户业务主键';
COMMENT ON COLUMN user_center.user_photos.photo_type IS '照片类型：AVATAR=头像，ALBUM=相册';
COMMENT ON COLUMN user_center.user_photos.object_key IS 'MinIO object key，只存 key，不存完整 URL';
COMMENT ON COLUMN user_center.user_photos.sort_order IS '排序值，越小越靠前';
COMMENT ON COLUMN user_center.user_photos.review_status IS '审核状态：PENDING、APPROVED、REJECTED';
COMMENT ON COLUMN user_center.user_photos.enabled IS '是否启用：0=禁用，1=启用';
COMMENT ON COLUMN user_center.user_photos.created_at IS '创建时间，统一 UTC';
COMMENT ON COLUMN user_center.user_photos.updated_at IS '更新时间，统一 UTC';
COMMENT ON COLUMN user_center.user_photos.deleted IS '逻辑删除标记：0=未删除，1=已删除';

-- E. user_devices 用户设备表
CREATE TABLE user_center.user_devices (
    id                  BIGSERIAL PRIMARY KEY,
    device_id           BIGINT       NOT NULL,
    user_id             BIGINT       NOT NULL,
    platform            VARCHAR(32)  NOT NULL,
    device_fingerprint  VARCHAR(128) NOT NULL,
    push_token_hash     VARCHAR(128) NULL,
    app_version         VARCHAR(64)  NULL,
    last_seen_at        TIMESTAMPTZ  NULL,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted             INTEGER      NOT NULL DEFAULT 0,
    CONSTRAINT uk_user_devices_device_id UNIQUE (device_id),
    CONSTRAINT uk_user_devices_user_fp UNIQUE (user_id, device_fingerprint)
);

COMMENT ON TABLE user_center.user_devices IS '用户设备表：保存用户登录设备、平台、设备指纹和推送 token 哈希';
COMMENT ON COLUMN user_center.user_devices.id IS '数据库自增主键，仅用于物理存储，不对外暴露';
COMMENT ON COLUMN user_center.user_devices.device_id IS '设备业务主键';
COMMENT ON COLUMN user_center.user_devices.user_id IS '用户业务主键';
COMMENT ON COLUMN user_center.user_devices.platform IS '平台：IOS、ANDROID、WEB';
COMMENT ON COLUMN user_center.user_devices.device_fingerprint IS '设备指纹，用于识别同一设备';
COMMENT ON COLUMN user_center.user_devices.push_token_hash IS '推送 token 哈希，不保存明文 token';
COMMENT ON COLUMN user_center.user_devices.app_version IS 'App 版本';
COMMENT ON COLUMN user_center.user_devices.last_seen_at IS '最近活跃时间，统一 UTC';
COMMENT ON COLUMN user_center.user_devices.created_at IS '创建时间，统一 UTC';
COMMENT ON COLUMN user_center.user_devices.updated_at IS '更新时间，统一 UTC';
COMMENT ON COLUMN user_center.user_devices.deleted IS '逻辑删除标记：0=未删除，1=已删除';

-- F. user_settings 用户设置表
CREATE TABLE user_center.user_settings (
    id                     BIGSERIAL PRIMARY KEY,
    setting_id             BIGINT       NOT NULL,
    user_id                BIGINT       NOT NULL,
    discoverable           INTEGER      NOT NULL DEFAULT 1,
    preferred_gender       VARCHAR(32)  NULL,
    preferred_age_min      INTEGER      NULL,
    preferred_age_max      INTEGER      NULL,
    notification_settings  JSONB        NULL,
    privacy_settings       JSONB        NULL,
    created_at             TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at             TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted                INTEGER      NOT NULL DEFAULT 0,
    CONSTRAINT uk_user_settings_setting_id UNIQUE (setting_id),
    CONSTRAINT uk_user_settings_user_id UNIQUE (user_id)
);

COMMENT ON TABLE user_center.user_settings IS '用户设置表：保存推荐可见性、偏好设置、通知设置和隐私设置';
COMMENT ON COLUMN user_center.user_settings.id IS '数据库自增主键，仅用于物理存储，不对外暴露';
COMMENT ON COLUMN user_center.user_settings.setting_id IS '设置业务主键';
COMMENT ON COLUMN user_center.user_settings.user_id IS '用户业务主键';
COMMENT ON COLUMN user_center.user_settings.discoverable IS '是否可被推荐：0=不可见，1=可见';
COMMENT ON COLUMN user_center.user_settings.preferred_gender IS '偏好性别';
COMMENT ON COLUMN user_center.user_settings.preferred_age_min IS '偏好最小年龄';
COMMENT ON COLUMN user_center.user_settings.preferred_age_max IS '偏好最大年龄';
COMMENT ON COLUMN user_center.user_settings.notification_settings IS '通知设置 JSON';
COMMENT ON COLUMN user_center.user_settings.privacy_settings IS '隐私设置 JSON';
COMMENT ON COLUMN user_center.user_settings.created_at IS '创建时间，统一 UTC';
COMMENT ON COLUMN user_center.user_settings.updated_at IS '更新时间，统一 UTC';
COMMENT ON COLUMN user_center.user_settings.deleted IS '逻辑删除标记：0=未删除，1=已删除';
