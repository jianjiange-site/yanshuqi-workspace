package com.dating.user.dto;

/**
 * 头像 confirm 命令。
 */
public class ConfirmAvatarUploadCommand {

    private Long userId;

    private String objectKey;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }
}
