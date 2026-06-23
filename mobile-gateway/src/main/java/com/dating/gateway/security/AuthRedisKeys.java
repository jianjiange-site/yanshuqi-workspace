package com.dating.gateway.security;

/**
 * JWT 黑名单等鉴权 Redis Key 命名。
 */
public final class AuthRedisKeys {

    private AuthRedisKeys() {
    }

    public static String smsCodeKey(String phone) {
        return "gateway:auth:sms:" + phone;
    }

    public static String smsCooldownKey(String phone) {
        return "gateway:auth:sms:cooldown:" + phone;
    }

    public static String accessBlacklistKey(String jti) {
        return "gateway:auth:blacklist:" + jti;
    }
}
