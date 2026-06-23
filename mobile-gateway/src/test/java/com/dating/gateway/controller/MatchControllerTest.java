package com.dating.gateway.controller;



import com.dating.gateway.dto.vo.MatchFeedVO;

import com.dating.gateway.dto.vo.MatchListVO;

import com.dating.gateway.dto.vo.MatchQuotaVO;

import com.dating.gateway.dto.vo.SuperHiResultVO;

import com.dating.gateway.dto.vo.SwipeResultVO;

import com.dating.gateway.dto.vo.VisitListVO;

import com.dating.gateway.exception.GatewayBizException;

import com.dating.gateway.exception.GatewayErrorCode;

import com.dating.gateway.resolver.CallerUserResolver;

import com.dating.gateway.resolver.DevHeaderCallerUserResolver;

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

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;



import static org.mockito.ArgumentMatchers.any;

import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.verify;

import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@WebMvcTest(MatchController.class)

@AutoConfigureMockMvc(addFilters = false)

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

                .andExpect(jsonPath("$.data.exhausted").value(false))

                .andExpect(jsonPath("$.data.cards").isArray());



        verify(matchGrpcClient).getTodayFeed(20002L, 5);

    }



    @Test

    void getFeed_shouldClampCountToMax20() throws Exception {

        when(callerUserResolver.resolveCallerUserId(any())).thenReturn(20002L);

        when(matchGrpcClient.getTodayFeed(20002L, 20)).thenReturn(GetTodayFeedResp.getDefaultInstance());



        mockMvc.perform(get("/api/v1/match/feed?count=100"))

                .andExpect(status().isOk());



        verify(matchGrpcClient).getTodayFeed(20002L, 20);

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

                .andExpect(jsonPath("$.data.matchId").doesNotExist());



        verify(matchGrpcClient).swipe(eq(20002L), any());

    }



    @Test

    void swipe_invalidDirection_shouldReject() throws Exception {

        when(callerUserResolver.resolveCallerUserId(any())).thenReturn(20002L);



        mockMvc.perform(post("/api/v1/match/swipe")

                        .contentType(MediaType.APPLICATION_JSON)

                        .content("{\"targetUserId\":30003,\"direction\":\"UP\"}"))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.code").value(10400));

    }



    @Test

    void superHi_shouldForwardClientRequestId() throws Exception {

        when(callerUserResolver.resolveCallerUserId(any())).thenReturn(20002L);

        when(matchGrpcClient.superHi(eq(20002L), any())).thenReturn(

                SuperHiResp.newBuilder().setMatchId(1L).setCoinsUsed(100).build());



        mockMvc.perform(post("/api/v1/match/super-hi")

                        .contentType(MediaType.APPLICATION_JSON)

                        .content("{\"targetUserId\":30003,\"clientRequestId\":\"gw3-superhi-001\"}"))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.data.matchId").value(1));



        verify(matchGrpcClient).superHi(eq(20002L), any());

    }



    @Test

    void superHi_missingClientRequestId_shouldReject() throws Exception {

        when(callerUserResolver.resolveCallerUserId(any())).thenReturn(20002L);



        mockMvc.perform(post("/api/v1/match/super-hi")

                        .contentType(MediaType.APPLICATION_JSON)

                        .content("{\"targetUserId\":30003,\"clientRequestId\":\"\"}"))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.code").value(10400));

    }



    @Test

    void getQuota_shouldReturnFreeTier() throws Exception {

        when(callerUserResolver.resolveCallerUserId(any())).thenReturn(20002L);

        when(matchGrpcClient.getQuota(20002L)).thenReturn(

                GetQuotaResp.newBuilder().setTier("FREE").setDailyCardLimit(20).build());



        mockMvc.perform(get("/api/v1/match/quota"))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.data.tier").value("FREE"))

                .andExpect(jsonPath("$.data.dailyCardLimit").value(20));

    }



    @Test

    void listMatches_shouldReturnPagedResult() throws Exception {

        when(callerUserResolver.resolveCallerUserId(any())).thenReturn(20002L);

        when(matchGrpcClient.listMatches(20002L, 20, null)).thenReturn(ListMatchesResp.getDefaultInstance());



        mockMvc.perform(get("/api/v1/match/matches?pageSize=20"))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.data.matches").isArray());

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



    @Test

    void recordVisit_invalidTarget_shouldReject() throws Exception {

        when(callerUserResolver.resolveCallerUserId(any())).thenReturn(20002L);



        mockMvc.perform(post("/api/v1/match/visit/0"))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.code").value(10400));

    }



    @Test

    void listVisits_shouldReturnPagedResult() throws Exception {

        when(callerUserResolver.resolveCallerUserId(any())).thenReturn(20002L);

        when(matchGrpcClient.listVisits(20002L, 20, null)).thenReturn(ListVisitsResp.getDefaultInstance());



        mockMvc.perform(get("/api/v1/match/visits?pageSize=20"))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.data.visits").isArray());

    }



    @Test

    void withoutAuth_resolverThrowsTokenInvalid() throws Exception {

        when(callerUserResolver.resolveCallerUserId(any()))

                .thenThrow(new GatewayBizException(GatewayErrorCode.TOKEN_INVALID));



        mockMvc.perform(get("/api/v1/match/quota"))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.code").value(10501));

    }

}


