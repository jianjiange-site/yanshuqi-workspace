package com.dating.gateway.dto.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "头像 presign 响应")
public class PresignAvatarUploadVO {

    private String presignedUrl;
    private String objectKey;
    private Long expiresAtMs;

    public String getPresignedUrl() {
        return presignedUrl;
    }

    public void setPresignedUrl(String presignedUrl) {
        this.presignedUrl = presignedUrl;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public Long getExpiresAtMs() {
        return expiresAtMs;
    }

    public void setExpiresAtMs(Long expiresAtMs) {
        this.expiresAtMs = expiresAtMs;
    }
}
