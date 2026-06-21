package com.dating.user.service;

import com.dating.user.constant.RedisKeyConstants;
import com.dating.user.service.support.CacheSafeExecutor;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserCacheInvalidationPolicyTest {

    @Test
    void evictProfileCacheShouldDeleteAllFourKeys() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        when(redisTemplate.delete(anyCollection())).thenReturn(4L);
        UserCacheInvalidationService service = new UserCacheInvalidationService(new CacheSafeExecutor(redisTemplate));

        service.evictProfileCache(8001L);

        ArgumentCaptor<Collection<String>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(redisTemplate).delete(captor.capture());
        List<String> keys = captor.getValue().stream().map(String::valueOf).toList();
        assertTrue(keys.contains(RedisKeyConstants.profileViewKey(8001L)));
    }

    @Test
    void redisDeleteFailureShouldNotThrow() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        when(redisTemplate.delete(anyCollection())).thenThrow(new RuntimeException("redis down"));
        UserCacheInvalidationService service = new UserCacheInvalidationService(new CacheSafeExecutor(redisTemplate));

        assertDoesNotThrow(() -> service.evictProfileCache(7001L));
    }
}
