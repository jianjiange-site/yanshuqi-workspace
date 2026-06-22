package com.dating.match.recommend;

import com.dating.match.client.MockCandidateClient;
import com.dating.match.config.MatchProperties;
import com.dating.match.constant.UserTypeConstant;
import com.dating.match.manager.SwipeHistoryManager;
import com.dating.match.support.InMemoryFeedQueueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class D1GeneratorTest {

    private static final long USER_ID = 10001L;

    @Mock
    private SwipeHistoryManager swipeHistoryManager;

    private MockCandidateClient candidateClient;
    private InMemoryFeedQueueRepository feedQueueRepository;
    private MatchProperties matchProperties;
    private D1Generator d1Generator;
    private LocalDate referenceDate;

    @BeforeEach
    void setUp() {
        candidateClient = new MockCandidateClient();
        candidateClient.resetPools(240, 240);
        feedQueueRepository = new InMemoryFeedQueueRepository();
        matchProperties = new MatchProperties();
        matchProperties.getFeed().setQueueSize(240);
        matchProperties.getFeed().setQueueTtlDays(7);
        matchProperties.getD1().setMinPreferenceSamples(10);

        PreferenceBuilder preferenceBuilder = new PreferenceBuilder(swipeHistoryManager, candidateClient, matchProperties);
        CandidateRecaller recaller = new CandidateRecaller(candidateClient, swipeHistoryManager, matchProperties);
        D1Ranker ranker = new D1Ranker(swipeHistoryManager, matchProperties);
        D1FeedMergeService mergeService = new D1FeedMergeService(new FeedMergeService(), matchProperties);

        d1Generator = new D1Generator(
                swipeHistoryManager, preferenceBuilder, recaller, ranker, mergeService,
                feedQueueRepository, matchProperties);
        referenceDate = LocalDate.now(ZoneOffset.UTC);
    }

    @Test
    void generateForUser_shouldSkipWhenNoSwipeYesterday() {
        when(swipeHistoryManager.hasSwipeYesterday(USER_ID, referenceDate.minusDays(1))).thenReturn(false);
        assertEquals(0, d1Generator.generateForUser(USER_ID, referenceDate));
    }

    @Test
    void generateForUser_shouldBuildQueueWhenSwipeYesterday() {
        LocalDate yesterday = referenceDate.minusDays(1);
        when(swipeHistoryManager.hasSwipeYesterday(USER_ID, yesterday)).thenReturn(true);
        when(swipeHistoryManager.listPositiveTargetIds(eq(USER_ID), any(), anyInt())).thenReturn(List.of());
        when(swipeHistoryManager.listAllSwipedTargetIds(USER_ID, 2000)).thenReturn(List.of());

        int written = d1Generator.generateForUser(USER_ID, referenceDate);
        assertTrue(written > 0);
        assertTrue(feedQueueRepository.wasLastReplace());
    }

    @Test
    void generateForUser_shouldReplaceAllNotAppend() {
        feedQueueRepository.pushAll(USER_ID, List.of(new FeedQueueItem(99999L, UserTypeConstant.BH)),
                java.time.Duration.ofDays(7));
        LocalDate yesterday = referenceDate.minusDays(1);
        when(swipeHistoryManager.hasSwipeYesterday(USER_ID, yesterday)).thenReturn(true);
        when(swipeHistoryManager.listPositiveTargetIds(eq(USER_ID), any(), anyInt())).thenReturn(List.of());
        when(swipeHistoryManager.listAllSwipedTargetIds(USER_ID, 2000)).thenReturn(List.of());

        d1Generator.generateForUser(USER_ID, referenceDate);
        assertTrue(feedQueueRepository.wasLastReplace());
        assertTrue(feedQueueRepository.size(USER_ID) > 0);
    }

    @Test
    void generateForUser_shouldReturnActualCountWhenCandidatesInsufficient() {
        candidateClient.resetPools(5, 5);
        LocalDate yesterday = referenceDate.minusDays(1);
        when(swipeHistoryManager.hasSwipeYesterday(USER_ID, yesterday)).thenReturn(true);
        when(swipeHistoryManager.listPositiveTargetIds(eq(USER_ID), any(), anyInt())).thenReturn(List.of());
        when(swipeHistoryManager.listAllSwipedTargetIds(USER_ID, 2000)).thenReturn(List.of());

        int written = d1Generator.generateForUser(USER_ID, referenceDate);
        assertTrue(written < 240);
        assertTrue(written > 0);
    }

    @Test
    void generateForUser_shouldExcludeSwipedTargets() {
        LocalDate yesterday = referenceDate.minusDays(1);
        when(swipeHistoryManager.hasSwipeYesterday(USER_ID, yesterday)).thenReturn(true);
        when(swipeHistoryManager.listPositiveTargetIds(eq(USER_ID), any(), anyInt())).thenReturn(List.of());
        when(swipeHistoryManager.listAllSwipedTargetIds(USER_ID, 2000))
                .thenReturn(List.of(MockCandidateClient.BH_ID_BASE));

        d1Generator.generateForUser(USER_ID, referenceDate);
        // leftPop and check first item is not excluded id
        var popped = feedQueueRepository.leftPop(USER_ID, 240);
        assertTrue(popped.stream().noneMatch(i -> i.getTargetUserId() == MockCandidateClient.BH_ID_BASE));
    }
}
