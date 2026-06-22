package com.dating.post.dto;

/**
 * Feed 混排候选项，记录 postId 与来源池。
 */
public class FeedCandidate {

    public enum Source {
        RECOMMEND,
        TIMELINE,
        COLD_START
    }

    private final long postId;
    private final Source source;

    public FeedCandidate(long postId, Source source) {
        this.postId = postId;
        this.source = source;
    }

    public long getPostId() {
        return postId;
    }

    public Source getSource() {
        return source;
    }
}
