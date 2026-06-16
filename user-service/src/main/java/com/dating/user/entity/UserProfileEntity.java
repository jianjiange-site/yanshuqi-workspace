package com.dating.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * 用户资料表实体，对应 user_center.user_profiles。
 */
@TableName("user_profiles")
public class UserProfileEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long profileId;

    private Long userId;

    private String nickname;

    private String gender;

    private LocalDate birthDate;

    private String countryCode;

    private String cityCode;

    /** 用户语言列表，JSON 数组字符串。 */
    private String languageCodes;

    private String bio;

    private String avatarKey;

    /** 兴趣标签，JSON 数组字符串。 */
    private String interests;

    private Integer profileScore;

    private Integer profileCompleted;

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
     * 获取资料业务主键。
     *
     * @return 资料业务主键
     */
    public Long getProfileId() {
        return profileId;
    }

    /**
     * 设置资料业务主键。
     *
     * @param profileId 资料业务主键
     */
    public void setProfileId(Long profileId) {
        this.profileId = profileId;
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
     * 获取用户昵称。
     *
     * @return 用户昵称
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * 设置用户昵称。
     *
     * @param nickname 用户昵称
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 获取用户性别。
     *
     * @return 用户性别
     */
    public String getGender() {
        return gender;
    }

    /**
     * 设置用户性别。
     *
     * @param gender 用户性别
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
     * 获取用户语言列表 JSON。
     *
     * @return 用户语言列表 JSON
     */
    public String getLanguageCodes() {
        return languageCodes;
    }

    /**
     * 设置用户语言列表 JSON。
     *
     * @param languageCodes 用户语言列表 JSON
     */
    public void setLanguageCodes(String languageCodes) {
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
     * 获取兴趣标签 JSON。
     *
     * @return 兴趣标签 JSON
     */
    public String getInterests() {
        return interests;
    }

    /**
     * 设置兴趣标签 JSON。
     *
     * @param interests 兴趣标签 JSON
     */
    public void setInterests(String interests) {
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
     * 获取资料是否完整。
     *
     * @return 0=未完成，1=已完成
     */
    public Integer getProfileCompleted() {
        return profileCompleted;
    }

    /**
     * 设置资料是否完整。
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
