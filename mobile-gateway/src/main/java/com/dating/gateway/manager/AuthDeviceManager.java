package com.dating.gateway.manager;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * auth_device 表读写：登录成功后 upsert 设备登记。
 */
@Repository
public class AuthDeviceManager {

    private final JdbcTemplate jdbcTemplate;

    public AuthDeviceManager(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 按 (user_id, device_id, platform) upsert 设备信息并刷新 last_login_at。
     */
    public void upsertDevice(long userId,
                             String deviceId,
                             int platform,
                             String deviceModel,
                             String osVersion,
                             String appVersion,
                             String pushToken,
                             Instant lastLoginAt) {
        Timestamp loginTs = Timestamp.from(lastLoginAt);
        int updated = jdbcTemplate.update("""
                UPDATE auth_device
                SET device_model = ?, os_version = ?, app_version = ?, push_token = ?,
                    last_login_at = ?, updated_at = now()
                WHERE user_id = ? AND device_id = ? AND platform = ?
                """,
                deviceModel, osVersion, appVersion, pushToken, loginTs,
                userId, deviceId, platform);
        if (updated > 0) {
            return;
        }
        jdbcTemplate.update("""
                INSERT INTO auth_device (
                    user_id, device_id, platform, device_model, os_version, app_version,
                    push_token, last_login_at, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, now(), now())
                """,
                userId, deviceId, platform, deviceModel, osVersion, appVersion, pushToken, loginTs);
    }
}
