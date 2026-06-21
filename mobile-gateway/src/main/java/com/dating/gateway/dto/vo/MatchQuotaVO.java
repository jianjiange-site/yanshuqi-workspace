package com.dating.gateway.dto.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "当日配额")
public class MatchQuotaVO {

    private String tier;
    private Integer dailyRightSwipeLimit;
    private Integer dailyRightSwipeUsed;
    private Integer dailyCardLimit;
    private Integer dailyCardUsed;
    private Integer dailySuperHiLimit;
    private Integer dailySuperHiUsed;
    private Integer superHiCoinPrice;

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public Integer getDailyRightSwipeLimit() {
        return dailyRightSwipeLimit;
    }

    public void setDailyRightSwipeLimit(Integer dailyRightSwipeLimit) {
        this.dailyRightSwipeLimit = dailyRightSwipeLimit;
    }

    public Integer getDailyRightSwipeUsed() {
        return dailyRightSwipeUsed;
    }

    public void setDailyRightSwipeUsed(Integer dailyRightSwipeUsed) {
        this.dailyRightSwipeUsed = dailyRightSwipeUsed;
    }

    public Integer getDailyCardLimit() {
        return dailyCardLimit;
    }

    public void setDailyCardLimit(Integer dailyCardLimit) {
        this.dailyCardLimit = dailyCardLimit;
    }

    public Integer getDailyCardUsed() {
        return dailyCardUsed;
    }

    public void setDailyCardUsed(Integer dailyCardUsed) {
        this.dailyCardUsed = dailyCardUsed;
    }

    public Integer getDailySuperHiLimit() {
        return dailySuperHiLimit;
    }

    public void setDailySuperHiLimit(Integer dailySuperHiLimit) {
        this.dailySuperHiLimit = dailySuperHiLimit;
    }

    public Integer getDailySuperHiUsed() {
        return dailySuperHiUsed;
    }

    public void setDailySuperHiUsed(Integer dailySuperHiUsed) {
        this.dailySuperHiUsed = dailySuperHiUsed;
    }

    public Integer getSuperHiCoinPrice() {
        return superHiCoinPrice;
    }

    public void setSuperHiCoinPrice(Integer superHiCoinPrice) {
        this.superHiCoinPrice = superHiCoinPrice;
    }
}
