package com.dating.gateway.manager;

import com.dating.gateway.entity.AuthRefreshTokenEntity;
import com.dating.gateway.exception.GatewayBizException;
import com.dating.gateway.exception.GatewayErrorCode;
import com.dating.gateway.security.TokenHashUtil;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

/**
 * auth_refresh_token 表：refresh 轮换、reuse detection、撤销。
 */
@Repository
public class AuthRefreshTokenManager {

    private static final RowMapper<AuthRefreshTokenEntity> ROW_MAPPER = (rs, rowNum) -> {
        AuthRefreshTokenEntity entity = new AuthRefreshTokenEntity();
        entity.setId(rs.getLong("id"));
        entity.setUserId(rs.getLong("user_id"));
        entity.setDeviceId(rs.getString("device_id"));
        entity.setPlatform(rs.getInt("platform"));
        entity.setTokenHash(rs.getString("token_hash"));
        entity.setJti(rs.getString("jti"));
        entity.setExpiresAt(rs.getTimestamp("expires_at").toInstant());
        Timestamp usedAt = rs.getTimestamp("used_at");
        entity.setUsedAt(usedAt == null ? null : usedAt.toInstant());
        Timestamp revokedAt = rs.getTimestamp("revoked_at");
        entity.setRevokedAt(revokedAt == null ? null : revokedAt.toInstant());
        entity.setReplacedByJti(rs.getString("replaced_by_jti"));
        return entity;
    };

    private final JdbcTemplate jdbcTemplate;

    public AuthRefreshTokenManager(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insertRefreshToken(long userId,
                                   String deviceId,
                                   int platform,
                                   String refreshTokenPlain,
                                   String refreshJti,
                                   Instant expiresAt) {
        jdbcTemplate.update("""
                INSERT INTO auth_refresh_token (
                    user_id, device_id, platform, token_hash, jti, expires_at, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, now(), now())
                """,
                userId,
                deviceId,
                platform,
                TokenHashUtil.sha256Hex(refreshTokenPlain),
                refreshJti,
                Timestamp.from(expiresAt));
    }

    public Optional<AuthRefreshTokenEntity> findByTokenHash(String refreshTokenPlain) {
        try {
            AuthRefreshTokenEntity entity = jdbcTemplate.queryForObject("""
                    SELECT id, user_id, device_id, platform, token_hash, jti, expires_at,
                           used_at, revoked_at, replaced_by_jti
                    FROM auth_refresh_token
                    WHERE token_hash = ?
                    """, ROW_MAPPER, TokenHashUtil.sha256Hex(refreshTokenPlain));
            return Optional.ofNullable(entity);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    /**
     * 校验 refresh 记录状态；已 used 触发 reuse detection。
     */
    public AuthRefreshTokenEntity validateAndConsume(String refreshTokenPlain,
                                                     String deviceId,
                                                     int platform) {
        AuthRefreshTokenEntity entity = findByTokenHash(refreshTokenPlain)
                .orElseThrow(() -> new GatewayBizException(GatewayErrorCode.REFRESH_TOKEN_INVALID));
        if (entity.getUsedAt() != null) {
            revokeByUserAndDevice(entity.getUserId(), entity.getDeviceId(), entity.getPlatform());
            throw new GatewayBizException(GatewayErrorCode.REFRESH_TOKEN_REUSED);
        }
        if (entity.getRevokedAt() != null) {
            throw new GatewayBizException(GatewayErrorCode.REFRESH_TOKEN_REVOKED);
        }
        if (entity.getExpiresAt().isBefore(Instant.now())) {
            throw new GatewayBizException(GatewayErrorCode.REFRESH_TOKEN_EXPIRED);
        }
        if (!deviceId.equals(entity.getDeviceId()) || platform != entity.getPlatform()) {
            throw new GatewayBizException(GatewayErrorCode.REFRESH_TOKEN_DEVICE_MISMATCH);
        }
        jdbcTemplate.update("""
                UPDATE auth_refresh_token SET used_at = now(), updated_at = now() WHERE id = ?
                """, entity.getId());
        return entity;
    }

    public void markReplaced(long oldRecordId, String newRefreshJti) {
        jdbcTemplate.update("""
                UPDATE auth_refresh_token
                SET replaced_by_jti = ?, updated_at = now()
                WHERE id = ?
                """, newRefreshJti, oldRecordId);
    }

    public void revokeByUserAndDevice(long userId, String deviceId, int platform) {
        jdbcTemplate.update("""
                UPDATE auth_refresh_token
                SET revoked_at = now(), updated_at = now()
                WHERE user_id = ? AND device_id = ? AND platform = ? AND revoked_at IS NULL
                """, userId, deviceId, platform);
    }
}
