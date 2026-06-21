package com.dating.match.repository;

import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

/**
 * 基于 StringRedisTemplate 的配额 Hash 仓储。
 */
@Repository
@Profile("!test")
public class RedisQuotaHashRepository implements QuotaHashRepository {

    private final StringRedisTemplate stringRedisTemplate;

    public RedisQuotaHashRepository(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public long increment(String quotaKey, String field, long delta) {
        Long value = stringRedisTemplate.opsForHash().increment(quotaKey, field, delta);
        if (value == null) {
            throw new IllegalStateException("Redis HINCRBY 返回 null");
        }
        return value;
    }

    @Override
    public long get(String quotaKey, String field) {
        Object value = stringRedisTemplate.opsForHash().get(quotaKey, field);
        if (value == null) {
            return 0L;
        }
        return Long.parseLong(value.toString());
    }

    @Override
    public void ensureTtl(String quotaKey, long ttlSeconds) {
        stringRedisTemplate.expire(quotaKey, ttlSeconds, TimeUnit.SECONDS);
    }
}
