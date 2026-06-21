package com.dating.user.service.support;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserCacheSafeExecutorTest {

    @Test
    void safeDeleteAllShouldIgnoreFlushCommands() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        CacheSafeExecutor executor = new CacheSafeExecutor(redisTemplate);

        executor.safeDeleteAll(List.of("FLUSHDB", "yanshuqi:user:basic:1", "prefixFLUSHALLsuffix"));

        ArgumentCaptor<Collection<String>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(redisTemplate).delete(captor.capture());
        List<String> keys = captor.getValue().stream().map(String::valueOf).toList();
        assertEquals(1, keys.size());
        assertEquals("yanshuqi:user:basic:1", keys.get(0));
    }

    @Test
    void safeMultiGetFailureShouldReturnNull() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(ops);
        when(ops.multiGet(any())).thenThrow(new RuntimeException("redis down"));
        CacheSafeExecutor executor = new CacheSafeExecutor(redisTemplate);

        assertNull(executor.safeMultiGet(List.of("yanshuqi:user:basic:1")));
    }

    @Test
    void safeSetFailureShouldReturnFalse() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(ops);
        doThrow(new RuntimeException("redis down"))
                .when(ops).set(anyString(), anyString(), any(Duration.class));
        CacheSafeExecutor executor = new CacheSafeExecutor(redisTemplate);

        assertFalse(executor.safeSet("yanshuqi:user:basic:1", "{}", Duration.ofSeconds(60)));
    }

    @Test
    void safeDeleteFailureShouldNotThrow() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        when(redisTemplate.delete(anyCollection())).thenThrow(new RuntimeException("redis down"));
        CacheSafeExecutor executor = new CacheSafeExecutor(redisTemplate);

        assertDoesNotThrow(() -> executor.safeDeleteAll(List.of("yanshuqi:user:basic:1")));
    }
}
