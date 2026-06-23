package com.dating.gateway.converter;

import com.dating.gateway.dto.vo.CommentVO;
import com.dating.gateway.dto.vo.PostDetailVO;
import com.dating.gateway.dto.vo.PostVO;
import com.dating.post.grpc.proto.CommentInfo;
import com.dating.post.grpc.proto.GetPostDetailResponse;
import com.dating.post.grpc.proto.ListCommentsResponse;
import com.dating.post.grpc.proto.PostInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostProtoAdapterTest {

    @Test
    void toPostVO_shouldMapAllSwaggerFields() {
        PostInfo post = PostInfo.newBuilder()
                .setPostId(88001L)
                .setUserId(10001L)
                .setContent("hello")
                .addImageKeys("post/demo/1.jpg")
                .setLikeCount(3)
                .setCommentCount(2)
                .setIsLiked(true)
                .setCreatedAtSeconds(1700000000L)
                .build();

        PostVO vo = PostProtoAdapter.toPostVO(post);

        assertEquals(88001L, vo.getPostId());
        assertEquals(10001L, vo.getUserId());
        assertEquals("hello", vo.getContent());
        assertEquals(1, vo.getImageKeys().size());
        assertEquals(3, vo.getLikeCount());
        assertEquals(2, vo.getCommentCount());
        assertTrue(vo.getIsLiked());
        assertEquals(1700000000L, vo.getCreatedAtSeconds());
    }

    @Test
    void toPostDetailVO_shouldMapFromResponse() {
        GetPostDetailResponse resp = GetPostDetailResponse.newBuilder()
                .setPost(PostInfo.newBuilder().setPostId(1L).setUserId(2L).setContent("c").build())
                .build();

        PostDetailVO vo = PostProtoAdapter.toPostDetailVO(resp);
        assertEquals(1L, vo.getPostId());
        assertEquals("c", vo.getContent());
    }

    @Test
    void toCommentVO_shouldMapFields() {
        CommentInfo comment = CommentInfo.newBuilder()
                .setCommentId(9001L)
                .setPostId(88001L)
                .setUserId(10001L)
                .setContent("nice")
                .setCreatedAtSeconds(1700000001L)
                .build();

        CommentVO vo = PostProtoAdapter.toCommentVO(comment);

        assertEquals(9001L, vo.getCommentId());
        assertEquals(88001L, vo.getPostId());
        assertEquals(10001L, vo.getUserId());
        assertEquals("nice", vo.getContent());
        assertEquals(1700000001L, vo.getCreatedAtSeconds());
    }

    @Test
    void emptyImageKeys_shouldNotNpe() {
        PostInfo post = PostInfo.newBuilder()
                .setPostId(1L)
                .setUserId(2L)
                .setContent("text")
                .build();

        PostVO vo = PostProtoAdapter.toPostVO(post);
        assertNotNull(vo.getImageKeys());
        assertTrue(vo.getImageKeys().isEmpty());

        List<CommentVO> comments = PostProtoAdapter.toCommentVOList(ListCommentsResponse.getDefaultInstance());
        assertTrue(comments.isEmpty());
    }
}
