package com.dating.user.service;

import com.dating.user.constant.RedisKeyConstants;
import com.dating.user.service.support.CacheSafeExecutor;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserProfileCacheEvictTest {

    @Test
    void shouldEvictProfileViewCacheKey() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        when(redisTemplate.delete(org.mockito.ArgumentMatchers.anyCollection())).thenReturn(4L);
        UserCacheInvalidationService service = new UserCacheInvalidationService(new CacheSafeExecutor(redisTemplate));

        service.evictProfileCache(9001L);

        ArgumentCaptor<Collection<String>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(redisTemplate).delete(captor.capture());
        List<String> keys = captor.getValue().stream().map(String::valueOf).toList();
        assertTrue(keys.contains(RedisKeyConstants.profileKey(9001L)));
        assertTrue(keys.contains(RedisKeyConstants.basicKey(9001L)));
        assertTrue(keys.contains(RedisKeyConstants.statusKey(9001L)));
        assertTrue(keys.contains(RedisKeyConstants.profileViewKey(9001L)));
    }
}
