package com.dating.post.service;

import com.dating.post.dto.ListCommentsResult;
import com.dating.post.entity.PostCommentEntity;
import com.dating.post.entity.PostEntity;
import com.dating.post.exception.PostBusinessException;
import com.dating.post.exception.PostErrorCode;
import com.dating.post.manager.PostCommentManager;
import com.dating.post.manager.PostManager;
import com.dating.post.repository.PostStatDeltaRepository;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostCommentServiceTest {

    private static final long CALLER = 10001L;
    private static final long POST_ID = 88001L;

    @Mock
    private PostManager postManager;

    @Mock
    private PostCommentManager postCommentManager;

    @Mock
    private PostStatDeltaRepository postStatDeltaRepository;

    @Mock
    private PostCacheService postCacheService;

    @Mock
    private BusinessIdGenerator businessIdGenerator;

    private PostCommentService postCommentService;

    @BeforeEach
    void setUp() {
        postCommentService = new PostCommentService(
                postManager, postCommentManager, postStatDeltaRepository, postCacheService, businessIdGenerator);
    }

    @Test
    void createComment_shouldPersistAndUpdateRedis() {
        PostEntity post = new PostEntity();
        post.setPostId(POST_ID);
        when(postManager.findActivePost(POST_ID)).thenReturn(Optional.of(post));
        when(businessIdGenerator.nextId()).thenReturn(99001L);

        long commentId = postCommentService.createComment(CALLER, POST_ID, "nice post");

        assertEquals(99001L, commentId);
        verify(postCommentManager).insertComment(any(PostCommentEntity.class));
        verify(postStatDeltaRepository).incrementCommentDelta(POST_ID, 1);
        verify(postStatDeltaRepository).addCommentToWindow(POST_ID, 99001L);
        verify(postCacheService).evictDetail(POST_ID);
    }

    @Test
    void listComments_shouldFallbackToDbWhenWindowEmpty() {
        when(postStatDeltaRepository.listCommentIdsFromWindow(POST_ID, 0L, 21)).thenReturn(Set.of());
        PostCommentEntity entity = sampleComment(99001L);
        when(postCommentManager.listCommentsFromDb(POST_ID, 0L, 21)).thenReturn(List.of(entity));

        ListCommentsResult result = postCommentService.listComments(POST_ID, "0", 20);

        assertEquals(1, result.getItems().size());
        assertEquals(99001L, result.getItems().get(0).getCommentId());
    }

    @Test
    void deleteComment_whenNotOwner_shouldForbidden() {
        PostCommentEntity comment = sampleComment(99001L);
        comment.setUserId(99999L);
        when(postCommentManager.findActiveComment(99001L)).thenReturn(Optional.of(comment));

        PostBusinessException ex = assertThrows(PostBusinessException.class,
                () -> postCommentService.deleteComment(CALLER, 99001L));
        assertEquals(PostErrorCode.FORBIDDEN, ex.getErrorCode());
    }

    private PostCommentEntity sampleComment(long commentId) {
        PostCommentEntity entity = new PostCommentEntity();
        entity.setCommentId(commentId);
        entity.setPostId(POST_ID);
        entity.setUserId(CALLER);
        entity.setContent("hello");
        entity.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        return entity;
    }
}
