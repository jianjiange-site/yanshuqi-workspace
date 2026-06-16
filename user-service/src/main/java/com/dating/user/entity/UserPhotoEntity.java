package com.dating.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.OffsetDateTime;

/**
 * 用户照片表实体，对应 user_center.user_photos。
 */
@TableName("user_photos")
public class UserPhotoEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long photoId;

    private Long userId;

    private String photoType;

    private String objectKey;

    private Integer sortOrder;

    private String reviewStatus;

    private Integer enabled;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    @TableLogic
    private Integer deleted;

    /**
     * 获取数据库自增主键。
     *
     * @return 自增主键
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置数据库自增主键。
     *
     * @param id 自增主键
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取照片业务主键。
     *
     * @return 照片业务主键
     */
    public Long getPhotoId() {
        return photoId;
    }

    /**
     * 设置照片业务主键。
     *
     * @param photoId 照片业务主键
     */
    public void setPhotoId(Long photoId) {
        this.photoId = photoId;
    }

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
     * @return 照片类型
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
     * 获取 MinIO object key。
     *
     * @return object key
     */
    public String getObjectKey() {
        return objectKey;
    }

    /**
     * 设置 MinIO object key。
     *
     * @param objectKey object key
     */
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    /**
     * 获取排序值。
     *
     * @return 排序值
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

    /**
     * 获取审核状态。
     *
     * @return 审核状态
     */
    public String getReviewStatus() {
        return reviewStatus;
    }

    /**
     * 设置审核状态。
     *
     * @param reviewStatus 审核状态
     */
    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    /**
     * 获取是否启用。
     *
     * @return 0=禁用，1=启用
     */
    public Integer getEnabled() {
        return enabled;
    }

    /**
     * 设置是否启用。
     *
     * @param enabled 0=禁用，1=启用
     */
    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }

    /**
     * 获取创建时间。
     *
     * @return 创建时间
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 设置创建时间。
     *
     * @param createdAt 创建时间
     */
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 获取更新时间。
     *
     * @return 更新时间
     */
    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 设置更新时间。
     *
     * @param updatedAt 更新时间
     */
    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * 获取逻辑删除标记。
     *
     * @return 逻辑删除标记
     */
    public Integer getDeleted() {
        return deleted;
    }

    /**
     * 设置逻辑删除标记。
     *
     * @param deleted 逻辑删除标记
     */
    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}
