package com.dating.gateway.security;

/**
 * 解析后的 JWT 声明，供 Filter 写入 {@link CurrentUserContext}。
 */
public class JwtClaims {

    private final long userId;
    private final String jti;
    private final String deviceId;
    private final int platform;
    private final Integer tokenVersion;
    private final long expiresAtEpochSeconds;

    public JwtClaims(long userId,
                     String jti,
                     String deviceId,
                     int platform,
                     Integer tokenVersion,
                     long expiresAtEpochSeconds) {
        this.userId = userId;
        this.jti = jti;
        this.deviceId = deviceId;
        this.platform = platform;
        this.tokenVersion = tokenVersion;
        this.expiresAtEpochSeconds = expiresAtEpochSeconds;
    }

    public long getUserId() {
        return userId;
    }

    public String getJti() {
        return jti;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public int getPlatform() {
        return platform;
    }

    public Integer getTokenVersion() {
        return tokenVersion;
    }

    public long getExpiresAtEpochSeconds() {
        return expiresAtEpochSeconds;
    }
}
