package com.dating.match.dto;

import com.dating.match.recommend.CandidateProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * GetTodayFeed 业务结果。
 */
public class GetTodayFeedResult {

    private final List<CandidateProfile> cards;
    private final boolean exhausted;

    public GetTodayFeedResult(List<CandidateProfile> cards, boolean exhausted) {
        this.cards = cards == null ? List.of() : new ArrayList<>(cards);
        this.exhausted = exhausted;
    }

    public List<CandidateProfile> getCards() {
        return cards;
    }

    public boolean isExhausted() {
        return exhausted;
    }
}
