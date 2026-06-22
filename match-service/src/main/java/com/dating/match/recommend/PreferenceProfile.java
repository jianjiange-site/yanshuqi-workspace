package com.dating.match.recommend;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户偏好画像，基于最近 30 天 RIGHT / SUPER_HI 行为聚合。
 */
public class PreferenceProfile {

    private int sampleCount;
    private double ageMean;
    private double ageStd;
    private double beautyMean;
    private double beautyStd;
    private Map<String, Double> raceDist = new HashMap<>();
    private double dhRatio;
    private double bhRatio;
    private boolean hasEnoughSamples;

    public static PreferenceProfile empty() {
        PreferenceProfile profile = new PreferenceProfile();
        profile.setSampleCount(0);
        profile.setAgeMean(25);
        profile.setAgeStd(1);
        profile.setBeautyMean(70);
        profile.setBeautyStd(1);
        profile.setRaceDist(Collections.emptyMap());
        profile.setDhRatio(0.5);
        profile.setBhRatio(0.5);
        profile.setHasEnoughSamples(false);
        return profile;
    }

    public int getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(int sampleCount) {
        this.sampleCount = sampleCount;
    }

    public double getAgeMean() {
        return ageMean;
    }

    public void setAgeMean(double ageMean) {
        this.ageMean = ageMean;
    }

    public double getAgeStd() {
        return ageStd;
    }

    public void setAgeStd(double ageStd) {
        this.ageStd = ageStd;
    }

    public double getBeautyMean() {
        return beautyMean;
    }

    public void setBeautyMean(double beautyMean) {
        this.beautyMean = beautyMean;
    }

    public double getBeautyStd() {
        return beautyStd;
    }

    public void setBeautyStd(double beautyStd) {
        this.beautyStd = beautyStd;
    }

    public Map<String, Double> getRaceDist() {
        return raceDist;
    }

    public void setRaceDist(Map<String, Double> raceDist) {
        this.raceDist = raceDist != null ? raceDist : new HashMap<>();
    }

    public double getDhRatio() {
        return dhRatio;
    }

    public void setDhRatio(double dhRatio) {
        this.dhRatio = dhRatio;
    }

    public double getBhRatio() {
        return bhRatio;
    }

    public void setBhRatio(double bhRatio) {
        this.bhRatio = bhRatio;
    }

    public boolean isHasEnoughSamples() {
        return hasEnoughSamples;
    }

    public void setHasEnoughSamples(boolean hasEnoughSamples) {
        this.hasEnoughSamples = hasEnoughSamples;
    }
}
