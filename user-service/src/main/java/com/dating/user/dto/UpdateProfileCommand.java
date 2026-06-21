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

    private Integer age;

    private Integer height;

    private String occupation;

    private String education;

    private String location;

    /** 是否携带 age 字段（Swagger 日常更新 merge 语义）。 */
    private boolean agePresent;

    /** 是否携带 height 字段。 */
    private boolean heightPresent;

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

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isAgePresent() {
        return agePresent;
    }

    public void setAgePresent(boolean agePresent) {
        this.agePresent = agePresent;
    }

    public boolean isHeightPresent() {
        return heightPresent;
    }

    public void setHeightPresent(boolean heightPresent) {
        this.heightPresent = heightPresent;
    }
}
