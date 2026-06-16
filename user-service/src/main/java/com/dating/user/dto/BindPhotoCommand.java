package com.dating.user.dto;

/**
 * 绑定用户照片命令。
 */
public class BindPhotoCommand {

    private Long userId;

    private String photoType;

    private String objectKey;

    private Integer sortOrder;

    /**
     * 获取用户业务主键。
     *
     * @return 用户业务主键
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 设置用户业务主键。
     *
     * @param userId 用户业务主键
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 获取照片类型。
     *
     * @return 照片类型，如 AVATAR、ALBUM
     */
    public String getPhotoType() {
        return photoType;
    }

    /**
     * 设置照片类型。
     *
     * @param photoType 照片类型
     */
    public void setPhotoType(String photoType) {
        this.photoType = photoType;
    }

    /**
     * 获取 object key。
     *
     * @return MinIO object key，非完整 URL
     */
    public String getObjectKey() {
        return objectKey;
    }

    /**
     * 设置 object key。
     *
     * @param objectKey MinIO object key
     */
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    /**
     * 获取排序值。
     *
     * @return 排序值，越小越靠前
     */
    public Integer getSortOrder() {
        return sortOrder;
    }

    /**
     * 设置排序值。
     *
     * @param sortOrder 排序值
     */
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
