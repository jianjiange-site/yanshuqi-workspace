package com.dating.user.exception;

import com.dating.user.config.UserCacheProperties;
import com.dating.user.constant.RedisKeyConstants;
import com.dating.user.service.impl.UserProfileCacheServiceImpl;
import com.dating.user.service.support.CacheSafeExecutor;
import com.dating.user.vo.BasicUserProfileVO;
import com.dating.user.vo.UserAvailableVO;
import io.grpc.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 用户资料缓存服务单元测试。
 */
@ExtendWith(MockitoExtension.class)
class UserProfileCacheServiceTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private UserCacheProperties userCacheProperties;

    private UserProfileCacheServiceImpl cacheService;

    /**
     * 初始化测试依赖。
     */
    @BeforeEach
    void setUp() {
        userCacheProperties = new UserCacheProperties();
        userCacheProperties.setProfileTtlSeconds(600);
        CacheSafeExecutor cacheSafeExecutor = new CacheSafeExecutor(stringRedisTemplate);
        cacheService = new UserProfileCacheServiceImpl(cacheSafeExecutor, userCacheProperties);
    }

    /**
     * Redis get 失败时应降级为 cache miss。
     */
    @Test
    void getShouldReturnEmptyWhenRedisFails() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.multiGet(anyList())).thenThrow(new RuntimeException("redis down"));

        Map<Long, BasicUserProfileVO> result = cacheService.getBasicProfiles(List.of(1001L));

        assertTrue(result.isEmpty());
    }

    /**
     * Redis set 失败时不应抛出异常。
     */
    @Test
    void putShouldIgnoreRedisWriteFailure() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        doThrow(new RuntimeException("redis down")).when(valueOperations)
                .set(anyString(), anyString(), any(Duration.class));

        BasicUserProfileVO vo = new BasicUserProfileVO();
        vo.setUserId(1001L);
        vo.setNickname("nick");

        cacheService.putBasicProfiles(Map.of(1001L, vo));
    }

    /**
     * 反序列化失败时应删除坏 key 并忽略该缓存。
     */
    @Test
    void getShouldDeleteCorruptedCacheEntry() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.multiGet(anyList())).thenReturn(List.of("{invalid-json"));

        Map<Long, BasicUserProfileVO> result = cacheService.getBasicProfiles(List.of(1001L));

        assertTrue(result.isEmpty());
        verify(stringRedisTemplate).delete(RedisKeyConstants.basicKey(1001L));
    }

    /**
     * 写入缓存时应设置 TTL。
     */
    @Test
    void putShouldUseConfiguredTtl() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        UserAvailableVO vo = new UserAvailableVO();
        vo.setUserId(1001L);
        vo.setAvailable(true);
        cacheService.putUserStatuses(Map.of(1001L, vo));

        verify(valueOperations).set(
                eq(RedisKeyConstants.statusKey(1001L)),
                anyString(),
                eq(Duration.ofSeconds(600))
        );
    }

    /**
     * 缓存 VO 不应包含敏感字段名。
     */
    @Test
    void cachedVoShouldNotContainSensitiveFieldNames() {
        BasicUserProfileVO vo = new BasicUserProfileVO();
        vo.setUserId(1001L);
        vo.setNickname("nick");
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        cacheService.putBasicProfiles(Map.of(1001L, vo));

        verify(valueOperations).set(eq(RedisKeyConstants.basicKey(1001L)), anyString(), any(Duration.class));
        assertFalse(vo.getClass().getDeclaredFields().length == 0);
    }
}
