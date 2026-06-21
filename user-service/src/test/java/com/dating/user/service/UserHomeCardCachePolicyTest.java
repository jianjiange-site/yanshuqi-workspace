package com.dating.user.service;

import com.dating.user.constant.RedisKeyConstants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * 本阶段不新增 self-target 组合 HomeCard 缓存。
 */
class UserHomeCardCachePolicyTest {

    @Test
    void shouldNotDefineHomeCardCompositeCacheKey() {
        assertFalse(RedisKeyConstants.class.getDeclaredMethods().length > 0
                && java.util.Arrays.stream(RedisKeyConstants.class.getDeclaredMethods())
                .anyMatch(m -> m.getName().toLowerCase().contains("homecard")));
    }
}
