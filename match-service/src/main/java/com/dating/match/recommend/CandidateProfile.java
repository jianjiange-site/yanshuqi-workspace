package com.dating.match.recommend;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 推荐候选用户资料；D0 使用 mock 数据，字段预留供 D1 打分。
 */
public class CandidateProfile {

    private long userId;
    private int userType;
    private String nickname;
    private int age;
    private List<String> photoKeys = new ArrayList<>();
    private String bio;
    private double distanceKm;
    private String stateCode;
    private String city;
    private int gender;
    private double beautyScore;
    private String race;
    private OffsetDateTime createdAt;
    private OffsetDateTime lastActiveAt;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public List<String> getPhotoKeys() {
        return photoKeys;
    }

    public void setPhotoKeys(List<String> photoKeys) {
        this.photoKeys = photoKeys != null ? photoKeys : new ArrayList<>();
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public double getBeautyScore() {
        return beautyScore;
    }

    public void setBeautyScore(double beautyScore) {
        this.beautyScore = beautyScore;
    }

    public String getRace() {
        return race;
    }

    public void setRace(String race) {
        this.race = race;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getLastActiveAt() {
        return lastActiveAt;
    }

    public void setLastActiveAt(OffsetDateTime lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }
}
