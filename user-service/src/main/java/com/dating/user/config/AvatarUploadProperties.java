package com.dating.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 头像上传业务配置。
 */
@ConfigurationProperties(prefix = "avatar.upload")
public class AvatarUploadProperties {

    private String allowedExt = "jpg,jpeg,png,webp";

    private long maxSizeBytes = 10_485_760L;

    private int presignExpireSeconds = 600;

    private String objectKeyPrefix = "avatar";

    public String getAllowedExt() {
        return allowedExt;
    }

    public void setAllowedExt(String allowedExt) {
        this.allowedExt = allowedExt;
    }

    public long getMaxSizeBytes() {
        return maxSizeBytes;
    }

    public void setMaxSizeBytes(long maxSizeBytes) {
        this.maxSizeBytes = maxSizeBytes;
    }

    public int getPresignExpireSeconds() {
        return presignExpireSeconds;
    }

    public void setPresignExpireSeconds(int presignExpireSeconds) {
        this.presignExpireSeconds = presignExpireSeconds;
    }

    public String getObjectKeyPrefix() {
        return objectKeyPrefix;
    }

    public void setObjectKeyPrefix(String objectKeyPrefix) {
        this.objectKeyPrefix = objectKeyPrefix;
    }
}
