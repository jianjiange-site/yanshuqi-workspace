package com.dating.user.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * 更新用户资料命令，作为 Service 层入参，不直接使用 gRPC Request。
 */
public class UpdateProfileCommand {

    private Long userId;

    private String nickname;

    private String gender;

    private LocalDate birthDate;

    private String countryCode;

    private String cityCode;

    private List<String> languageCodes;

    private String bio;

    private List<String> interests;

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
}
