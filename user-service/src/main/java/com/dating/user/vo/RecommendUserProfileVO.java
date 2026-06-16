package com.dating.user.vo;

import java.time.LocalDate;
import java.util.List;

/**
 * 用户推荐展示资料 VO，供下游批量展示，不含敏感字段。
 */
public class RecommendUserProfileVO {

    private Long userId;

    private String userType;

    private String gender;

    private LocalDate birthDate;

    private String countryCode;

    private String cityCode;

    private List<String> languageCodes;

    private List<String> interests;

    private String bio;

    private String avatarKey;

    private Integer profileScore;

    private Integer profileCompleted;

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
     * 获取出生日期。
     *
     * @return 出生日期
     */
    public LocalDate getBirthDate() {
        return birthDate;
    }

    /**
     * 设置出生日期。
     *
     * @param birthDate 出生日期
     */
    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    /**
     * 获取国家编码。
     *
     * @return 国家编码
     */
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * 设置国家编码。
     *
     * @param countryCode 国家编码
     */
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
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
     * 获取语言编码列表。
     *
     * @return 语言编码列表
     */
    public List<String> getLanguageCodes() {
        return languageCodes;
    }

    /**
     * 设置语言编码列表。
     *
     * @param languageCodes 语言编码列表
     */
    public void setLanguageCodes(List<String> languageCodes) {
        this.languageCodes = languageCodes;
    }

    /**
     * 获取兴趣标签列表。
     *
     * @return 兴趣标签列表
     */
    public List<String> getInterests() {
        return interests;
    }

    /**
     * 设置兴趣标签列表。
     *
     * @param interests 兴趣标签列表
     */
    public void setInterests(List<String> interests) {
        this.interests = interests;
    }

    /**
     * 获取个人简介。
     *
     * @return 个人简介
     */
    public String getBio() {
        return bio;
    }

    /**
     * 设置个人简介。
     *
     * @param bio 个人简介
     */
    public void setBio(String bio) {
        this.bio = bio;
    }

    /**
     * 获取外显头像 object key。
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
     * 获取资料分数。
     *
     * @return 资料分数
     */
    public Integer getProfileScore() {
        return profileScore;
    }

    /**
     * 设置资料分数。
     *
     * @param profileScore 资料分数
     */
    public void setProfileScore(Integer profileScore) {
        this.profileScore = profileScore;
    }

    /**
     * 获取资料是否完成。
     *
     * @return 0 或 1
     */
    public Integer getProfileCompleted() {
        return profileCompleted;
    }

    /**
     * 设置资料是否完成。
     *
     * @param profileCompleted 资料是否完成
     */
    public void setProfileCompleted(Integer profileCompleted) {
        this.profileCompleted = profileCompleted;
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
     * @return 不可用原因
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
