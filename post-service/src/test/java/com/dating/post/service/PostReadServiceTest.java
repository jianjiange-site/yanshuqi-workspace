package com.dating.post.service;

import com.dating.post.dto.PostInfoDTO;
import com.dating.post.entity.PostEntity;
import com.dating.post.entity.PostImageEntity;
import com.dating.post.entity.PostStatEntity;
import com.dating.post.exception.PostBusinessException;
import com.dating.post.exception.PostErrorCode;
import com.dating.post.manager.PostManager;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostReadServiceTest {

    private static final long POST_ID = 88001L;
    private static final long USER_ID = 10001L;

    @Mock
    private PostManager postManager;

    @Mock
    private PostCacheService postCacheService;

    @Mock
    private PostStatReadService postStatReadService;

    private PostReadService postReadService;

    @BeforeEach
    void setUp() {
        postReadService = new PostReadService(postManager, postCacheService, postStatReadService);
    }

    @Test
    void getPostDetail_whenCacheHit_shouldEnrichRealtimeFields() {
        PostInfoDTO cached = sampleDto();
        when(postCacheService.getDetail(POST_ID)).thenReturn(Optional.of(cached));
        PostStatEntity stat = sampleStat();
        when(postManager.findStat(POST_ID)).thenReturn(Optional.of(stat));
        when(postStatReadService.getRealLikeCount(POST_ID, stat)).thenReturn(5);
        when(postStatReadService.getRealCommentCount(POST_ID, stat)).thenReturn(2);
        when(postStatReadService.isLiked(USER_ID, POST_ID)).thenReturn(true);

        PostInfoDTO result = postReadService.getPostDetail(POST_ID, USER_ID);

        assertEquals(5, result.getLikeCount());
        assertEquals(2, result.getCommentCount());
        assertTrue(result.isLiked());
    }

    @Test
    void getPostDetail_whenCacheMiss_shouldLoadDbAndCache() {
        when(postCacheService.getDetail(POST_ID)).thenReturn(Optional.empty());
        PostEntity post = samplePost();
        when(postManager.findActivePost(POST_ID)).thenReturn(Optional.of(post));
        when(postManager.listImages(POST_ID)).thenReturn(List.of(sampleImage()));
        PostStatEntity stat = sampleStat();
        when(postManager.findStat(POST_ID)).thenReturn(Optional.of(stat));
        when(postStatReadService.getRealLikeCount(POST_ID, stat)).thenReturn(1);
        when(postStatReadService.getRealCommentCount(POST_ID, stat)).thenReturn(1);
        when(postStatReadService.isLiked(USER_ID, POST_ID)).thenReturn(false);

        PostInfoDTO result = postReadService.getPostDetail(POST_ID, USER_ID);

        assertEquals("hello", result.getContent());
        verify(postCacheService).putDetail(result);
        assertFalse(result.isLiked());
    }

    @Test
    void getPostDetail_whenNotFound_shouldThrow() {
        when(postCacheService.getDetail(POST_ID)).thenReturn(Optional.empty());
        when(postManager.findActivePost(POST_ID)).thenReturn(Optional.empty());

        PostBusinessException ex = assertThrows(PostBusinessException.class,
                () -> postReadService.getPostDetail(POST_ID, USER_ID));
        assertEquals(PostErrorCode.POST_NOT_FOUND, ex.getErrorCode());
    }

    private PostEntity samplePost() {
        PostEntity post = new PostEntity();
        post.setPostId(POST_ID);
        post.setUserId(USER_ID);
        post.setContent("hello");
        post.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        return post;
    }

    private PostImageEntity sampleImage() {
        PostImageEntity image = new PostImageEntity();
        image.setPostId(POST_ID);
        image.setSortOrder(0);
        image.setImageKey("post/10001/a.jpg");
        return image;
    }

    private PostStatEntity sampleStat() {
        PostStatEntity stat = new PostStatEntity();
        stat.setPostId(POST_ID);
        stat.setLikeCount(0);
        stat.setCommentCount(0);
        return stat;
    }

    private PostInfoDTO sampleDto() {
        PostInfoDTO dto = new PostInfoDTO();
        dto.setPostId(POST_ID);
        dto.setUserId(USER_ID);
        dto.setContent("cached");
        dto.setLiked(false);
        return dto;
    }
}
