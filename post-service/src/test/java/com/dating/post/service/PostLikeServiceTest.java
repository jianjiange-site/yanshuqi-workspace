package com.dating.post.service;

import com.dating.post.constant.LikeStatus;
import com.dating.post.entity.PostEntity;
import com.dating.post.manager.PostLikeManager;
import com.dating.post.manager.PostManager;
import com.dating.post.repository.PostStatDeltaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostLikeServiceTest {

    private static final long CALLER = 10001L;
    private static final long POST_ID = 88001L;

    @Mock
    private PostManager postManager;

    @Mock
    private PostLikeManager postLikeManager;

    @Mock
    private PostStatDeltaRepository postStatDeltaRepository;

    @Mock
    private PostCacheService postCacheService;

    private PostLikeService postLikeService;

    @BeforeEach
    void setUp() {
        postLikeService = new PostLikeService(
                postManager, postLikeManager, postStatDeltaRepository, postCacheService);
        PostEntity post = new PostEntity();
        post.setPostId(POST_ID);
        when(postManager.findActivePost(POST_ID)).thenReturn(Optional.of(post));
    }

    @Test
    void actionLike_whenStatusChanged_shouldIncrementDelta() {
        when(postLikeManager.upsertIfChanged(CALLER, POST_ID, LikeStatus.LIKED)).thenReturn(true);

        assertTrue(postLikeService.actionLike(CALLER, POST_ID, true));

        verify(postStatDeltaRepository).incrementLikeDelta(POST_ID, 1);
        verify(postCacheService).evictDetail(POST_ID);
    }

    @Test
    void actionLike_whenDuplicateLike_shouldNotIncrementDelta() {
        when(postLikeManager.upsertIfChanged(CALLER, POST_ID, LikeStatus.LIKED)).thenReturn(false);

        assertTrue(postLikeService.actionLike(CALLER, POST_ID, true));

        verify(postStatDeltaRepository, never()).incrementLikeDelta(eq(POST_ID), eq(1));
        verify(postCacheService, never()).evictDetail(POST_ID);
    }
}
