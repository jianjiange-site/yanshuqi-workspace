package com.dating.user.vo;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * 用户资料详情 VO，对外返回对象，不包含敏感字段。
 */
public class UserProfileDetailVO {

    private Long userId;

    private String userType;

    private String accountStatus;

    private String profileStatus;

    private String nickname;

    private String gender;

    private LocalDate birthDate;

    private String countryCode;

    private String cityCode;

    private List<String> languageCodes;

    private String bio;

    private String avatarKey;

    private List<String> interests;

    private Integer profileScore;

    private Integer profileCompleted;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

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
     * 获取国家或地区编码。
     *
     * @return 国家或地区编码
     */
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * 设置国家或地区编码。
     *
     * @param countryCode 国家或地区编码
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
     * 获取语言列表。
     *
     * @return 语言列表
     */
    public List<String> getLanguageCodes() {
        return languageCodes;
    }

    /**
     * 设置语言列表。
     *
     * @param languageCodes 语言列表
     */
    public void setLanguageCodes(List<String> languageCodes) {
        this.languageCodes = languageCodes;
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
     * 获取头像 object key。
     *
     * @return 头像 object key
     */
    public String getAvatarKey() {
        return avatarKey;
    }

    /**
     * 设置头像 object key。
     *
     * @param avatarKey 头像 object key
     */
    public void setAvatarKey(String avatarKey) {
        this.avatarKey = avatarKey;
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
     * 获取资料完整度分数。
     *
     * @return 资料完整度分数
     */
    public Integer getProfileScore() {
        return profileScore;
    }

    /**
     * 设置资料完整度分数。
     *
     * @param profileScore 资料完整度分数
     */
    public void setProfileScore(Integer profileScore) {
        this.profileScore = profileScore;
    }

    /**
     * 获取资料是否完整标记。
     *
     * @return 0=未完成，1=已完成
     */
    public Integer getProfileCompleted() {
        return profileCompleted;
    }

    /**
     * 设置资料是否完整标记。
     *
     * @param profileCompleted 0=未完成，1=已完成
     */
    public void setProfileCompleted(Integer profileCompleted) {
        this.profileCompleted = profileCompleted;
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
}
