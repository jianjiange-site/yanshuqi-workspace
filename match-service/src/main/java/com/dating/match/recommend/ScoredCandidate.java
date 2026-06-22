package com.dating.match.recommend;

/**
 * 打分后的候选用户。
 */
public class ScoredCandidate {

    private final CandidateProfile profile;
    private final double score;

    public ScoredCandidate(CandidateProfile profile, double score) {
        this.profile = profile;
        this.score = score;
    }

    public CandidateProfile getProfile() {
        return profile;
    }

    public double getScore() {
        return score;
    }
}
