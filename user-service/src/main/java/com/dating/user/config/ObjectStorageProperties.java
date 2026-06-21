package com.dating.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 对象存储模式与 bucket 配置。
 */
@ConfigurationProperties(prefix = "object.storage")
public class ObjectStorageProperties {

    /** mock：dev/test 可测试；minio：生产 MinIO 实现。 */
    private String mode = "mock";

    private String bucket = "dating-yanshuqi";

    private String publicBaseUrl = "";

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getPublicBaseUrl() {
        return publicBaseUrl;
    }

    public void setPublicBaseUrl(String publicBaseUrl) {
        this.publicBaseUrl = publicBaseUrl;
    }
}
