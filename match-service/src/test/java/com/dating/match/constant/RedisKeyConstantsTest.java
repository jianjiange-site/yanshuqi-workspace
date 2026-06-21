package com.dating.match.constant;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RedisKeyConstantsTest {

    @Test
    void feedKey_shouldUseYanshuqiPrefix() {
        assertEquals("yanshuqi:match:feed:10001", RedisKeyConstants.feedKey(10001L));
    }

    @Test
    void quotaKey_shouldUseUtcDate() {
        LocalDate utcDate = LocalDate.of(2026, 6, 21);
        assertEquals("yanshuqi:match:quota:10001:20260621", RedisKeyConstants.quotaKey(10001L, utcDate));
    }

    @Test
    void swipedKey_shouldUseYanshuqiPrefix() {
        assertEquals("yanshuqi:match:swiped:10001", RedisKeyConstants.swipedKey(10001L));
    }

    @Test
    void prefKey_shouldUseYanshuqiPrefix() {
        assertEquals("yanshuqi:match:pref:10001", RedisKeyConstants.prefKey(10001L));
    }

    @Test
    void swipeLockKey_shouldUseLockPrefix() {
        assertEquals("yanshuqi:lock:match:swipe:10001:20002", RedisKeyConstants.swipeLockKey(10001L, 20002L));
    }

    @Test
    void superHiRequestKey_shouldUseYanshuqiPrefix() {
        assertEquals("yanshuqi:match:superhi:req:10001:req-abc",
                RedisKeyConstants.superHiRequestKey(10001L, "req-abc"));
    }
}
