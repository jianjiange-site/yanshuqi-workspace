package com.dating.user.service;

import com.dating.user.constant.RedisKeyConstants;
import com.dating.user.service.support.CacheSafeExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * 用户资料相关 Redis 缓存失效服务，本阶段仅删除缓存，不写入缓存。
 */
@Service
@Profile("!test")
public class UserCacheInvalidationService {

    private static final Logger log = LoggerFactory.getLogger(UserCacheInvalidationService.class);

    private final CacheSafeExecutor cacheSafeExecutor;

    public UserCacheInvalidationService(CacheSafeExecutor cacheSafeExecutor) {
        this.cacheSafeExecutor = cacheSafeExecutor;
    }

    /**
     * 删除用户资料相关四类缓存：basic / profile / status / profile_view。
     * 写路径（Onboarding、UpdateProfile、ConfirmAvatar、BindPhoto 等）统一调用此方法，
     * 避免业务代码散落拼 key；Redis 失败仅打日志，不回滚主事务。
     */
    public void evictProfileCache(Long userId) {
        if (userId == null || userId <= 0) {
            return;
        }
        try {
            cacheSafeExecutor.safeDeleteAll(RedisKeyConstants.allUserProfileCacheKeys(userId));
            log.info("用户资料缓存已删除, userId={}", userId);
        } catch (Exception ex) {
            log.warn("用户资料缓存删除失败, userId={}", userId, ex);
        }
    }
}
