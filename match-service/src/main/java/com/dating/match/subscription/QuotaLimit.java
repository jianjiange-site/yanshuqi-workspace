package com.dating.match.subscription;

/**
 * 每日配额上限。
 */
public class QuotaLimit {

    private final int dailyRightSwipeLimit;
    private final int dailyCardLimit;
    private final int dailySuperHiLimit;

    public QuotaLimit(int dailyRightSwipeLimit, int dailyCardLimit, int dailySuperHiLimit) {
        this.dailyRightSwipeLimit = dailyRightSwipeLimit;
        this.dailyCardLimit = dailyCardLimit;
        this.dailySuperHiLimit = dailySuperHiLimit;
    }

    public int getDailyRightSwipeLimit() {
        return dailyRightSwipeLimit;
    }

    public int getDailyCardLimit() {
        return dailyCardLimit;
    }

    public int getDailySuperHiLimit() {
        return dailySuperHiLimit;
    }
}
