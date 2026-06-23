package com.dating.gateway.dto.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "帖子列表项")
public class PostVO {

    private Long postId;
    private Long userId;
    private String content;
    private List<String> imageKeys;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean isLiked;
    private Long createdAtSeconds;

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

    public List<String> getImageKeys() {
        return imageKeys;
    }

    public void setImageKeys(List<String> imageKeys) {
        this.imageKeys = imageKeys;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public Boolean getIsLiked() {
        return isLiked;
    }

    public void setIsLiked(Boolean isLiked) {
        this.isLiked = isLiked;
    }

    public Long getCreatedAtSeconds() {
        return createdAtSeconds;
    }

    public void setCreatedAtSeconds(Long createdAtSeconds) {
        this.createdAtSeconds = createdAtSeconds;
    }
}
