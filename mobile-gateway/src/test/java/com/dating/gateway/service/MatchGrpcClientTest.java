package com.dating.gateway.service;

import com.dating.gateway.dto.req.SuperHiReq;
import com.dating.gateway.exception.GatewayBizException;
import com.dating.gateway.exception.GatewayErrorCode;
import com.dating.match.grpc.proto.MatchServiceGrpc;
import com.dating.match.grpc.proto.SuperHiResp;
import com.dating.match.grpc.proto.SwipeResp;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchGrpcClientTest {

    @Mock
    private MatchServiceGrpc.MatchServiceBlockingStub matchServiceStub;

    private MatchGrpcClient matchGrpcClient;

    @BeforeEach
    void setUp() {
        matchGrpcClient = new MatchGrpcClient();
        ReflectionTestUtils.setField(matchGrpcClient, "matchServiceStub", matchServiceStub);
        ReflectionTestUtils.setField(matchGrpcClient, "deadlineSeconds", 5L);
    }

    @Test
    void superHi_shouldPassThroughClientRequestIdUnchanged() {
        when(matchServiceStub.withDeadlineAfter(anyLong(), any(TimeUnit.class))).thenReturn(matchServiceStub);
        when(matchServiceStub.superHi(any())).thenReturn(SuperHiResp.newBuilder().setMatchId(1L).build());

        SuperHiReq req = new SuperHiReq();
        req.setTargetUserId(10002L);
        req.setClientRequestId("gw3-superhi-001");

        matchGrpcClient.superHi(20002L, req);

        ArgumentCaptor<com.dating.match.grpc.proto.SuperHiReq> captor =
                ArgumentCaptor.forClass(com.dating.match.grpc.proto.SuperHiReq.class);
        verify(matchServiceStub).superHi(captor.capture());
        assertEquals(20002L, captor.getValue().getCallerUserId());
        assertEquals(10002L, captor.getValue().getTargetUserId());
        assertEquals("gw3-superhi-001", captor.getValue().getClientRequestId());
    }

    @Test
    void swipe_shouldSetCallerUserIdInRequest() {
        when(matchServiceStub.withDeadlineAfter(anyLong(), any(TimeUnit.class))).thenReturn(matchServiceStub);
        when(matchServiceStub.swipe(any())).thenReturn(SwipeResp.getDefaultInstance());

        com.dating.gateway.dto.req.SwipeReq req = new com.dating.gateway.dto.req.SwipeReq();
        req.setTargetUserId(10003L);
        req.setDirection("RIGHT");

        matchGrpcClient.swipe(40004L, req);

        ArgumentCaptor<com.dating.match.grpc.proto.SwipeReq> captor =
                ArgumentCaptor.forClass(com.dating.match.grpc.proto.SwipeReq.class);
        verify(matchServiceStub).swipe(captor.capture());
        assertEquals(40004L, captor.getValue().getCallerUserId());
    }

    @Test
    void unavailable_shouldMapToUpstreamUnavailable() {
        when(matchServiceStub.withDeadlineAfter(anyLong(), any(TimeUnit.class))).thenReturn(matchServiceStub);
        when(matchServiceStub.getQuota(any())).thenThrow(Status.UNAVAILABLE.asRuntimeException());

        GatewayBizException ex = assertThrows(GatewayBizException.class, () -> matchGrpcClient.getQuota(1L));
        assertEquals(GatewayErrorCode.UPSTREAM_UNAVAILABLE, ex.getErrorCode());
    }

    @Test
    void invalidArgument_shouldMapToParamError() {
        when(matchServiceStub.withDeadlineAfter(anyLong(), any(TimeUnit.class))).thenReturn(matchServiceStub);
        when(matchServiceStub.getQuota(any()))
                .thenThrow(Status.INVALID_ARGUMENT.withDescription("INVALID_ARGUMENT").asRuntimeException());

        GatewayBizException ex = assertThrows(GatewayBizException.class, () -> matchGrpcClient.getQuota(1L));
        assertEquals(GatewayErrorCode.INVALID_ARGUMENT, ex.getErrorCode());
    }

    @Test
    void resourceExhausted_shouldMapToQuotaExhausted() {
        when(matchServiceStub.withDeadlineAfter(anyLong(), any(TimeUnit.class))).thenReturn(matchServiceStub);
        when(matchServiceStub.swipe(any()))
                .thenThrow(Status.RESOURCE_EXHAUSTED.withDescription("QUOTA_CARD_EXCEEDED").asRuntimeException());

        GatewayBizException ex = assertThrows(GatewayBizException.class, () -> {
            com.dating.gateway.dto.req.SwipeReq req = new com.dating.gateway.dto.req.SwipeReq();
            req.setTargetUserId(2L);
            req.setDirection("RIGHT");
            matchGrpcClient.swipe(1L, req);
        });
        assertEquals(GatewayErrorCode.QUOTA_EXHAUSTED, ex.getErrorCode());
    }
}
