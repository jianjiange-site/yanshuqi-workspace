package com.dating.match.service;

import com.dating.match.constant.RedisKeyConstants;
import com.dating.match.dto.SuperHiResult;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Redis 实现的 SuperHi clientRequestId 幂等存储。
 */
@Service
@Profile("!test")
public class RedisSuperHiIdempotencyStore implements SuperHiIdempotencyStore {

    public static final long IDEMPOTENCY_TTL_SECONDS = 36 * 3600L;

    private final StringRedisTemplate stringRedisTemplate;

    public RedisSuperHiIdempotencyStore(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public Optional<SuperHiResult> find(long userId, String clientRequestId) {
        String value = stringRedisTemplate.opsForValue()
                .get(RedisKeyConstants.superHiRequestKey(userId, clientRequestId));
        if (!StringUtils.hasText(value)) {
            return Optional.empty();
        }
        return Optional.of(SuperHiIdempotencyCodec.parse(value));
    }

    @Override
    public void save(long userId, String clientRequestId, SuperHiResult result) {
        String key = RedisKeyConstants.superHiRequestKey(userId, clientRequestId);
        stringRedisTemplate.opsForValue().set(
                key,
                SuperHiIdempotencyCodec.format(result),
                IDEMPOTENCY_TTL_SECONDS,
                TimeUnit.SECONDS);
    }
}
