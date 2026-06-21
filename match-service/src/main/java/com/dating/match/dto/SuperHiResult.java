package com.dating.match.dto;

/**
 * SuperHi 业务结果，本阶段 matchId 恒为 0。
 */
public class SuperHiResult {

    private final long matchId;
    private final int coinsUsed;

    public SuperHiResult(long matchId, int coinsUsed) {
        this.matchId = matchId;
        this.coinsUsed = coinsUsed;
    }

    public long getMatchId() {
        return matchId;
    }

    public int getCoinsUsed() {
        return coinsUsed;
    }
}
