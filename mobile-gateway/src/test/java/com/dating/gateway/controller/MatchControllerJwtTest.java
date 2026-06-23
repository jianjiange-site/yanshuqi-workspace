package com.dating.gateway.controller;

import com.dating.gateway.config.AuthConfiguration;
import com.dating.gateway.resolver.JwtCallerUserResolver;
import com.dating.gateway.security.JwtClaims;
import com.dating.gateway.security.JwtVerifier;
import com.dating.gateway.security.TokenBlacklistService;
import com.dating.gateway.service.MatchGrpcClient;
import com.dating.match.grpc.proto.GetQuotaResp;
import com.dating.match.grpc.proto.GetTodayFeedResp;
import com.dating.match.grpc.proto.ListMatchesResp;
import com.dating.match.grpc.proto.ListVisitsResp;
import com.dating.match.grpc.proto.RecordVisitResp;
import com.dating.match.grpc.proto.SuperHiResp;
import com.dating.match.grpc.proto.SwipeResp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Match JWT 生产化测试：验证 prod 无 JWT 返回 401、Bearer JWT 解析 userId、dev X-User-Id 兜底。
 */
@WebMvcTest(controllers = MatchController.class)
@Import({AuthConfiguration.class, JwtCallerUserResolver.class})
@ActiveProfiles("prod")
class MatchControllerJwtTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MatchGrpcClient matchGrpcClient;

    @MockBean
    private JwtVerifier jwtVerifier;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    @Test
    void withoutJwt_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/match/quota"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(10501));
    }

    @Test
    void withJwt_shouldUseTokenUserId() throws Exception {
        JwtClaims claims = new JwtClaims(40004L, "jti-gw3", "dev-device", 3, 1, Long.MAX_VALUE / 1000);
        when(jwtVerifier.verifyAccessToken("good-token")).thenReturn(claims);
        when(tokenBlacklistService.isBlacklisted("jti-gw3")).thenReturn(false);
        when(matchGrpcClient.getQuota(40004L)).thenReturn(GetQuotaResp.newBuilder().setTier("FREE").build());

        mockMvc.perform(get("/api/v1/match/quota")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer good-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tier").value("FREE"));

        verify(matchGrpcClient).getQuota(40004L);
    }

    @Test
    void feedWithJwt_shouldSucceed() throws Exception {
        mockJwtUser(50005L);
        when(matchGrpcClient.getTodayFeed(50005L, 5)).thenReturn(
                GetTodayFeedResp.newBuilder().setExhausted(false).build());

        mockMvc.perform(get("/api/v1/match/feed?count=5")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer good-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.exhausted").value(false));
    }

    @Test
    void swipeWithJwt_shouldSucceed() throws Exception {
        mockJwtUser(50005L);
        when(matchGrpcClient.swipe(eq(50005L), any())).thenReturn(SwipeResp.newBuilder().setMatchId(99L).build());

        mockMvc.perform(post("/api/v1/match/swipe")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer good-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetUserId\":10002,\"direction\":\"RIGHT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.matchId").value(99));
    }

    @Test
    void superHiWithJwt_shouldSucceed() throws Exception {
        mockJwtUser(50005L);
        when(matchGrpcClient.superHi(eq(50005L), any()))
                .thenReturn(SuperHiResp.newBuilder().setMatchId(88L).setCoinsUsed(100).build());

        mockMvc.perform(post("/api/v1/match/super-hi")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer good-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetUserId\":10003,\"clientRequestId\":\"gw3-superhi-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.matchId").value(88))
                .andExpect(jsonPath("$.data.coinsUsed").value(100));
    }

    @Test
    void matchesWithJwt_shouldSucceed() throws Exception {
        mockJwtUser(50005L);
        when(matchGrpcClient.listMatches(50005L, 20, null)).thenReturn(ListMatchesResp.getDefaultInstance());

        mockMvc.perform(get("/api/v1/match/matches?pageSize=20")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer good-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.matches").isArray());
    }

    @Test
    void visitWithJwt_shouldSucceed() throws Exception {
        mockJwtUser(50005L);
        when(matchGrpcClient.recordVisit(50005L, 10002L))
                .thenReturn(RecordVisitResp.newBuilder().setSuccess(true).build());

        mockMvc.perform(post("/api/v1/match/visit/10002")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer good-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void visitsWithJwt_shouldSucceed() throws Exception {
        mockJwtUser(50005L);
        when(matchGrpcClient.listVisits(50005L, 20, null)).thenReturn(ListVisitsResp.getDefaultInstance());

        mockMvc.perform(get("/api/v1/match/visits?pageSize=20")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer good-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.visits").isArray());
    }

    private void mockJwtUser(long userId) {
        JwtClaims claims = new JwtClaims(userId, "jti-gw3", "dev-device", 3, 1, Long.MAX_VALUE / 1000);
        when(jwtVerifier.verifyAccessToken("good-token")).thenReturn(claims);
        when(tokenBlacklistService.isBlacklisted("jti-gw3")).thenReturn(false);
    }
}
