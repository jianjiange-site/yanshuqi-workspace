package com.dating.user.service.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

/**
 * Redis 安全执行器，统一缓存读写删的异常降级策略。
 */
@Component
@Profile("!test")
public class CacheSafeExecutor {

    private static final Logger log = LoggerFactory.getLogger(CacheSafeExecutor.class);

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 构造 Redis 安全执行器。
     *
     * @param stringRedisTemplate Redis 字符串模板
     */
    public CacheSafeExecutor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 安全批量读取 Redis 字符串值。
     *
     * @param keys Redis Key 列表
     * @return 值列表；失败时返回 null 表示全部 cache miss
     */
    public List<String> safeMultiGet(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return List.of();
        }
        try {
            return stringRedisTemplate.opsForValue().multiGet(keys);
        } catch (Exception ex) {
            log.warn("Redis 批量读取失败, keyCount={}", keys.size(), ex);
            return null;
        }
    }

    /**
     * 安全写入 Redis 字符串值并设置 TTL。
     *
     * @param key   Redis Key
     * @param value 缓存值
     * @param ttl   过期时间
     * @return true 表示写入成功
     */
    public boolean safeSet(String key, String value, Duration ttl) {
        if (!org.springframework.util.StringUtils.hasText(key) || value == null) {
            return false;
        }
        try {
            stringRedisTemplate.opsForValue().set(key, value, ttl);
            return true;
        } catch (Exception ex) {
            log.warn("Redis 写入失败, key={}", key, ex);
            return false;
        }
    }

    /**
     * 安全删除单个 Redis Key。
     *
     * @param key Redis Key
     */
    public void safeDelete(String key) {
        if (!org.springframework.util.StringUtils.hasText(key)) {
            return;
        }
        try {
            stringRedisTemplate.delete(key);
        } catch (Exception ex) {
            log.warn("Redis 删除失败, key={}", key, ex);
        }
    }

    /**
     * 安全批量删除 Redis Key。
     *
     * @param keys Redis Key 集合
     */
    public void safeDeleteAll(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        try {
            stringRedisTemplate.delete(keys);
        } catch (Exception ex) {
            log.warn("Redis 批量删除失败, keyCount={}", keys.size(), ex);
        }
    }
}
