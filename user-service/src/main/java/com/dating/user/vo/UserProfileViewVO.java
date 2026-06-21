package com.dating.user.vo;

import java.util.List;

/**
 * Swagger UserProfileVO 风格资料视图。
 */
public class UserProfileViewVO {

    private Long userId;

    private String nickname;

    private Integer age;

    private String gender;

    private Integer height;

    private String bio;

    private String occupation;

    private String education;

    private String location;

    private String birthday;

    private AvatarViewVO avatar;

    private List<String> interests;

    private boolean pending;

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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
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

    public AvatarViewVO getAvatar() {
        return avatar;
    }

    public void setAvatar(AvatarViewVO avatar) {
        this.avatar = avatar;
    }

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests;
    }

    public boolean isPending() {
        return pending;
    }

    public void setPending(boolean pending) {
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
