package com.dating.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "首次完善资料请求")
public class UpsertOnboardingReq {

    private String nickname;

    @Schema(description = "0=未指定，1=男，2=女")
    private Integer gender;

    private String birthday;
    private Integer age;
    private Integer height;
    private String bio;
    private String occupation;
    private String education;
    private String location;
    private String defaultAvatarObjectKey;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
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

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
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

    public String getDefaultAvatarObjectKey() {
        return defaultAvatarObjectKey;
    }

    public void setDefaultAvatarObjectKey(String defaultAvatarObjectKey) {
        this.defaultAvatarObjectKey = defaultAvatarObjectKey;
    }
}
