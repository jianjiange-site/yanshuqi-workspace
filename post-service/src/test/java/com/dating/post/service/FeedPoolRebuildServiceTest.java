package com.dating.post.service;

import com.dating.post.client.UserProfileClient;
import com.dating.post.constant.GenderBucket;
import com.dating.post.entity.PostEntity;
import com.dating.post.entity.PostStatEntity;
import com.dating.post.manager.PostManager;
import com.dating.post.manager.PostStatManager;
import com.dating.post.repository.FeedPoolRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedPoolRebuildServiceTest {

    @Mock
    private PostManager postManager;

    @Mock
    private PostStatManager postStatManager;

    @Mock
    private PostStatReadService postStatReadService;

    @Mock
    private FeedScoreService feedScoreService;

    @Mock
    private UserProfileClient userProfileClient;

    @Mock
    private FeedPoolRepository feedPoolRepository;

    private FeedPoolRebuildService feedPoolRebuildService;

    @BeforeEach
    void setUp() {
        feedPoolRebuildService = new FeedPoolRebuildService(
                postManager, postStatManager, postStatReadService, feedScoreService, userProfileClient, feedPoolRepository);
    }

    @Test
    void rebuildAllRecommendPools_shouldWriteBothGenderPools() {
        PostEntity post = new PostEntity();
        post.setPostId(88001L);
        post.setUserId(20002L);
        post.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        when(postManager.listRecentActivePosts(any())).thenReturn(List.of(post));

        PostStatEntity stat = new PostStatEntity();
        stat.setPostId(88001L);
        when(postStatManager.findByPostId(88001L)).thenReturn(Optional.of(stat));
        when(postStatReadService.getRealLikeCount(88001L, stat)).thenReturn(2);
        when(postStatReadService.getRealCommentCount(88001L, stat)).thenReturn(1);
        when(feedScoreService.calculateHotScore(2, 1, post.getCreatedAt().toEpochSecond())).thenReturn(5.5);
        when(userProfileClient.batchGetGenderBuckets(any())).thenReturn(Map.of(20002L, GenderBucket.FEMALE));

        feedPoolRebuildService.rebuildAllRecommendPools();

        verify(feedPoolRepository).rebuildRecommendPool(eq(GenderBucket.MALE), any());
        verify(feedPoolRepository).rebuildRecommendPool(eq(GenderBucket.FEMALE), any());
    }
}
