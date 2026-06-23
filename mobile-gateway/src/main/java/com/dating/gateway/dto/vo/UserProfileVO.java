package com.dating.gateway.dto.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "用户资料视图")
public class UserProfileVO {

    private Long userId;
    private String nickname;
    private Integer age;

    @Schema(description = "0=未指定，1=男，2=女")
    private Integer gender;

    private Integer height;
    private String bio;
    private String occupation;
    private String education;
    private String location;
    private String birthday;
    private AvatarVO avatar;
    private List<String> interests;
    private Boolean pending;
    private Integer regulationStatus;
    private Long lastOpenAtMs;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
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

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public AvatarVO getAvatar() {
        return avatar;
    }

    public void setAvatar(AvatarVO avatar) {
        this.avatar = avatar;
    }

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests;
    }

    public Boolean getPending() {
        return pending;
    }

    public void setPending(Boolean pending) {
        this.pending = pending;
    }

    public Integer getRegulationStatus() {
        return regulationStatus;
    }

    public void setRegulationStatus(Integer regulationStatus) {
        this.regulationStatus = regulationStatus;
    }

    public Long getLastOpenAtMs() {
        return lastOpenAtMs;
    }

    public void setLastOpenAtMs(Long lastOpenAtMs) {
        this.lastOpenAtMs = lastOpenAtMs;
    }
}
