package com.dating.match.dto;

/**
 * Swipe 业务结果，matchId 为 match.biz_id。
 */
public class SwipeResult {

    private long matchId;

    public SwipeResult(long matchId) {
        this.matchId = matchId;
    }

    public long getMatchId() {
        return matchId;
    }
}
