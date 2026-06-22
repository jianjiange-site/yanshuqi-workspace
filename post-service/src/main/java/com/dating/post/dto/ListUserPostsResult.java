package com.dating.post.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户帖子列表查询结果。
 */
public class ListUserPostsResult {

    private List<PostInfoDTO> items = new ArrayList<>();
    private String nextCursor;
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
