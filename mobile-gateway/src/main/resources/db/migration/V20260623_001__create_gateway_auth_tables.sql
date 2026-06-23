-- GW-1：网关鉴权表（设备登记 + refresh token 轮换链）

CREATE TABLE IF NOT EXISTS auth_device (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    device_id       VARCHAR(128) NOT NULL,
    platform        INTEGER NOT NULL,
    device_model    VARCHAR(128),
    os_version      VARCHAR(64),
    app_version     VARCHAR(64),
    push_token      VARCHAR(512),
    last_login_at   TIMESTAMPTZ NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_auth_device_user_device_platform
    ON auth_device (user_id, device_id, platform);
CREATE INDEX IF NOT EXISTS idx_auth_device_device_platform
    ON auth_device (device_id, platform);
CREATE INDEX IF NOT EXISTS idx_auth_device_user_id
    ON auth_device (user_id);

CREATE TABLE IF NOT EXISTS auth_refresh_token (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    device_id       VARCHAR(128) NOT NULL,
    platform        INTEGER NOT NULL,
    token_hash      VARCHAR(64) NOT NULL,
    jti             VARCHAR(64) NOT NULL,
    expires_at      TIMESTAMPTZ NOT NULL,
    used_at         TIMESTAMPTZ,
    revoked_at      TIMESTAMPTZ,
    replaced_by_jti VARCHAR(64),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_auth_refresh_token_hash
    ON auth_refresh_token (token_hash);
CREATE UNIQUE INDEX IF NOT EXISTS uk_auth_refresh_token_jti
    ON auth_refresh_token (jti);
CREATE INDEX IF NOT EXISTS idx_auth_refresh_token_user_device
    ON auth_refresh_token (user_id, device_id);
CREATE INDEX IF NOT EXISTS idx_auth_refresh_token_expires_at
    ON auth_refresh_token (expires_at);
