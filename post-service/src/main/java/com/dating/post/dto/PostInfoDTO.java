package com.dating.post.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 帖子详情内部 DTO，供 Service / gRPC / 缓存层复用。
 */
public class PostInfoDTO {

    private long postId;
    private long userId;
    private String content;
    private List<String> imageKeys = new ArrayList<>();
    private int likeCount;
    private int commentCount;
    private boolean liked;
    private long createdAtSeconds;

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

    public List<String> getImageKeys() {
        return imageKeys;
    }

    public void setImageKeys(List<String> imageKeys) {
        this.imageKeys = imageKeys == null ? new ArrayList<>() : imageKeys;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public long getCreatedAtSeconds() {
        return createdAtSeconds;
    }

    public void setCreatedAtSeconds(long createdAtSeconds) {
        this.createdAtSeconds = createdAtSeconds;
    }
}
