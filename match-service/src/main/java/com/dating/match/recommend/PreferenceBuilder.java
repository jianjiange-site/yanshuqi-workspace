package com.dating.match.recommend;

import com.dating.match.client.CandidateClient;
import com.dating.match.config.MatchProperties;
import com.dating.match.constant.UserTypeConstant;
import com.dating.match.manager.SwipeHistoryManager;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于最近 30 天正向 swipe 历史构建偏好画像。
 */
@Component
@Profile("!test")
public class PreferenceBuilder {

    private static final double MIN_STD = 1.0D;
    private static final int PROFILE_FETCH_LIMIT = 500;

    private final SwipeHistoryManager swipeHistoryManager;
    private final CandidateClient candidateClient;
    private final MatchProperties matchProperties;

    public PreferenceBuilder(SwipeHistoryManager swipeHistoryManager,
                             CandidateClient candidateClient,
                             MatchProperties matchProperties) {
        this.swipeHistoryManager = swipeHistoryManager;
        this.candidateClient = candidateClient;
        this.matchProperties = matchProperties;
    }

    public PreferenceProfile build(long userId) {
        int windowDays = matchProperties.getD1().getPreferenceWindowDays();
        Instant since = Instant.now().minus(windowDays, ChronoUnit.DAYS);
        List<Long> targetIds = swipeHistoryManager.listPositiveTargetIds(userId, since, PROFILE_FETCH_LIMIT);
        if (targetIds.isEmpty()) {
            return PreferenceProfile.empty();
        }

        Map<Long, CandidateProfile> profiles = candidateClient.batchGetProfiles(targetIds);
        if (profiles.isEmpty()) {
            return PreferenceProfile.empty();
        }

        int sampleCount = 0;
        double ageSum = 0;
        double beautySum = 0;
        int dhCount = 0;
        int bhCount = 0;
        Map<String, Integer> raceCounts = new HashMap<>();
        List<Double> ages = new java.util.ArrayList<>();
        List<Double> beauties = new java.util.ArrayList<>();

        for (Long targetId : targetIds) {
            CandidateProfile profile = profiles.get(targetId);
            if (profile == null) {
                continue;
            }
            sampleCount++;
            ageSum += profile.getAge();
            beautySum += profile.getBeautyScore();
            ages.add((double) profile.getAge());
            beauties.add(profile.getBeautyScore());
            if (profile.getUserType() == UserTypeConstant.DH) {
                dhCount++;
            } else {
                bhCount++;
            }
            String race = profile.getRace() == null ? "unknown" : profile.getRace();
            raceCounts.merge(race, 1, Integer::sum);
        }

        if (sampleCount == 0) {
            return PreferenceProfile.empty();
        }

        int minSamples = matchProperties.getD1().getMinPreferenceSamples();
        PreferenceProfile result = new PreferenceProfile();
        result.setSampleCount(sampleCount);
        result.setAgeMean(ageSum / sampleCount);
        result.setBeautyMean(beautySum / sampleCount);
        result.setAgeStd(Math.max(MIN_STD, stdDev(ages, result.getAgeMean())));
        result.setBeautyStd(Math.max(MIN_STD, stdDev(beauties, result.getBeautyMean())));
        result.setDhRatio((double) dhCount / sampleCount);
        result.setBhRatio((double) bhCount / sampleCount);
        result.setRaceDist(toRaceDist(raceCounts, sampleCount));
        result.setHasEnoughSamples(sampleCount >= minSamples);
        return result;
    }

    static double stdDev(List<Double> values, double mean) {
        if (values == null || values.size() <= 1) {
            return MIN_STD;
        }
        double sumSq = 0;
        for (Double value : values) {
            double diff = value - mean;
            sumSq += diff * diff;
        }
        return Math.sqrt(sumSq / values.size());
    }

    static Map<String, Double> toRaceDist(Map<String, Integer> raceCounts, int total) {
        Map<String, Double> dist = new HashMap<>();
        for (Map.Entry<String, Integer> entry : raceCounts.entrySet()) {
            dist.put(entry.getKey(), (double) entry.getValue() / total);
        }
        return dist;
    }
}
