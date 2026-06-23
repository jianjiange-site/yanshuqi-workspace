package com.dating.gateway.dto.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "评论")
public class CommentVO {

    private Long commentId;
    private Long postId;
    private Long userId;
    private String content;
    private Long createdAtSeconds;

    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getCreatedAtSeconds() {
        return createdAtSeconds;
    }

    public void setCreatedAtSeconds(Long createdAtSeconds) {
        this.createdAtSeconds = createdAtSeconds;
    }
}
