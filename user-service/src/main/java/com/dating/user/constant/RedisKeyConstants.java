package com.dating.user.constant;

/**
 * Redis Key 常量，本阶段仅定义前缀，不包含读写逻辑。
 */
public final class RedisKeyConstants {

    /** user-service Redis Key 统一前缀。 */
    public static final String KEY_PREFIX = "yanshuqi:user:";

    private RedisKeyConstants() {
    }
}
