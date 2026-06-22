package com.dating.match.grpc;

import com.dating.match.dto.SuperHiResult;
import com.dating.match.dto.SwipeResult;
import com.dating.match.grpc.proto.SuperHiReq;
import com.dating.match.grpc.proto.SuperHiResp;
import com.dating.match.grpc.proto.SwipeDirection;
import com.dating.match.grpc.proto.SwipeReq;
import com.dating.match.grpc.proto.SwipeResp;
import com.dating.match.service.FeedService;
import com.dating.match.service.MatchQueryService;
import com.dating.match.service.ProfileVisitQueryService;
import com.dating.match.service.ProfileVisitService;
import com.dating.match.service.QuotaService;
import com.dating.match.service.SwipeService;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchGrpcServiceMatchTest {

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
  void grpcRightBhMutual_shouldReturnRealMatchId() {
    when(swipeService.swipe(10001L, 20002L, SwipeDirection.RIGHT)).thenReturn(new SwipeResult(77701L));
    AtomicReference<SwipeResp> captured = new AtomicReference<>();
    matchGrpcService.swipe(
        SwipeReq.newBuilder().setCallerUserId(10001L).setTargetUserId(20002L).setDirection(SwipeDirection.RIGHT).build(),
        observer(captured));
    assertEquals(77701L, captured.get().getMatchId());
  }

  @Test
  void grpcRightDh_shouldReturnZero() {
    when(swipeService.swipe(10001L, 30003L, SwipeDirection.RIGHT)).thenReturn(new SwipeResult(0L));
    AtomicReference<SwipeResp> captured = new AtomicReference<>();
    matchGrpcService.swipe(
        SwipeReq.newBuilder().setCallerUserId(10001L).setTargetUserId(30003L).setDirection(SwipeDirection.RIGHT).build(),
        observer(captured));
    assertEquals(0L, captured.get().getMatchId());
  }

  @Test
  void grpcSuperHi_shouldReturnRealMatchId() {
    when(swipeService.superHi(10001L, 20002L, "req-1")).thenReturn(new SuperHiResult(88801L, 100));
    AtomicReference<SuperHiResp> captured = new AtomicReference<>();
    matchGrpcService.superHi(
        SuperHiReq.newBuilder().setCallerUserId(10001L).setTargetUserId(20002L).setClientRequestId("req-1").build(),
        observer(captured));
    assertEquals(88801L, captured.get().getMatchId());
    assertEquals(100, captured.get().getCoinsUsed());
  }

  @Test
  void grpcSuperHiRetry_shouldReturnSameMatchId() {
    when(swipeService.superHi(10001L, 20002L, "req-retry")).thenReturn(new SuperHiResult(88802L, 0));
    AtomicReference<SuperHiResp> first = new AtomicReference<>();
    AtomicReference<SuperHiResp> second = new AtomicReference<>();
    matchGrpcService.superHi(
        SuperHiReq.newBuilder().setCallerUserId(10001L).setTargetUserId(20002L).setClientRequestId("req-retry").build(),
        observer(first));
    matchGrpcService.superHi(
        SuperHiReq.newBuilder().setCallerUserId(10001L).setTargetUserId(20002L).setClientRequestId("req-retry").build(),
        observer(second));
    assertEquals(88802L, first.get().getMatchId());
    assertEquals(88802L, second.get().getMatchId());
    verify(swipeService, times(2)).superHi(10001L, 20002L, "req-retry");
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
