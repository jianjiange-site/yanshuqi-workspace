package com.dating.post.constant;

/**
 * Post 域 Redis key 统一管理，前缀遵循 yanshuqi:post: 规范。
 */
public final class PostRedisKeys {

    private static final String PREFIX = "yanshuqi:post:";

    /** 通用 TTL：7 天。 */
    public static final long TTL_SECONDS = 7L * 24 * 60 * 60;

    /** 帖子详情缓存 TTL：7 天。 */
    public static final long DETAIL_TTL_SECONDS = TTL_SECONDS;

    /** 评论窗口最大条数。 */
    public static final int COMMENT_WINDOW_SIZE = 200;

    /** 热门推荐池最大条数。 */
    public static final int RECOMMEND_POOL_SIZE = 3000;

    /** 冷启动池最大条数。 */
    public static final int COLD_START_POOL_SIZE = 10000;

    /** 好友时间线最大条数。 */
    public static final int TIMELINE_SIZE = 100;

    private static final String FEED_PREFIX = "yanshuqi:feed:";
    private static final String USER_PREFIX = "yanshuqi:user:";

    private PostRedisKeys() {
    }

    public static String detail(long postId) {
        return PREFIX + "detail:" + postId;
    }

    public static String statIncrLikes(long postId) {
        return PREFIX + "stat:incr:" + postId + ":likes";
    }

    public static String statIncrComments(long postId) {
        return PREFIX + "stat:incr:" + postId + ":comments";
    }

    public static String updatedSet() {
        return PREFIX + "updated_set";
    }

    public static String commentWindow(long postId) {
        return PREFIX + "comments:" + postId;
    }

    /** 热门推荐正式池：按性别分桶，ZSet score=热度分。 */
    public static String recommendPool(GenderBucket bucket) {
        return FEED_PREFIX + "pool:recommend:" + bucket.getRedisSuffix();
    }

    /** 热门推荐临时池：重建完成后 rename 原子替换正式池。 */
    public static String recommendPoolTmp(GenderBucket bucket) {
        return FEED_PREFIX + "pool:recommend:" + bucket.getRedisSuffix() + ":tmp";
    }

    /** 冷启动池：新帖按作者性别分桶，score=createdAt epoch seconds。 */
    public static String coldStartPool(GenderBucket bucket) {
        return FEED_PREFIX + "cold_start:pool:" + bucket.getRedisSuffix();
    }

    /** 好友时间线：发帖写扩散到好友的 ZSet。 */
    public static String userTimeline(long userId) {
        return USER_PREFIX + "timeline:" + userId;
    }

    /** 已读帖子 Set：当前用 Set 去重，后续可替换 Redisson BloomFilter。 */
    public static String readPosts(long userId) {
        return USER_PREFIX + "read:posts:" + userId;
    }
}
