package com.dating.match.recommend;

/**
 * Feed Redis LIST 元素：<targetUserId>:<targetUserType>。
 */
public class FeedQueueItem {

    private final long targetUserId;
    private final int targetUserType;

    public FeedQueueItem(long targetUserId, int targetUserType) {
        this.targetUserId = targetUserId;
        this.targetUserType = targetUserType;
    }

    public long getTargetUserId() {
        return targetUserId;
    }

    public int getTargetUserType() {
        return targetUserType;
    }

    public String encode() {
        return targetUserId + ":" + targetUserType;
    }

    public static FeedQueueItem decode(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("invalid feed queue item: " + value);
        }
        String[] parts = value.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("invalid feed queue item: " + value);
        }
        return new FeedQueueItem(Long.parseLong(parts[0]), Integer.parseInt(parts[1]));
    }
}
