package com.dating.post.service;

import com.dating.post.dto.FeedCandidate;
import com.dating.post.dto.FeedResult;
import com.dating.post.dto.PostInfoDTO;
import com.dating.post.client.UserProfileClient;
import com.dating.post.constant.GenderBucket;
import com.dating.post.repository.FeedPoolRepository;
import com.dating.post.repository.UserTimelineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedServiceTest {

    private static final long CALLER = 10001L;

    @Mock
    private UserProfileClient userProfileClient;

    @Mock
    private FeedPoolRepository feedPoolRepository;

    @Mock
    private UserTimelineRepository userTimelineRepository;

    @Mock
    private ReadHistoryService readHistoryService;

    @Mock
    private PostReadService postReadService;

    private FeedService feedService;

    @BeforeEach
    void setUp() {
        feedService = new FeedService(
                userProfileClient, feedPoolRepository, userTimelineRepository, readHistoryService, postReadService);
    }

    @Test
    void mixCandidates_slot3ShouldPreferTimeline() {
        List<Long> recommend = List.of(1L, 2L, 4L, 5L, 7L, 8L);
        List<Long> timeline = List.of(3L);
        List<Long> coldStart = List.of(6L);

        FeedService.MixContext context = feedService.mixCandidates(6, recommend, timeline, coldStart);
        assertEquals(3L, context.candidates().get(2).getPostId());
        assertEquals(FeedCandidate.Source.TIMELINE, context.candidates().get(2).getSource());
    }

    @Test
    void mixCandidates_slot6ShouldPreferColdStart() {
        List<Long> recommend = List.of(1L, 2L, 3L, 4L, 5L, 7L);
        List<Long> timeline = List.of();
        List<Long> coldStart = List.of(6L);

        FeedService.MixContext context = feedService.mixCandidates(6, recommend, timeline, coldStart);

        assertEquals(6L, context.candidates().get(5).getPostId());
        assertEquals(FeedCandidate.Source.COLD_START, context.candidates().get(5).getSource());
    }

    @Test
    void getRecommendFeed_shouldAssembleDetailsAndMarkRead() {
        when(userProfileClient.getGenderBucket(CALLER)).thenReturn(GenderBucket.MALE);
        when(feedPoolRepository.listRecommendPool(GenderBucket.FEMALE, 0, 40)).thenReturn(List.of(88001L));
        when(userTimelineRepository.listTimeline(CALLER, 40)).thenReturn(List.of());
        when(feedPoolRepository.listColdStartPool(GenderBucket.FEMALE, 0, 40)).thenReturn(List.of());
        when(readHistoryService.filterUnread(eq(CALLER), anyList())).thenAnswer(inv -> inv.getArgument(1));

        PostInfoDTO dto = new PostInfoDTO();
        dto.setPostId(88001L);
        dto.setUserId(20002L);
        dto.setContent("feed item");
        when(postReadService.getPostDetail(eq(88001L), eq(CALLER))).thenReturn(dto);

        FeedResult result = feedService.getRecommendFeed(CALLER, 10, "0");

        assertEquals(1, result.getItems().size());
        verify(readHistoryService).markRead(eq(CALLER), eq(List.of(88001L)));
    }
}
