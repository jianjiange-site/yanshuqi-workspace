package com.dating.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.OffsetDateTime;

/**
 * 用户登录凭证表实体，对应 user_center.user_auth_identities。
 */
@TableName("user_auth_identities")
public class UserAuthIdentityEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long authId;

    private Long userId;

    private String identityType;

    private String identityValue;

    private String identityHash;

    private String passwordHash;

    private Integer verified;

    private OffsetDateTime verifiedAt;

    private OffsetDateTime lastLoginAt;

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
     * 获取登录凭证业务主键。
     *
     * @return 登录凭证业务主键
     */
    public Long getAuthId() {
        return authId;
    }

    /**
     * 设置登录凭证业务主键。
     *
     * @param authId 登录凭证业务主键
     */
    public void setAuthId(Long authId) {
        this.authId = authId;
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
     * 获取凭证类型。
     *
     * @return 凭证类型
     */
    public String getIdentityType() {
        return identityType;
    }

    /**
     * 设置凭证类型。
     *
     * @param identityType 凭证类型
     */
    public void setIdentityType(String identityType) {
        this.identityType = identityType;
    }

    /**
     * 获取凭证明文或脱敏值。
     *
     * @return 凭证明文或脱敏值
     */
    public String getIdentityValue() {
        return identityValue;
    }

    /**
     * 设置凭证明文或脱敏值。
     *
     * @param identityValue 凭证明文或脱敏值
     */
    public void setIdentityValue(String identityValue) {
        this.identityValue = identityValue;
    }

    /**
     * 获取凭证归一化哈希值。
     *
     * @return 凭证哈希值
     */
    public String getIdentityHash() {
        return identityHash;
    }

    /**
     * 设置凭证归一化哈希值。
     *
     * @param identityHash 凭证哈希值
     */
    public void setIdentityHash(String identityHash) {
        this.identityHash = identityHash;
    }

    /**
     * 获取密码哈希。
     *
     * @return 密码哈希
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * 设置密码哈希。
     *
     * @param passwordHash 密码哈希
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * 获取凭证是否已验证。
     *
     * @return 0=未验证，1=已验证
     */
    public Integer getVerified() {
        return verified;
    }

    /**
     * 设置凭证是否已验证。
     *
     * @param verified 0=未验证，1=已验证
     */
    public void setVerified(Integer verified) {
        this.verified = verified;
    }

    /**
     * 获取凭证验证时间。
     *
     * @return 凭证验证时间
     */
    public OffsetDateTime getVerifiedAt() {
        return verifiedAt;
    }

    /**
     * 设置凭证验证时间。
     *
     * @param verifiedAt 凭证验证时间
     */
    public void setVerifiedAt(OffsetDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    /**
     * 获取该凭证最近一次登录时间。
     *
     * @return 最近一次登录时间
     */
    public OffsetDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    /**
     * 设置该凭证最近一次登录时间。
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
