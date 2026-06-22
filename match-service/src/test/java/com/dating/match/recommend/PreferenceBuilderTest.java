package com.dating.match.recommend;

import com.dating.match.client.MockCandidateClient;
import com.dating.match.config.MatchProperties;
import com.dating.match.constant.UserTypeConstant;
import com.dating.match.manager.SwipeHistoryManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PreferenceBuilderTest {

    @Mock
    private SwipeHistoryManager swipeHistoryManager;

    private MockCandidateClient candidateClient;
    private MatchProperties matchProperties;
    private PreferenceBuilder preferenceBuilder;

    @BeforeEach
    void setUp() {
        candidateClient = new MockCandidateClient();
        matchProperties = new MatchProperties();
        matchProperties.getD1().setMinPreferenceSamples(10);
        preferenceBuilder = new PreferenceBuilder(swipeHistoryManager, candidateClient, matchProperties);
    }

    @Test
    void build_shouldMarkInsufficientWhenSampleCountBelow10() {
        when(swipeHistoryManager.listPositiveTargetIds(eq(10001L), any(Instant.class), anyInt()))
                .thenReturn(buildTargetIds(5, 20001L));
        PreferenceProfile profile = preferenceBuilder.build(10001L);
        assertFalse(profile.isHasEnoughSamples());
        assertEquals(5, profile.getSampleCount());
    }

    @Test
    void build_shouldComputeMeansWhenEnoughSamples() {
        when(swipeHistoryManager.listPositiveTargetIds(eq(10001L), any(Instant.class), anyInt()))
                .thenReturn(buildTargetIds(12, 20001L));
        PreferenceProfile profile = preferenceBuilder.build(10001L);
        assertTrue(profile.isHasEnoughSamples());
        assertEquals(12, profile.getSampleCount());
        assertTrue(profile.getAgeMean() > 0);
        assertTrue(profile.getBeautyMean() > 0);
    }

    @Test
    void build_shouldComputeRaceDist() {
        for (int i = 0; i < 10; i++) {
            CandidateProfile p = new CandidateProfile();
            p.setUserId(20001L + i);
            p.setUserType(UserTypeConstant.BH);
            p.setAge(25);
            p.setBeautyScore(80);
            p.setRace(i < 5 ? "asian" : "white");
            candidateClient.putProfile(p);
        }
        when(swipeHistoryManager.listPositiveTargetIds(eq(10001L), any(Instant.class), anyInt()))
                .thenReturn(buildTargetIds(10, 20001L));

        PreferenceProfile profile = preferenceBuilder.build(10001L);
        assertEquals(0.5, profile.getRaceDist().get("asian"), 0.001);
        assertEquals(0.5, profile.getRaceDist().get("white"), 0.001);
    }

    @Test
    void build_shouldComputeDhAndBhRatio() {
        for (int i = 0; i < 10; i++) {
            CandidateProfile p = new CandidateProfile();
            p.setUserId(20001L + i);
            p.setUserType(i < 3 ? UserTypeConstant.DH : UserTypeConstant.BH);
            p.setAge(25);
            p.setBeautyScore(80);
            p.setRace("asian");
            candidateClient.putProfile(p);
        }
        when(swipeHistoryManager.listPositiveTargetIds(eq(10001L), any(Instant.class), anyInt()))
                .thenReturn(buildTargetIds(10, 20001L));

        PreferenceProfile profile = preferenceBuilder.build(10001L);
        assertEquals(0.3, profile.getDhRatio(), 0.001);
        assertEquals(0.7, profile.getBhRatio(), 0.001);
    }

    @Test
    void stdDev_shouldNotBeZeroForSingleValue() {
        assertEquals(1.0, PreferenceBuilder.stdDev(List.of(25.0), 25.0), 0.001);
    }

    private static List<Long> buildTargetIds(int count, long baseId) {
        return LongStream.range(0, count).map(i -> baseId + i).boxed().toList();
    }
}
