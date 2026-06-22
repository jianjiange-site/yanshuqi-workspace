package com.dating.match.recommend;

import com.dating.match.config.MatchProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * D1 混排：默认 bhRatio=0.40，支持偏好偏移；BH 不足时 DH 补齐。
 */
@Service
@Profile("!test")
public class D1FeedMergeService {

    private final FeedMergeService feedMergeService;
    private final MatchProperties matchProperties;

    public D1FeedMergeService(FeedMergeService feedMergeService, MatchProperties matchProperties) {
        this.feedMergeService = feedMergeService;
        this.matchProperties = matchProperties;
    }

    public List<FeedQueueItem> merge(PreferenceProfile preference,
                                     List<CandidateProfile> rankedBh,
                                     List<CandidateProfile> rankedDh,
                                     int queueSize) {
        double finalBhRatio = computeFinalBhRatio(preference);
        return feedMergeService.merge(rankedBh, rankedDh, queueSize, finalBhRatio);
    }

    double computeFinalBhRatio(PreferenceProfile preference) {
        MatchProperties.D1Properties d1 = matchProperties.getD1();
        double offset = 0;
        if (d1.isPreferenceEnabled() && preference.isHasEnoughSamples()) {
            offset = (0.5 - preference.getDhRatio()) * d1.getPreferenceOffset() * 2;
        }
        return clamp(d1.getBhRatio() + offset, 0, 1);
    }

    static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
