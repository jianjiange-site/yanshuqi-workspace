package com.dating.post.constant;

/**
 * 性别分桶，用于 Feed 热门池 / 冷启动池按异性推荐分桶。
 */
public enum GenderBucket {

    MALE("male"),
    FEMALE("female");

    private final String redisSuffix;

    GenderBucket(String redisSuffix) {
        this.redisSuffix = redisSuffix;
    }

    public String getRedisSuffix() {
        return redisSuffix;
    }

    /** 取异性分桶：男看女池，女看男池。 */
    public GenderBucket opposite() {
        return this == MALE ? FEMALE : MALE;
    }
}
