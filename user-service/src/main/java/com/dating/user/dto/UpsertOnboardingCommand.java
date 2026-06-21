package com.dating.user.dto;

/**
 * Onboarding 补齐资料命令。
 */
public class UpsertOnboardingCommand {

    private Long userId;

    private String nickname;

    private String gender;

    private String birthday;

    private Integer age;

    private Integer height;

    private String bio;

    private String occupation;

    private String education;

    private String location;

    private String defaultAvatarObjectKey;

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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
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
