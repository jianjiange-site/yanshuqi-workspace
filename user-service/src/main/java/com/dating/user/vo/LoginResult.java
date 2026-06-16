package com.dating.user.vo;

import java.time.OffsetDateTime;

/**
 * 用户登录校验结果，对外返回对象，不包含敏感字段与 JWT。
 */
public class LoginResult {

    private Long userId;

    private String accountStatus;

    private String profileStatus;

    private Integer tokenVersion;

    private OffsetDateTime lastLoginAt;

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
     * @return 最近一次登录时间，UTC
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
}
