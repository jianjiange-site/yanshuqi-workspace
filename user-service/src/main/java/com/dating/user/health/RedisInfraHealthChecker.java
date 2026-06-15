package com.dating.user.health;

import com.dating.user.config.AppProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class RedisInfraHealthChecker {

    private final StringRedisTemplate stringRedisTemplate;
    private final AppProperties appProperties;

    public RedisInfraHealthChecker(StringRedisTemplate stringRedisTemplate, AppProperties appProperties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.appProperties = appProperties;
    }

    public Map<String, Object> check() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("component", "redis");
        String testKey = appProperties.getInfra().getRedisTestKey();
        int ttlSeconds = appProperties.getInfra().getRedisTestTtlSeconds();
        result.put("testKey", testKey);
        try {
            stringRedisTemplate.opsForValue().set(testKey, "ping", Duration.ofSeconds(ttlSeconds));
            String value = stringRedisTemplate.opsForValue().get(testKey);
            if (!Objects.equals("ping", value)) {
                throw new IllegalStateException("Redis read/write mismatch");
            }
            stringRedisTemplate.delete(testKey);
            result.put("status", "UP");
        } catch (Exception ex) {
            result.put("status", "DOWN");
            result.put("error", ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
        return result;
    }
}