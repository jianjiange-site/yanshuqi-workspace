package com.dating.user.service.storage;

/**
 * presign PUT 结果。
 */
public class PresignPutResult {

    private final String presignedUrl;

    private final long expiresAtMs;

    public PresignPutResult(String presignedUrl, long expiresAtMs) {
        this.presignedUrl = presignedUrl;
        this.expiresAtMs = expiresAtMs;
    }

    public String getPresignedUrl() {
        return presignedUrl;
    }

    public long getExpiresAtMs() {
        return expiresAtMs;
    }
}
