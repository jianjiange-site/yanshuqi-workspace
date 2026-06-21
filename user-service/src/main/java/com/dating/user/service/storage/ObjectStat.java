package com.dating.user.service.storage;

/**
 * 对象存储 stat 元信息。
 */
public class ObjectStat {

    private final long sizeBytes;

    private final String contentType;

    private final int width;

    private final int height;

    public ObjectStat(long sizeBytes, String contentType, int width, int height) {
        this.sizeBytes = sizeBytes;
        this.contentType = contentType;
        this.width = width;
        this.height = height;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public String getContentType() {
        return contentType;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
