package com.dating.match.dto;

/**
 * 当日配额已用量。
 */
public class QuotaUsage {

    private int cardsUsed;
    private int rightSwipeUsed;
    private int superHiUsed;

    public int getCardsUsed() {
        return cardsUsed;
    }

    public void setCardsUsed(int cardsUsed) {
        this.cardsUsed = cardsUsed;
    }

    public int getRightSwipeUsed() {
        return rightSwipeUsed;
    }

    public void setRightSwipeUsed(int rightSwipeUsed) {
        this.rightSwipeUsed = rightSwipeUsed;
    }

    public int getSuperHiUsed() {
        return superHiUsed;
    }

    public void setSuperHiUsed(int superHiUsed) {
        this.superHiUsed = superHiUsed;
    }
}
