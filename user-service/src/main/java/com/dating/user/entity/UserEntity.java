package com.dating.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.OffsetDateTime;

/**
 * 用户主表实体，对应 user_center.users。
 */
@TableName("users")
public class UserEntity {

    /** 数据库自增主键，仅用于物理存储，不对外暴露。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户业务主键，跨服务引用和接口返回都使用该字段。 */
    private Long userId;

    /** 用户类型：BH=真人用户，DH=数字人用户。 */
    private String userType;

    /** 账号状态：ACTIVE、DISABLED、BANNED、DELETED。 */
    private String accountStatus;

    /** 资料状态：INIT、BASIC_DONE、PHOTO_DONE、COMPLETED、BLOCKED。 */
    private String profileStatus;

    /** 注册来源：PHONE、EMAIL、GOOGLE、APPLE、DEVICE、ADMIN。 */
    private String registerSource;

    /** Token 版本号，用于强制失效历史 token。 */
    private Integer tokenVersion;

    /** 最近一次登录时间，统一 UTC。 */
    private OffsetDateTime lastLoginAt;

    /** 创建时间，统一 UTC。 */
    private OffsetDateTime createdAt;

    /** 更新时间，统一 UTC。 */
    private OffsetDateTime updatedAt;

    /** 逻辑删除标记：0=未删除，1=已删除。 */
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
     * 获取用户类型。
     *
     * @return 用户类型
     */
    public String getUserType() {
        return userType;
    }

    /**
     * 设置用户类型。
     *
     * @param userType 用户类型
     */
    public void setUserType(String userType) {
        this.userType = userType;
    }

    /**
     * 获取账号状态。
     *
     * @return 账号状态
     */
    public String getAccountStatus() {
        return accountStatus;
    }

    /**
     * 设置账号状态。
     *
     * @param accountStatus 账号状态
     */
    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    /**
     * 获取资料状态。
     *
     * @return 资料状态
     */
    public String getProfileStatus() {
        return profileStatus;
    }

    /**
     * 设置资料状态。
     *
     * @param profileStatus 资料状态
     */
    public void setProfileStatus(String profileStatus) {
        this.profileStatus = profileStatus;
    }

    /**
     * 获取注册来源。
     *
     * @return 注册来源
     */
    public String getRegisterSource() {
        return registerSource;
    }

    /**
     * 设置注册来源。
     *
     * @param registerSource 注册来源
     */
    public void setRegisterSource(String registerSource) {
        this.registerSource = registerSource;
    }

    /**
     * 获取 Token 版本号。
     *
     * @return Token 版本号
     */
    public Integer getTokenVersion() {
        return tokenVersion;
    }

    /**
     * 设置 Token 版本号。
     *
     * @param tokenVersion Token 版本号
     */
    public void setTokenVersion(Integer tokenVersion) {
        this.tokenVersion = tokenVersion;
    }

    /**
     * 获取最近一次登录时间。
     *
     * @return 最近一次登录时间
     */
    public OffsetDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    /**
     * 设置最近一次登录时间。
     *
     * @param lastLoginAt 最近一次登录时间
     */
    public void setLastLoginAt(OffsetDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
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
