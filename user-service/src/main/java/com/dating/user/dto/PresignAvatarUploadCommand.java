package com.dating.user.dto;

/**
 * 头像 presign 命令。
 */
public class PresignAvatarUploadCommand {

    private Long userId;

    private String ext;

    private Long expectedSizeBytes;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public Long getExpectedSizeBytes() {
        return expectedSizeBytes;
    }

    public void setExpectedSizeBytes(Long expectedSizeBytes) {
        this.expectedSizeBytes = expectedSizeBytes;
    }
}
