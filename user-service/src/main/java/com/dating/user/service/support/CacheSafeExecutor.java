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
 * Redis 故障不能阻断主业务：读失败视为 miss 回源 DB，写/删失败仅打 warn 日志。
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
     * 安全批量删除 Redis Key；禁止 FLUSHDB/FLUSHALL 等危险命令渗入 key 名。
     */
    public void safeDeleteAll(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        List<String> safeKeys = keys.stream()
                .filter(key -> org.springframework.util.StringUtils.hasText(key))
                .filter(key -> !isDangerousRedisKey(key))
                .toList();
        if (safeKeys.isEmpty()) {
            return;
        }
        try {
            stringRedisTemplate.delete(safeKeys);
        } catch (Exception ex) {
            // Redis 删除失败不能影响主流程，仅记录告警
            log.warn("Redis 批量删除失败, keyCount={}", safeKeys.size(), ex);
        }
    }

    /**
     * 拒绝将 FLUSH 类命令伪装成 key，防止误删全库。
     */
    private boolean isDangerousRedisKey(String key) {
        String upper = key.trim().toUpperCase();
        return upper.contains("FLUSHDB") || upper.contains("FLUSHALL");
    }
}
