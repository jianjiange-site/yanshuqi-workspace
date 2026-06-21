package com.dating.user.constant;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRedisKeyConstantsTest {

    @Test
    void profileCacheKeysShouldFollowPrefixAndFormat() {
        long userId = 12345L;
        assertEquals("yanshuqi:user:basic:12345", RedisKeyConstants.basicKey(userId));
        assertEquals("yanshuqi:user:profile:12345", RedisKeyConstants.profileKey(userId));
        assertEquals("yanshuqi:user:status:12345", RedisKeyConstants.statusKey(userId));
        assertEquals("yanshuqi:user:profile_view:12345", RedisKeyConstants.profileViewKey(userId));
    }

    @Test
    void allUserProfileCacheKeysShouldContainFourKeys() {
        List<String> keys = RedisKeyConstants.allUserProfileCacheKeys(9001L);
        assertEquals(4, keys.size());
        assertTrue(keys.contains(RedisKeyConstants.basicKey(9001L)));
        assertTrue(keys.contains(RedisKeyConstants.profileKey(9001L)));
        assertTrue(keys.contains(RedisKeyConstants.statusKey(9001L)));
        assertTrue(keys.contains(RedisKeyConstants.profileViewKey(9001L)));
    }

    @Test
    void shouldNotDefineHomeCardSelfTargetKey() {
        String sample = RedisKeyConstants.KEY_PREFIX + "home_card:1:2";
        List<String> keys = RedisKeyConstants.allUserProfileCacheKeys(1L);
        assertFalse(keys.contains(sample));
        assertFalse(RedisKeyConstants.KEY_PREFIX.contains("home_card"));
    }

    @Test
    void defaultTtlShouldBeSixHundredSeconds() {
        assertEquals(600, RedisKeyConstants.DEFAULT_PROFILE_TTL_SECONDS);
    }
}
