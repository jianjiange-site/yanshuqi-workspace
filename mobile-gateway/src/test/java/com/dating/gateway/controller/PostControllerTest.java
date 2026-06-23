package com.dating.gateway.controller;

import com.dating.gateway.dto.vo.CommentVO;
import com.dating.gateway.dto.vo.PostDetailVO;
import com.dating.gateway.dto.vo.PostVO;
import com.dating.gateway.exception.GatewayBizException;
import com.dating.gateway.exception.GatewayErrorCode;
import com.dating.gateway.resolver.CallerUserResolver;
import com.dating.gateway.resolver.DevHeaderCallerUserResolver;
import com.dating.gateway.service.PostBffService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
@AutoConfigureMockMvc(addFilters = false)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostBffService postBffService;

    @MockBean
    private CallerUserResolver callerUserResolver;

    @Test
    void create_shouldReturnPostId() throws Exception {
        when(callerUserResolver.resolveCallerUserId(any())).thenReturn(10001L);
        when(postBffService.createPost(eq(10001L), any())).thenReturn(88001L);

        mockMvc.perform(post("/api/v1/post")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hello\",\"imageKeys\":[\"post/demo/1.jpg\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(88001));
    }

    @Test
    void detail_shouldReturnPostDetail() throws Exception {
        when(callerUserResolver.resolveCallerUserId(any())).thenReturn(10001L);
        PostDetailVO vo = new PostDetailVO();
        vo.setPostId(88001L);
        vo.setContent("hello");
        when(postBffService.getPostDetail(10001L, 88001L)).thenReturn(vo);

        mockMvc.perform(get("/api/v1/post/88001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.postId").value(88001))
                .andExpect(jsonPath("$.data.content").value("hello"));
    }

    @Test
    void delete_shouldReturnTrue() throws Exception {
        when(callerUserResolver.resolveCallerUserId(any())).thenReturn(10001L);
        when(postBffService.deletePost(10001L, 88001L)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/post/88001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void feed_shouldReturnList() throws Exception {
        when(callerUserResolver.resolveCallerUserId(any())).thenReturn(10001L);
        PostVO item = new PostVO();
        item.setPostId(88001L);
        when(postBffService.getRecommendFeed(eq(10001L), eq(10), isNull())).thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/post/feed?pageSize=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].postId").value(88001));
    }

    @Test
    void listUserPosts_shouldReturnList() throws Exception {
        when(callerUserResolver.resolveCallerUserId(any())).thenReturn(10001L);
        when(postBffService.listUserPosts(eq(10001L), eq(20002L), eq(20), isNull())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/post/user/20002?pageSize=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void like_shouldReturnTrue() throws Exception {
        when(callerUserResolver.resolveCallerUserId(any())).thenReturn(10001L);
        when(postBffService.likePost(10001L, 88001L)).thenReturn(true);

        mockMvc.perform(post("/api/v1/post/88001/like"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void unlike_shouldReturnTrue() throws Exception {
        when(callerUserResolver.resolveCallerUserId(any())).thenReturn(10001L);
        when(postBffService.unlikePost(10001L, 88001L)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/post/88001/like"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void listComments_shouldReturnList() throws Exception {
        when(callerUserResolver.resolveCallerUserId(any())).thenReturn(10001L);
        CommentVO comment = new CommentVO();
        comment.setCommentId(9001L);
        when(postBffService.listComments(eq(10001L), eq(88001L), eq(20), isNull())).thenReturn(List.of(comment));

        mockMvc.perform(get("/api/v1/post/88001/comment?pageSize=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].commentId").value(9001));
    }

    @Test
    void createComment_shouldReturnCommentId() throws Exception {
        when(callerUserResolver.resolveCallerUserId(any())).thenReturn(10001L);
        when(postBffService.createComment(eq(10001L), eq(88001L), any())).thenReturn(9001L);

        mockMvc.perform(post("/api/v1/post/88001/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"postId\":88001,\"content\":\"hello comment\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(9001));
    }

    @Test
    void deleteComment_shouldReturnTrue() throws Exception {
        when(callerUserResolver.resolveCallerUserId(any())).thenReturn(10001L);
        when(postBffService.deleteComment(10001L, 9001L)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/post/comment/9001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void invalidPostId_shouldReturnError() throws Exception {
        when(callerUserResolver.resolveCallerUserId(any())).thenReturn(10001L);

        mockMvc.perform(get("/api/v1/post/0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(10400));
    }

    @Test
    void createComment_mismatchedPostId_shouldReturnError() throws Exception {
        when(callerUserResolver.resolveCallerUserId(any())).thenReturn(10001L);

        mockMvc.perform(post("/api/v1/post/88001/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"postId\":99999,\"content\":\"x\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(10400));
    }

    @Test
    void devHeaderFallback_resolverUsesCallerUserId() throws Exception {
        when(callerUserResolver.resolveCallerUserId(any())).thenReturn(20002L);
        when(postBffService.getRecommendFeed(eq(20002L), anyInt(), isNull())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/post/feed")
                        .header(DevHeaderCallerUserResolver.HEADER_USER_ID, "20002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(postBffService).getRecommendFeed(eq(20002L), anyInt(), isNull());
    }

    @Test
    void withoutJwt_resolverThrowsTokenInvalid() throws Exception {
        when(callerUserResolver.resolveCallerUserId(any()))
                .thenThrow(new GatewayBizException(GatewayErrorCode.TOKEN_INVALID));

        mockMvc.perform(get("/api/v1/post/feed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(10501));
    }
}
