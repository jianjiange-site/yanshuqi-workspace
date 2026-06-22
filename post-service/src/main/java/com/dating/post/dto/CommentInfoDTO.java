package com.dating.post.dto;

/**
 * 评论详情 DTO。
 */
public class CommentInfoDTO {

    private long commentId;
    private long postId;
    private long userId;
    private String content;
    private long createdAtSeconds;

    public long getCommentId() {
        return commentId;
    }

    public void setCommentId(long commentId) {
        this.commentId = commentId;
    }

    public long getPostId() {
        return postId;
    }

    public void setPostId(long postId) {
        this.postId = postId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getCreatedAtSeconds() {
        return createdAtSeconds;
    }

    public void setCreatedAtSeconds(long createdAtSeconds) {
        this.createdAtSeconds = createdAtSeconds;
    }
}
