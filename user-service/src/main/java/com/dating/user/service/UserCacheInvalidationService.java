package com.dating.user.service;

import com.dating.user.constant.RedisKeyConstants;
import com.dating.user.service.support.CacheSafeExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户资料相关 Redis 缓存失效服务，本阶段仅删除缓存，不写入缓存。
 */
@Service
@Profile("!test")
public class UserCacheInvalidationService {

    private static final Logger log = LoggerFactory.getLogger(UserCacheInvalidationService.class);

    private final CacheSafeExecutor cacheSafeExecutor;

    /**
     * 构造缓存失效服务。
     *
     * @param cacheSafeExecutor Redis 安全执行器
     */
    public UserCacheInvalidationService(CacheSafeExecutor cacheSafeExecutor) {
        this.cacheSafeExecutor = cacheSafeExecutor;
    }

    /**
     * 删除用户资料相关缓存 Key。
     *
     * @param userId 用户业务主键
     */
    public void evictProfileCache(Long userId) {
        if (userId == null || userId <= 0) {
            return;
        }
        List<String> keys = List.of(
                RedisKeyConstants.profileKey(userId),
                RedisKeyConstants.basicKey(userId),
                RedisKeyConstants.statusKey(userId)
        );
        try {
            // 1. 删除 profile/basic/status 三个缓存 Key
            cacheSafeExecutor.safeDeleteAll(keys);
            log.info("用户资料缓存已删除, userId={}", userId);
        } catch (Exception ex) {
            log.warn("用户资料缓存删除失败, userId={}", userId, ex);
        }
    }
}
