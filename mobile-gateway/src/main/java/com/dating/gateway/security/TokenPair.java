package com.dating.gateway.security;

/**
 * 一次登录签发的 access + refresh 明文对（refresh 明文仅此时返回给客户端）。
 */
public class TokenPair {

    private final String accessToken;
    private final String refreshToken;
    private final String accessJti;
    private final String refreshJti;
    private final long accessExpiresAtMs;
    private final long refreshExpiresAtMs;

    public TokenPair(String accessToken,
                     String refreshToken,
                     String accessJti,
                     String refreshJti,
                     long accessExpiresAtMs,
                     long refreshExpiresAtMs) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessJti = accessJti;
        this.refreshJti = refreshJti;
        this.accessExpiresAtMs = accessExpiresAtMs;
        this.refreshExpiresAtMs = refreshExpiresAtMs;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getAccessJti() {
        return accessJti;
    }

    public String getRefreshJti() {
        return refreshJti;
    }

    public long getAccessExpiresAtMs() {
        return accessExpiresAtMs;
    }

    public long getRefreshExpiresAtMs() {
        return refreshExpiresAtMs;
    }
}
