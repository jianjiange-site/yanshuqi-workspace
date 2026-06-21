package com.dating.user.constant;

/**
 * Redis Key 常量，本阶段仅定义前缀，不包含读写逻辑。
 */
public final class RedisKeyConstants {

    /** user-service Redis Key 统一前缀。 */
    public static final String KEY_PREFIX = "yanshuqi:user:";

    /** 资料缓存默认 TTL（秒），实际以 UserCacheProperties 为准。 */
    public static final int DEFAULT_PROFILE_TTL_SECONDS = 600;

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

    /**
     * 构建 Swagger 资料视图缓存 Key。
     *
     * @param userId 用户业务主键
     * @return Redis Key
     */
    public static String profileViewKey(long userId) {
        return KEY_PREFIX + "profile_view:" + userId;
    }

    /**
     * USER-09 资料相关缓存 Key 清单（basic/profile/status/profile_view）。
     * 不引入 home_card:{self}:{target} 组合 key，避免资料更新时无法精准失效。
     *
     * @param userId 用户业务主键
     * @return 需要统一失效的 Key 列表
     */
    public static java.util.List<String> allUserProfileCacheKeys(long userId) {
        return java.util.List.of(
                profileKey(userId),
                basicKey(userId),
                statusKey(userId),
                profileViewKey(userId)
        );
    }

    private RedisKeyConstants() {
    }
}
