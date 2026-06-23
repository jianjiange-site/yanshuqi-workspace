package com.dating.gateway.controller;

import com.dating.gateway.config.AuthConfiguration;
import com.dating.gateway.dto.vo.CommentVO;
import com.dating.gateway.dto.vo.PostDetailVO;
import com.dating.gateway.dto.vo.PostVO;
import com.dating.gateway.resolver.JwtCallerUserResolver;
import com.dating.gateway.security.JwtClaims;
import com.dating.gateway.security.JwtVerifier;
import com.dating.gateway.security.TokenBlacklistService;
import com.dating.gateway.service.PostBffService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PostController.class)
@Import({AuthConfiguration.class, JwtCallerUserResolver.class})
@ActiveProfiles("prod")
class PostControllerJwtTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostBffService postBffService;

    @MockBean
    private JwtVerifier jwtVerifier;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    @Test
    void withoutJwt_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/post/feed"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(10501));
    }

    @Test
    void createWithJwt_shouldUseTokenUserId() throws Exception {
        mockJwtUser(60006L);
        when(postBffService.createPost(eq(60006L), any())).thenReturn(88001L);

        mockMvc.perform(post("/api/v1/post")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer good-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hello post\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(88001));

        verify(postBffService).createPost(eq(60006L), any());
    }

    private void mockJwtUser(long userId) {
        JwtClaims claims = new JwtClaims(userId, "jti-gw4", "dev-device", 3, 1, Long.MAX_VALUE / 1000);
        when(jwtVerifier.verifyAccessToken("good-token")).thenReturn(claims);
        when(tokenBlacklistService.isBlacklisted("jti-gw4")).thenReturn(false);
    }
}
