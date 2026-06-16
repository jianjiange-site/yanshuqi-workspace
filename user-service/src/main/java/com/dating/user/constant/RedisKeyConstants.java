package com.dating.user.constant;

/**
 * Redis Key 常量，本阶段仅定义前缀，不包含读写逻辑。
 */
public final class RedisKeyConstants {

    /** user-service Redis Key 统一前缀。 */
    public static final String KEY_PREFIX = "yanshuqi:user:";

    /**
     * 构建用户资料缓存 Key。
     *
     * @param userId 用户业务主键
     * @return Redis Key
     */
    public static String profileKey(long userId) {
        return KEY_PREFIX + "profile:" + userId;
    }

    /**
     * 构建用户基础信息缓存 Key。
     *
     * @param userId 用户业务主键
     * @return Redis Key
     */
    public static String basicKey(long userId) {
        return KEY_PREFIX + "basic:" + userId;
    }

    /**
     * 构建用户状态缓存 Key。
     *
     * @param userId 用户业务主键
     * @return Redis Key
     */
    public static String statusKey(long userId) {
        return KEY_PREFIX + "status:" + userId;
    }

    private RedisKeyConstants() {
    }
}
