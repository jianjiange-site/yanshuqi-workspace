package com.dating.post.service;

import com.dating.post.client.UserProfileClient;
import com.dating.post.repository.UserTimelineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostFanoutServiceTest {

    @Mock
    private UserProfileClient userProfileClient;

    @Mock
    private UserTimelineRepository userTimelineRepository;

    private PostFanoutService postFanoutService;

    @BeforeEach
    void setUp() {
        postFanoutService = new PostFanoutService(userProfileClient, userTimelineRepository);
    }

    @Test
    void fanoutToFriendsAsync_whenFriendsExist_shouldWriteTimeline() {
        when(userProfileClient.getFriendUserIds(10001L)).thenReturn(List.of(20002L, 20003L));

        postFanoutService.fanoutToFriendsAsync(10001L, 88001L, 1_700_000_000L);

        verify(userTimelineRepository).addToTimeline(20002L, 88001L, 1_700_000_000L);
        verify(userTimelineRepository).addToTimeline(20003L, 88001L, 1_700_000_000L);
    }

    @Test
    void fanoutToFriendsAsync_whenFallbackEmpty_shouldSkip() {
        when(userProfileClient.getFriendUserIds(10001L)).thenReturn(List.of());

        postFanoutService.fanoutToFriendsAsync(10001L, 88001L, 1_700_000_000L);

        verify(userTimelineRepository, never()).addToTimeline(org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyDouble());
    }
}
