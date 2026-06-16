package com.dating.user.dto;

/**
 * 查询用户照片列表查询条件。
 */
public class ListUserPhotosQuery {

    private Long userId;

    private String photoType;

    private boolean includeDisabled;

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
     * 获取照片类型过滤条件。
     *
     * @return 照片类型，可为 null 表示不过滤
     */
    public String getPhotoType() {
        return photoType;
    }

    /**
     * 设置照片类型过滤条件。
     *
     * @param photoType 照片类型
     */
    public void setPhotoType(String photoType) {
        this.photoType = photoType;
    }

    /**
     * 是否包含已禁用照片。
     *
     * @return true 表示包含 enabled=0 的记录
     */
    public boolean isIncludeDisabled() {
        return includeDisabled;
    }

    /**
     * 设置是否包含已禁用照片。
     *
     * @param includeDisabled 是否包含已禁用照片
     */
    public void setIncludeDisabled(boolean includeDisabled) {
        this.includeDisabled = includeDisabled;
    }
}
