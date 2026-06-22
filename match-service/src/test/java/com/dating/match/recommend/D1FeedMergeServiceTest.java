package com.dating.match.recommend;

import com.dating.match.config.MatchProperties;
import com.dating.match.constant.UserTypeConstant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class D1FeedMergeServiceTest {

    private D1FeedMergeService d1FeedMergeService;
    private MatchProperties matchProperties;

    @BeforeEach
    void setUp() {
        matchProperties = new MatchProperties();
        matchProperties.getD1().setBhRatio(0.40);
        matchProperties.getD1().setPreferenceEnabled(true);
        matchProperties.getD1().setPreferenceOffset(0.20);
        d1FeedMergeService = new D1FeedMergeService(new FeedMergeService(), matchProperties);
    }

    @Test
    void merge_shouldUseDefaultBhRatio040() {
        PreferenceProfile pref = PreferenceProfile.empty();
        pref.setHasEnoughSamples(false);
        List<FeedQueueItem> merged = d1FeedMergeService.merge(pref, profiles(UserTypeConstant.BH, 200, 200),
                profiles(UserTypeConstant.DH, 300, 300), 240);
        long bhCount = merged.stream().filter(i -> i.getTargetUserType() == UserTypeConstant.BH).count();
        assertEquals(96, bhCount);
        assertEquals(240, merged.size());
    }

    @Test
    void merge_shouldIncreaseBhRatioWhenUserPrefersBh() {
        PreferenceProfile pref = new PreferenceProfile();
        pref.setHasEnoughSamples(true);
        pref.setDhRatio(0.0);
        double ratio = d1FeedMergeService.computeFinalBhRatio(pref);
        assertEquals(0.60, ratio, 0.001);
    }

    @Test
    void merge_shouldDecreaseBhRatioWhenUserPrefersDh() {
        PreferenceProfile pref = new PreferenceProfile();
        pref.setHasEnoughSamples(true);
        pref.setDhRatio(1.0);
        double ratio = d1FeedMergeService.computeFinalBhRatio(pref);
        assertEquals(0.20, ratio, 0.001);
    }

    @Test
    void merge_shouldFillWithDhWhenBhInsufficient() {
        PreferenceProfile pref = PreferenceProfile.empty();
        List<FeedQueueItem> merged = d1FeedMergeService.merge(pref,
                profiles(UserTypeConstant.BH, 10, 10),
                profiles(UserTypeConstant.DH, 300, 300), 240);
        assertEquals(10, merged.stream().filter(i -> i.getTargetUserType() == UserTypeConstant.BH).count());
        assertEquals(240, merged.size());
    }

    @Test
    void merge_shouldCapAt240() {
        PreferenceProfile pref = PreferenceProfile.empty();
        List<FeedQueueItem> merged = d1FeedMergeService.merge(pref,
                profiles(UserTypeConstant.BH, 300, 300),
                profiles(UserTypeConstant.DH, 300, 300), 240);
        assertEquals(240, merged.size());
    }

    private static List<CandidateProfile> profiles(int userType, int count, long baseId) {
        List<CandidateProfile> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            CandidateProfile p = new CandidateProfile();
            p.setUserId(baseId + i);
            p.setUserType(userType);
            list.add(p);
        }
        return list;
    }
}
