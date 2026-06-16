package com.dating.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.OffsetDateTime;

/**
 * 用户设备表实体，对应 user_center.user_devices。
 */
@TableName("user_devices")
public class UserDeviceEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long deviceId;

    private Long userId;

    private String platform;

    private String deviceFingerprint;

    private String pushTokenHash;

    private String appVersion;

    private OffsetDateTime lastSeenAt;

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
     * 获取设备业务主键。
     *
     * @return 设备业务主键
     */
    public Long getDeviceId() {
        return deviceId;
    }

    /**
     * 设置设备业务主键。
     *
     * @param deviceId 设备业务主键
     */
    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
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
     * 获取平台类型。
     *
     * @return 平台类型
     */
    public String getPlatform() {
        return platform;
    }

    /**
     * 设置平台类型。
     *
     * @param platform 平台类型
     */
    public void setPlatform(String platform) {
        this.platform = platform;
    }

    /**
     * 获取设备指纹。
     *
     * @return 设备指纹
     */
    public String getDeviceFingerprint() {
        return deviceFingerprint;
    }

    /**
     * 设置设备指纹。
     *
     * @param deviceFingerprint 设备指纹
     */
    public void setDeviceFingerprint(String deviceFingerprint) {
        this.deviceFingerprint = deviceFingerprint;
    }

    /**
     * 获取推送 token 哈希。
     *
     * @return 推送 token 哈希
     */
    public String getPushTokenHash() {
        return pushTokenHash;
    }

    /**
     * 设置推送 token 哈希。
     *
     * @param pushTokenHash 推送 token 哈希
     */
    public void setPushTokenHash(String pushTokenHash) {
        this.pushTokenHash = pushTokenHash;
    }

    /**
     * 获取 App 版本。
     *
     * @return App 版本
     */
    public String getAppVersion() {
        return appVersion;
    }

    /**
     * 设置 App 版本。
     *
     * @param appVersion App 版本
     */
    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    /**
     * 获取最近活跃时间。
     *
     * @return 最近活跃时间
     */
    public OffsetDateTime getLastSeenAt() {
        return lastSeenAt;
    }

    /**
     * 设置最近活跃时间。
     *
     * @param lastSeenAt 最近活跃时间
     */
    public void setLastSeenAt(OffsetDateTime lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
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
