package com.dating.post.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Feed 推荐返回结果。
 */
public class FeedResult {

    private List<PostInfoDTO> items = new ArrayList<>();
    private String nextCursor = "";
    private boolean hasMore;

    public List<PostInfoDTO> getItems() {
        return items;
    }

    public void setItems(List<PostInfoDTO> items) {
        this.items = items;
    }

    public String getNextCursor() {
        return nextCursor;
    }

    public void setNextCursor(String nextCursor) {
        this.nextCursor = nextCursor;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }
}
