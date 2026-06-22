package com.dating.post.dto;

import com.dating.post.exception.PostBusinessException;
import com.dating.post.exception.PostErrorCode;
import org.springframework.util.StringUtils;

/**
 * Feed 分页游标，格式 recOffset:csOffset；空或 0:0 表示第一页。
 */
public class FeedCursor {

    private final int recOffset;
    private final int csOffset;

    public FeedCursor(int recOffset, int csOffset) {
        this.recOffset = Math.max(0, recOffset);
        this.csOffset = Math.max(0, csOffset);
    }

    public static FeedCursor initial() {
        return new FeedCursor(0, 0);
    }

    public static FeedCursor parse(String cursor) {
        if (!StringUtils.hasText(cursor) || "0".equals(cursor.trim()) || "0:0".equals(cursor.trim())) {
            return initial();
        }
        String[] parts = cursor.trim().split(":");
        if (parts.length != 2) {
            throw new PostBusinessException(PostErrorCode.INVALID_ARGUMENT, "Feed cursor 格式应为 recOffset:csOffset");
        }
        try {
            return new FeedCursor(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        } catch (NumberFormatException ex) {
            throw new PostBusinessException(PostErrorCode.INVALID_ARGUMENT, "Feed cursor 非法");
        }
    }

    public int getRecOffset() {
        return recOffset;
    }

    public int getCsOffset() {
        return csOffset;
    }

    public FeedCursor advance(int recDelta, int csDelta) {
        return new FeedCursor(recOffset + recDelta, csOffset + csDelta);
    }

    public String encode() {
        return recOffset + ":" + csOffset;
    }
}
