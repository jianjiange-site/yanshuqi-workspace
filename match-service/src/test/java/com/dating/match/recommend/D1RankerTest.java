package com.dating.match.recommend;

import com.dating.match.config.MatchProperties;
import com.dating.match.constant.UserTypeConstant;
import com.dating.match.manager.SwipeHistoryManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class D1RankerTest {

    private static final long USER_ID = 10001L;

    @Mock
    private SwipeHistoryManager swipeHistoryManager;

    private MatchProperties matchProperties;
    private D1Ranker d1Ranker;

    @BeforeEach
    void setUp() {
        matchProperties = new MatchProperties();
        d1Ranker = new D1Ranker(swipeHistoryManager, matchProperties);
    }

    @Test
    void score_higherBeautyShouldScoreHigher() {
        PreferenceProfile pref = PreferenceProfile.empty();
        pref.setHasEnoughSamples(false);
        CandidateProfile low = profile(UserTypeConstant.BH, 60, 10, 1);
        CandidateProfile high = profile(UserTypeConstant.BH, 90, 10, 1);
        assertTrue(d1Ranker.score(USER_ID, pref, high) > d1Ranker.score(USER_ID, pref, low));
    }

    @Test
    void score_bhCloserDistanceShouldScoreHigher() {
        PreferenceProfile pref = PreferenceProfile.empty();
        CandidateProfile near = profile(UserTypeConstant.BH, 80, 5, 1);
        CandidateProfile far = profile(UserTypeConstant.BH, 80, 50, 1);
        assertTrue(d1Ranker.score(USER_ID, pref, near) > d1Ranker.score(USER_ID, pref, far));
    }

    @Test
    void score_dhDistanceShouldBeNeutral() {
        PreferenceProfile pref = PreferenceProfile.empty();
        CandidateProfile dh1 = profile(UserTypeConstant.DH, 80, -1, 1);
        CandidateProfile dh2 = profile(UserTypeConstant.DH, 80, 100, 1);
        double s1 = d1Ranker.score(USER_ID, pref, dh1);
        double s2 = d1Ranker.score(USER_ID, pref, dh2);
        assertTrue(Math.abs(s1 - s2) < 0.001);
    }

    @Test
    void score_mutualLikeBonusShouldApply() {
        PreferenceProfile pref = PreferenceProfile.empty();
        CandidateProfile candidate = profile(UserTypeConstant.BH, 80, 10, 1);
        when(swipeHistoryManager.hasPositiveSwipe(20001L, USER_ID)).thenReturn(true);
        double withBonus = d1Ranker.score(USER_ID, pref, candidate);
        when(swipeHistoryManager.hasPositiveSwipe(20001L, USER_ID)).thenReturn(false);
        double withoutBonus = d1Ranker.score(USER_ID, pref, candidate);
        assertTrue(withBonus > withoutBonus);
        assertTrue(withBonus - withoutBonus >= 0.19);
    }

    @Test
    void score_newBhBonusShouldApply() {
        PreferenceProfile pref = PreferenceProfile.empty();
        CandidateProfile newBh = profile(UserTypeConstant.BH, 80, 10, 1);
        newBh.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC).minusDays(1));
        CandidateProfile oldBh = profile(UserTypeConstant.BH, 80, 10, 1);
        oldBh.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC).minusDays(30));
        assertTrue(d1Ranker.score(USER_ID, pref, newBh) > d1Ranker.score(USER_ID, pref, oldBh));
    }

    @Test
    void score_bothBonusesShouldStack() {
        PreferenceProfile pref = PreferenceProfile.empty();
        CandidateProfile candidate = profile(UserTypeConstant.BH, 80, 10, 1);
        candidate.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC).minusDays(1));
        when(swipeHistoryManager.hasPositiveSwipe(20001L, USER_ID)).thenReturn(true);
        double stacked = d1Ranker.score(USER_ID, pref, candidate);
        when(swipeHistoryManager.hasPositiveSwipe(20001L, USER_ID)).thenReturn(false);
        candidate.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC).minusDays(30));
        double base = d1Ranker.score(USER_ID, pref, candidate);
        assertTrue(stacked - base >= 0.39);
    }

    private static CandidateProfile profile(int userType, double beauty, double distanceKm, long userId) {
        CandidateProfile profile = new CandidateProfile();
        profile.setUserId(userId == 1 ? 20001L : userId);
        profile.setUserType(userType);
        profile.setBeautyScore(beauty);
        profile.setDistanceKm(distanceKm);
        profile.setAge(25);
        profile.setRace("asian");
        profile.setLastActiveAt(OffsetDateTime.now(ZoneOffset.UTC).minusHours(1));
        profile.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC).minusDays(10));
        return profile;
    }
}
