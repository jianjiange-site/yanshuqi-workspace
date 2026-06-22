package com.dating.match.grpc;

import com.dating.match.dto.ListMatchesResult;
import com.dating.match.dto.ListVisitsResult;
import com.dating.match.dto.MatchInfoDto;
import com.dating.match.dto.VisitInfoDto;
import com.dating.match.exception.MatchBizException;
import com.dating.match.exception.MatchErrorCode;
import com.dating.match.grpc.proto.ListMatchesReq;
import com.dating.match.grpc.proto.ListMatchesResp;
import com.dating.match.grpc.proto.ListVisitsReq;
import com.dating.match.grpc.proto.ListVisitsResp;
import com.dating.match.grpc.proto.RecordVisitReq;
import com.dating.match.grpc.proto.RecordVisitResp;
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

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchGrpcServiceQueryTest {

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
    void grpcListMatches_shouldReturnRealList() {
        MatchInfoDto dto = new MatchInfoDto();
        dto.setMatchId(90001L);
        dto.setPartnerUserId(20002L);
        dto.setPartnerNickname("Nick");
        dto.setPartnerPhotoKeys(List.of("photo/1.jpg"));
        dto.setMatchedAtMs(1_700_000_000_000L);
        dto.setSource("SWIPE_MATCH");
        when(matchQueryService.listMatches(10001L, 20, ""))
                .thenReturn(new ListMatchesResult(List.of(dto), "token-next"));

        AtomicReference<ListMatchesResp> captured = new AtomicReference<>();
        matchGrpcService.listMatches(
                ListMatchesReq.newBuilder().setCallerUserId(10001L).setPageSize(20).build(),
                observer(captured));

        ListMatchesResp resp = captured.get();
        assertEquals(1, resp.getMatchesCount());
        assertEquals(90001L, resp.getMatches(0).getMatchId());
        assertEquals(20002L, resp.getMatches(0).getPartnerUserId());
        assertEquals("Nick", resp.getMatches(0).getPartnerNickname());
        assertEquals(1_700_000_000_000L, resp.getMatches(0).getMatchedAtMs());
        assertEquals("token-next", resp.getNextPageToken());
    }

    @Test
    void grpcRecordVisit_shouldReturnSuccess() {
        AtomicReference<RecordVisitResp> captured = new AtomicReference<>();
        matchGrpcService.recordVisit(
                RecordVisitReq.newBuilder().setCallerUserId(10001L).setTargetUserId(20002L).build(),
                observer(captured));
        assertTrue(captured.get().getSuccess());
        verify(profileVisitService).recordVisit(10001L, 20002L);
    }

    @Test
    void grpcListVisits_shouldReturnRealList() {
        VisitInfoDto dto = new VisitInfoDto();
        dto.setVisitId(70001L);
        dto.setFromUserId(20002L);
        dto.setVisitCount(2);
        dto.setFirstVisitedAtMs(1_700_000_000_000L);
        dto.setLastVisitedAtMs(1_700_000_100_000L);
        when(profileVisitQueryService.listVisits(10001L, 20, ""))
                .thenReturn(new ListVisitsResult(List.of(dto), "visit-token"));

        AtomicReference<ListVisitsResp> captured = new AtomicReference<>();
        matchGrpcService.listVisits(
                ListVisitsReq.newBuilder().setCallerUserId(10001L).setPageSize(20).build(),
                observer(captured));

        ListVisitsResp resp = captured.get();
        assertEquals(1, resp.getVisitsCount());
        assertEquals(70001L, resp.getVisits(0).getVisitId());
        assertEquals(2, resp.getVisits(0).getVisitCount());
        assertEquals(1_700_000_000_000L, resp.getVisits(0).getFirstVisitedAtMs());
        assertEquals(1_700_000_100_000L, resp.getVisits(0).getLastVisitedAtMs());
    }

    @Test
    void grpcRecordVisit_selfVisit_shouldPropagateInvalidArgument() {
        doThrow(new MatchBizException(MatchErrorCode.INVALID_ARGUMENT))
                .when(profileVisitService).recordVisit(10001L, 10001L);
        assertThrows(RuntimeException.class, () -> matchGrpcService.recordVisit(
                RecordVisitReq.newBuilder().setCallerUserId(10001L).setTargetUserId(10001L).build(),
                observer(new AtomicReference<>())));
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
