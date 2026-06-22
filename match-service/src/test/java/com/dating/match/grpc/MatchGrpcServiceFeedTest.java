package com.dating.match.grpc;

import com.dating.match.constant.UserTypeConstant;
import com.dating.match.dto.GetTodayFeedResult;
import com.dating.match.exception.MatchBizException;
import com.dating.match.exception.MatchErrorCode;
import com.dating.match.exception.MatchGrpcStatusMapper;
import com.dating.match.grpc.proto.GetTodayFeedReq;
import com.dating.match.grpc.proto.GetTodayFeedResp;
import com.dating.match.recommend.CandidateProfile;
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

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchGrpcServiceFeedTest {

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
  void grpcGetTodayFeed_shouldReturnCards() {
    CandidateProfile profile = new CandidateProfile();
    profile.setUserId(30001L);
    profile.setUserType(UserTypeConstant.DH);
    profile.setNickname("DH-30001");
    profile.setAge(25);
    profile.setBio("bio");
    profile.setDistanceKm(-1);
    profile.setStateCode("CA");
    profile.setCity("Los Angeles");
    profile.setPhotoKeys(List.of("photo/30001/1.jpg"));
    when(feedService.getTodayFeed(10001L, 5)).thenReturn(new GetTodayFeedResult(List.of(profile), false));

    AtomicReference<GetTodayFeedResp> captured = new AtomicReference<>();
    matchGrpcService.getTodayFeed(
        GetTodayFeedReq.newBuilder().setCallerUserId(10001L).setCount(5).build(),
        observer(captured));

    GetTodayFeedResp resp = captured.get();
    assertEquals(1, resp.getCardsCount());
    assertEquals("CA", resp.getCards(0).getStateCode());
    assertEquals("Los Angeles", resp.getCards(0).getCity());
    assertEquals(-1D, resp.getCards(0).getDistanceKm(), 0.001);
  }

  @Test
  void grpcGetTodayFeed_shouldReturnExhausted() {
    when(feedService.getTodayFeed(10001L, 5)).thenReturn(new GetTodayFeedResult(List.of(), true));
    AtomicReference<GetTodayFeedResp> captured = new AtomicReference<>();
    matchGrpcService.getTodayFeed(
        GetTodayFeedReq.newBuilder().setCallerUserId(10001L).setCount(5).build(),
        observer(captured));
    assertTrue(captured.get().getExhausted());
    assertEquals(0, captured.get().getCardsCount());
  }

  @Test
  void bizException_shouldMapToGrpcStatus() {
    StatusRuntimeException ex = MatchGrpcStatusMapper.toRuntimeException(
        new MatchBizException(MatchErrorCode.INVALID_ARGUMENT));
    assertEquals(Status.Code.INVALID_ARGUMENT, ex.getStatus().getCode());
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
