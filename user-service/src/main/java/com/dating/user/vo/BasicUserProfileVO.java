package com.dating.user.vo;

/**
 * 用户基础资料 VO，供下游批量展示。
 */
public class BasicUserProfileVO {

    private Long userId;

    private String nickname;

    private String gender;

    private String cityCode;

    private String avatarKey;

    private String profileStatus;

    private String accountStatus;

    private boolean available;

    private String unavailableReason;

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
     * 获取昵称。
     *
     * @return 昵称
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * 设置昵称。
     *
     * @param nickname 昵称
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 获取性别。
     *
     * @return 性别
     */
    public String getGender() {
        return gender;
    }

    /**
     * 设置性别。
     *
     * @param gender 性别
     */
    public void setGender(String gender) {
        this.gender = gender;
    }

    /**
     * 获取城市编码。
     *
     * @return 城市编码
     */
    public String getCityCode() {
        return cityCode;
    }

    /**
     * 设置城市编码。
     *
     * @param cityCode 城市编码
     */
    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    /**
     * 获取外显头像 object key，仅 APPROVED 头像。
     *
     * @return 头像 object key
     */
    public String getAvatarKey() {
        return avatarKey;
    }

    /**
     * 设置外显头像 object key。
     *
     * @param avatarKey 头像 object key
     */
    public void setAvatarKey(String avatarKey) {
        this.avatarKey = avatarKey;
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
     * 是否可用。
     *
     * @return true 表示可用
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * 设置是否可用。
     *
     * @param available 是否可用
     */
    public void setAvailable(boolean available) {
        this.available = available;
    }

    /**
     * 获取不可用原因码。
     *
     * @return 不可用原因，可用时为空
     */
    public String getUnavailableReason() {
        return unavailableReason;
    }

    /**
     * 设置不可用原因码。
     *
     * @param unavailableReason 不可用原因
     */
    public void setUnavailableReason(String unavailableReason) {
        this.unavailableReason = unavailableReason;
    }
}
