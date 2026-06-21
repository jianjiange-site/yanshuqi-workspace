package com.dating.gateway.controller;

import com.dating.gateway.dto.vo.MatchFeedVO;
import com.dating.gateway.dto.vo.MatchQuotaVO;
import com.dating.gateway.security.CallerUserResolver;
import com.dating.gateway.security.DevHeaderCallerUserResolver;
import com.dating.gateway.service.MatchGrpcClient;
import com.dating.match.grpc.proto.GetQuotaResp;
import com.dating.match.grpc.proto.GetTodayFeedResp;
import com.dating.match.grpc.proto.RecordVisitResp;
import com.dating.match.grpc.proto.SwipeResp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MatchController.class)
class MatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MatchGrpcClient matchGrpcClient;

    @MockBean
    private CallerUserResolver callerUserResolver;

    @Test
    void getFeed_shouldReturnResultWithCallerUserId() throws Exception {
        when(callerUserResolver.resolveCallerUserId(any())).thenReturn(20002L);
        when(matchGrpcClient.getTodayFeed(20002L, 5)).thenReturn(
                GetTodayFeedResp.newBuilder().setExhausted(false).build());

        mockMvc.perform(get("/api/v1/match/feed")
                        .header(DevHeaderCallerUserResolver.HEADER_USER_ID, "20002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data.exhausted").value(false))
                .andExpect(jsonPath("$.data.cards").isArray());

        verify(matchGrpcClient).getTodayFeed(20002L, 5);
    }

    @Test
    void swipe_shouldForwardToGrpcClient() throws Exception {
        when(callerUserResolver.resolveCallerUserId(any())).thenReturn(20002L);
        when(matchGrpcClient.swipe(eq(20002L), any())).thenReturn(
                SwipeResp.newBuilder().setMatchId(0L).build());

        mockMvc.perform(post("/api/v1/match/swipe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetUserId\":30003,\"direction\":\"RIGHT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.matchId").doesNotExist());

        verify(matchGrpcClient).swipe(eq(20002L), any());
    }

    @Test
    void getQuota_shouldReturnFreeTierMock() throws Exception {
        when(callerUserResolver.resolveCallerUserId(any())).thenReturn(20002L);
        when(matchGrpcClient.getQuota(20002L)).thenReturn(
                GetQuotaResp.newBuilder().setTier("FREE").setDailyCardLimit(20).build());

        mockMvc.perform(get("/api/v1/match/quota"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tier").value("FREE"))
                .andExpect(jsonPath("$.data.dailyCardLimit").value(20));
    }

    @Test
    void recordVisit_shouldReturnBooleanResult() throws Exception {
        when(callerUserResolver.resolveCallerUserId(any())).thenReturn(20002L);
        when(matchGrpcClient.recordVisit(20002L, 30003L)).thenReturn(
                RecordVisitResp.newBuilder().setSuccess(true).build());

        mockMvc.perform(post("/api/v1/match/visit/30003"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }
}
