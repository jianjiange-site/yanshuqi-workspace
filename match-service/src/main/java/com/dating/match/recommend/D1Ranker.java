package com.dating.match.recommend;

import com.dating.match.config.MatchProperties;
import com.dating.match.constant.UserTypeConstant;
import com.dating.match.manager.SwipeHistoryManager;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * D1 打分排序：偏好相似度 + beauty + 距离衰减 + 活跃度 + bonus。
 */
@Component
@Profile("!test")
public class D1Ranker {

    private static final double NEUTRAL_SIMILARITY = 0.5D;
    private static final double DH_DISTANCE_DECAY = 0.5D;
    private static final double DH_ACTIVITY_SCORE = 0.5D;
    private static final double DISTANCE_DECAY_DIVISOR = 50.0D;
    private static final double ACTIVITY_DECAY_HOURS = 168.0D;

    private final SwipeHistoryManager swipeHistoryManager;
    private final MatchProperties matchProperties;

    public D1Ranker(SwipeHistoryManager swipeHistoryManager, MatchProperties matchProperties) {
        this.swipeHistoryManager = swipeHistoryManager;
        this.matchProperties = matchProperties;
    }

    public List<CandidateProfile> rank(long userId,
                                       PreferenceProfile preference,
                                       List<CandidateProfile> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }
        List<ScoredCandidate> scored = new ArrayList<>(candidates.size());
        for (CandidateProfile candidate : candidates) {
            scored.add(new ScoredCandidate(candidate, score(userId, preference, candidate)));
        }
        scored.sort(Comparator.comparingDouble(ScoredCandidate::getScore).reversed());
        return scored.stream().map(ScoredCandidate::getProfile).toList();
    }

    double score(long userId, PreferenceProfile preference, CandidateProfile candidate) {
        double preferenceSimilarity = computePreferenceSimilarity(preference, candidate);
        double normalizedBeauty = Math.min(1.0, Math.max(0, candidate.getBeautyScore() / 100.0));
        double distanceDecay = computeDistanceDecay(candidate);
        double activityScore = computeActivityScore(candidate);
        double bonus = computeBonus(userId, candidate);

        return 0.45 * preferenceSimilarity
                + 0.30 * normalizedBeauty
                + 0.15 * distanceDecay
                + 0.10 * activityScore
                + bonus;
    }

    private double computePreferenceSimilarity(PreferenceProfile preference, CandidateProfile candidate) {
        if (!preference.isHasEnoughSamples()) {
            return NEUTRAL_SIMILARITY;
        }
        double ageSim = gaussianSimilarity(candidate.getAge(), preference.getAgeMean(), preference.getAgeStd());
        double beautySim = gaussianSimilarity(candidate.getBeautyScore(), preference.getBeautyMean(),
                preference.getBeautyStd());
        String race = candidate.getRace() == null ? "unknown" : candidate.getRace();
        double raceSim = preference.getRaceDist().getOrDefault(race, 0.0);
        return (ageSim + beautySim + raceSim) / 3.0;
    }

    static double gaussianSimilarity(double value, double mean, double std) {
        double diff = Math.abs(value - mean);
        return Math.exp(-diff / Math.max(std, 1.0));
    }

    private double computeDistanceDecay(CandidateProfile candidate) {
        if (candidate.getUserType() == UserTypeConstant.DH) {
            return DH_DISTANCE_DECAY;
        }
        if (candidate.getDistanceKm() < 0) {
            return 0.5;
        }
        return Math.exp(-candidate.getDistanceKm() / DISTANCE_DECAY_DIVISOR);
    }

    private double computeActivityScore(CandidateProfile candidate) {
        if (candidate.getUserType() == UserTypeConstant.DH) {
            return DH_ACTIVITY_SCORE;
        }
        OffsetDateTime lastActive = candidate.getLastActiveAt();
        if (lastActive == null) {
            return 0.5;
        }
        long hoursSince = ChronoUnit.HOURS.between(lastActive, OffsetDateTime.now(ZoneOffset.UTC));
        return Math.exp(-Math.max(0, hoursSince) / ACTIVITY_DECAY_HOURS);
    }

    private double computeBonus(long userId, CandidateProfile candidate) {
        MatchProperties.ScoreProperties scoreProps = matchProperties.getScore();
        double bonus = 0;
        if (swipeHistoryManager.hasPositiveSwipe(candidate.getUserId(), userId)) {
            bonus += scoreProps.getMutualLikeBonus();
        }
        if (candidate.getUserType() == UserTypeConstant.BH && isNewBh(candidate, scoreProps.getNewBhWindowDays())) {
            bonus += scoreProps.getNewBhBonus();
        }
        return bonus;
    }

    private static boolean isNewBh(CandidateProfile candidate, int windowDays) {
        OffsetDateTime createdAt = candidate.getCreatedAt();
        if (createdAt == null) {
            return false;
        }
        OffsetDateTime cutoff = OffsetDateTime.now(ZoneOffset.UTC).minusDays(windowDays);
        return !createdAt.isBefore(cutoff);
    }
}
