package com.dating.post.service;

import com.dating.post.dto.PostCreateCommand;
import com.dating.post.dto.PostInfoDTO;
import com.dating.post.entity.PostEntity;
import com.dating.post.exception.PostBusinessException;
import com.dating.post.exception.PostErrorCode;
import com.dating.post.manager.PostManager;
import com.dating.post.client.UserProfileClient;
import com.dating.post.constant.GenderBucket;
import com.dating.post.repository.FeedPoolRepository;
import com.dating.post.service.PostFanoutService;
import com.dating.post.service.support.BusinessIdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostWriteServiceTest {

    private static final long CALLER = 10001L;
    private static final long POST_ID = 88001L;

    @Mock
    private PostManager postManager;

    @Mock
    private PostReadService postReadService;

    @Mock
    private PostCacheService postCacheService;

    @Mock
    private BusinessIdGenerator businessIdGenerator;

    @Mock
    private UserProfileClient userProfileClient;

    @Mock
    private FeedPoolRepository feedPoolRepository;

    @Mock
    private PostFanoutService postFanoutService;

    private PostWriteService postWriteService;

    @BeforeEach
    void setUp() {
        postWriteService = new PostWriteService(
                postManager, postReadService, postCacheService, businessIdGenerator,
                userProfileClient, feedPoolRepository, postFanoutService);
    }

    @Test
    void createPost_shouldPersistThenCache() {
        PostCreateCommand command = new PostCreateCommand();
        command.setCallerUserId(CALLER);
        command.setContent("hello post");
        command.setImageKeys(List.of("post/10001/a.jpg"));

        when(businessIdGenerator.nextId()).thenReturn(POST_ID);
        doNothing().when(postManager).createPost(eq(POST_ID), eq(CALLER), eq("hello post"), anyList());
        when(userProfileClient.getGenderBucket(CALLER)).thenReturn(GenderBucket.MALE);

        PostInfoDTO dto = new PostInfoDTO();
        dto.setPostId(POST_ID);
        dto.setUserId(CALLER);
        dto.setContent("hello post");
        dto.setCreatedAtSeconds(1_700_000_000L);
        when(postReadService.loadFromDb(POST_ID)).thenReturn(Optional.of(dto));

        long postId = postWriteService.createPost(command);

        assertEquals(POST_ID, postId);
        verify(postCacheService).putDetail(dto);
        verify(feedPoolRepository).addToColdStartPool(GenderBucket.MALE, POST_ID, 1_700_000_000L);
        verify(postFanoutService).fanoutToFriendsAsync(CALLER, POST_ID, 1_700_000_000L);
    }

    @Test
    void deletePost_whenNotOwner_shouldForbidden() {
        PostEntity post = new PostEntity();
        post.setPostId(POST_ID);
        post.setUserId(99999L);
        when(postManager.findActivePost(POST_ID)).thenReturn(Optional.of(post));

        PostBusinessException ex = assertThrows(PostBusinessException.class,
                () -> postWriteService.deletePost(CALLER, POST_ID));
        assertEquals(PostErrorCode.FORBIDDEN, ex.getErrorCode());
        verify(postManager, never()).softDeletePost(post);
    }

    @Test
    void deletePost_whenOwner_shouldSoftDeleteAndEvictCache() {
        PostEntity post = new PostEntity();
        post.setPostId(POST_ID);
        post.setUserId(CALLER);
        when(postManager.findActivePost(POST_ID)).thenReturn(Optional.of(post));
        doNothing().when(postManager).softDeletePost(post);

        postWriteService.deletePost(CALLER, POST_ID);

        verify(postManager).softDeletePost(post);
        verify(postCacheService).evictDetail(POST_ID);
    }
}
