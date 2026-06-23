package com.dating.gateway.security;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * access token 登出黑名单：key=gateway:auth:blacklist:{jti}，TTL=token 剩余有效期。
 */
@Service
public class TokenBlacklistService {

    private final StringRedisTemplate stringRedisTemplate;

    public TokenBlacklistService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void blacklist(String jti, Instant expiresAt) {
        if (jti == null || jti.isBlank()) {
            return;
        }
        Duration ttl = Duration.between(Instant.now(), expiresAt);
        if (ttl.isNegative() || ttl.isZero()) {
            ttl = Duration.ofSeconds(1);
        }
        stringRedisTemplate.opsForValue().set(
                AuthRedisKeys.accessBlacklistKey(jti),
                "1",
                ttl);
    }

    public boolean isBlacklisted(String jti) {
        if (jti == null || jti.isBlank()) {
            return false;
        }
        Boolean exists = stringRedisTemplate.hasKey(AuthRedisKeys.accessBlacklistKey(jti));
        return Boolean.TRUE.equals(exists);
    }
}
