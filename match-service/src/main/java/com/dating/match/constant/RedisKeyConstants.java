package com.dating.match.constant;

/**
 * Match 域 Redis Key 常量，统一 yanshuqi 前缀。
 * <p>
 * 工程约束要求所有 Redis key 带 yanshuqi 前缀，避免多学员/多环境 key 冲突；
 * 分布式锁 key 同样纳入统一命名空间；日期段使用 UTC，保证跨时区配额与日切一致。
 */
public final class RedisKeyConstants {

    public static final String KEY_PREFIX = "yanshuqi:match:";

    public static final String LOCK_PREFIX = "yanshuqi:lock:match:";

    private RedisKeyConstants() {
    }

    public static String feedKey(Long userId) {
        return KEY_PREFIX + "feed:" + userId;
    }

    public static String quotaKey(Long userId, java.time.LocalDate utcDate) {
        return KEY_PREFIX + "quota:" + userId + ":" + formatUtcDate(utcDate);
    }

    public static String swipedKey(Long userId) {
        return KEY_PREFIX + "swiped:" + userId;
    }

    public static String prefKey(Long userId) {
        return KEY_PREFIX + "pref:" + userId;
    }

    public static String swipeLockKey(Long userId, Long targetUserId) {
        return LOCK_PREFIX + "swipe:" + userId + ":" + targetUserId;
    }

    public static String d1LockKey(java.time.LocalDate utcDate) {
        return LOCK_PREFIX + "d1:" + formatUtcDate(utcDate);
    }

    /** SuperHi clientRequestId 幂等 key，TTL 36 小时。 */
    public static String superHiRequestKey(Long userId, String clientRequestId) {
        return KEY_PREFIX + "superhi:req:" + userId + ":" + clientRequestId;
    }

    private static String formatUtcDate(java.time.LocalDate utcDate) {
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd");
        return utcDate.format(formatter);
    }
}
