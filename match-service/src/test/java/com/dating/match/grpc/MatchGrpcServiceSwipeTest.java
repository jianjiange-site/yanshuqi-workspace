package com.dating.match.grpc;

import com.dating.match.dto.SuperHiResult;
import com.dating.match.dto.SwipeResult;
import com.dating.match.exception.MatchBizException;
import com.dating.match.exception.MatchErrorCode;
import com.dating.match.grpc.proto.GetQuotaReq;
import com.dating.match.grpc.proto.GetQuotaResp;
import com.dating.match.grpc.proto.SuperHiReq;
import com.dating.match.grpc.proto.SuperHiResp;
import com.dating.match.grpc.proto.SwipeDirection;
import com.dating.match.grpc.proto.SwipeReq;
import com.dating.match.grpc.proto.SwipeResp;
import com.dating.match.exception.MatchGrpcStatusMapper;
import com.dating.match.service.FeedService;
import com.dating.match.service.MatchQueryService;
import com.dating.match.service.ProfileVisitQueryService;
import com.dating.match.service.ProfileVisitService;
import com.dating.match.service.QuotaService;
import com.dating.match.service.SwipeService;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchGrpcServiceSwipeTest {

    @Mock
    private FeedService feedService;

    @Mock
    private SwipeService swipeService;

    @Mock
    private QuotaService quotaService;

    @Mock
    private MatchQueryService matchQueryService;

    @Mock
    private ProfileVisitService profileVisitService;

    @Mock
    private ProfileVisitQueryService profileVisitQueryService;

    private MatchGrpcService matchGrpcService;

    @BeforeEach
    void setUp() {
        matchGrpcService = new MatchGrpcService(
                feedService, swipeService, quotaService,
                matchQueryService, profileVisitService, profileVisitQueryService);
    }

    @Test
    void grpcSwipeLeft_shouldSucceed() {
        when(swipeService.swipe(10001L, 20002L, SwipeDirection.LEFT)).thenReturn(new SwipeResult(0L));
        AtomicReference<SwipeResp> captured = new AtomicReference<>();
        matchGrpcService.swipe(
                SwipeReq.newBuilder().setCallerUserId(10001L).setTargetUserId(20002L).setDirection(SwipeDirection.LEFT).build(),
                observer(captured));
        assertEquals(0L, captured.get().getMatchId());
    }

    @Test
    void grpcSwipeRight_shouldSucceed() {
        when(swipeService.swipe(10001L, 20002L, SwipeDirection.RIGHT)).thenReturn(new SwipeResult(0L));
        AtomicReference<SwipeResp> captured = new AtomicReference<>();
        matchGrpcService.swipe(
                SwipeReq.newBuilder().setCallerUserId(10001L).setTargetUserId(20002L).setDirection(SwipeDirection.RIGHT).build(),
                observer(captured));
        assertEquals(0L, captured.get().getMatchId());
    }

    @Test
    void grpcSuperHi_shouldReturnCoinsUsed() {
        when(swipeService.superHi(10001L, 20002L, "req-1")).thenReturn(new SuperHiResult(0L, 100));
        AtomicReference<SuperHiResp> captured = new AtomicReference<>();
        matchGrpcService.superHi(
                SuperHiReq.newBuilder().setCallerUserId(10001L).setTargetUserId(20002L).setClientRequestId("req-1").build(),
                observer(captured));
        assertEquals(100, captured.get().getCoinsUsed());
    }

    @Test
    void grpcGetQuota_shouldUseQuotaService() {
        when(quotaService.buildQuotaResponse(10001L)).thenReturn(
                GetQuotaResp.newBuilder().setTier("FREE").setDailyCardUsed(3).setDailyCardLimit(50).build());
        AtomicReference<GetQuotaResp> captured = new AtomicReference<>();
        matchGrpcService.getQuota(GetQuotaReq.newBuilder().setCallerUserId(10001L).build(), observer(captured));
        assertEquals(3, captured.get().getDailyCardUsed());
    }

    @Test
    void bizException_shouldMapToGrpcStatus() {
        StatusRuntimeException ex = MatchGrpcStatusMapper.toRuntimeException(
                new MatchBizException(MatchErrorCode.QUOTA_CARD_EXCEEDED));
        assertEquals(Status.Code.RESOURCE_EXHAUSTED, ex.getStatus().getCode());
    }

    private static <T> StreamObserver<T> observer(AtomicReference<T> captured) {
        return new StreamObserver<>() {
            @Override
            public void onNext(T value) {
                captured.set(value);
            }

            @Override
            public void onError(Throwable t) {
                throw new RuntimeException(t);
            }

            @Override
            public void onCompleted() {
            }
        };
    }
}
