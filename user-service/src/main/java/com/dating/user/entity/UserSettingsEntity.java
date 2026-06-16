package com.dating.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.OffsetDateTime;

/**
 * 用户设置表实体，对应 user_center.user_settings。
 */
@TableName("user_settings")
public class UserSettingsEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long settingId;

    private Long userId;

    private Integer discoverable;

    private String preferredGender;

    private Integer preferredAgeMin;

    private Integer preferredAgeMax;

    /** 通知设置 JSON 字符串。 */
    private String notificationSettings;

    /** 隐私设置 JSON 字符串。 */
    private String privacySettings;

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
     * 获取设置业务主键。
     *
     * @return 设置业务主键
     */
    public Long getSettingId() {
        return settingId;
    }

    /**
     * 设置设置业务主键。
     *
     * @param settingId 设置业务主键
     */
    public void setSettingId(Long settingId) {
        this.settingId = settingId;
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
     * 获取是否可被推荐。
     *
     * @return 0=不可见，1=可见
     */
    public Integer getDiscoverable() {
        return discoverable;
    }

    /**
     * 设置是否可被推荐。
     *
     * @param discoverable 0=不可见，1=可见
     */
    public void setDiscoverable(Integer discoverable) {
        this.discoverable = discoverable;
    }

    /**
     * 获取偏好性别。
     *
     * @return 偏好性别
     */
    public String getPreferredGender() {
        return preferredGender;
    }

    /**
     * 设置偏好性别。
     *
     * @param preferredGender 偏好性别
     */
    public void setPreferredGender(String preferredGender) {
        this.preferredGender = preferredGender;
    }

    /**
     * 获取偏好最小年龄。
     *
     * @return 偏好最小年龄
     */
    public Integer getPreferredAgeMin() {
        return preferredAgeMin;
    }

    /**
     * 设置偏好最小年龄。
     *
     * @param preferredAgeMin 偏好最小年龄
     */
    public void setPreferredAgeMin(Integer preferredAgeMin) {
        this.preferredAgeMin = preferredAgeMin;
    }

    /**
     * 获取偏好最大年龄。
     *
     * @return 偏好最大年龄
     */
    public Integer getPreferredAgeMax() {
        return preferredAgeMax;
    }

    /**
     * 设置偏好最大年龄。
     *
     * @param preferredAgeMax 偏好最大年龄
     */
    public void setPreferredAgeMax(Integer preferredAgeMax) {
        this.preferredAgeMax = preferredAgeMax;
    }

    /**
     * 获取通知设置 JSON。
     *
     * @return 通知设置 JSON
     */
    public String getNotificationSettings() {
        return notificationSettings;
    }

    /**
     * 设置通知设置 JSON。
     *
     * @param notificationSettings 通知设置 JSON
     */
    public void setNotificationSettings(String notificationSettings) {
        this.notificationSettings = notificationSettings;
    }

    /**
     * 获取隐私设置 JSON。
     *
     * @return 隐私设置 JSON
     */
    public String getPrivacySettings() {
        return privacySettings;
    }

    /**
     * 设置隐私设置 JSON。
     *
     * @param privacySettings 隐私设置 JSON
     */
    public void setPrivacySettings(String privacySettings) {
        this.privacySettings = privacySettings;
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
