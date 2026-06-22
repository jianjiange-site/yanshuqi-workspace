package com.dating.post.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 发帖内部命令对象。
 */
public class PostCreateCommand {

    private long callerUserId;
    private String content;
    private List<String> imageKeys = new ArrayList<>();

    public long getCallerUserId() {
        return callerUserId;
    }

    public void setCallerUserId(long callerUserId) {
        this.callerUserId = callerUserId;
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
}
