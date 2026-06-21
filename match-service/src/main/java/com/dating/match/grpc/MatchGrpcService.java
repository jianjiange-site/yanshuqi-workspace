package com.dating.match.grpc;

import com.dating.match.dto.SuperHiResult;
import com.dating.match.dto.SwipeResult;
import com.dating.match.grpc.proto.GetQuotaReq;
import com.dating.match.grpc.proto.GetQuotaResp;
import com.dating.match.grpc.proto.GetTodayFeedReq;
import com.dating.match.grpc.proto.GetTodayFeedResp;
import com.dating.match.grpc.proto.ListMatchesReq;
import com.dating.match.grpc.proto.ListMatchesResp;
import com.dating.match.grpc.proto.ListVisitsReq;
import com.dating.match.grpc.proto.ListVisitsResp;
import com.dating.match.grpc.proto.MatchServiceGrpc;
import com.dating.match.grpc.proto.RecordVisitReq;
import com.dating.match.grpc.proto.RecordVisitResp;
import com.dating.match.grpc.proto.SuperHiReq;
import com.dating.match.grpc.proto.SuperHiResp;
import com.dating.match.grpc.proto.SwipeReq;
import com.dating.match.grpc.proto.SwipeResp;
import com.dating.match.service.QuotaService;
import com.dating.match.service.SwipeService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.context.annotation.Profile;

/**
 * Match gRPC 服务：Swipe / SuperHi / GetQuota 接入阶段 3 真实逻辑；Feed/Match/Visit 仍为 mock。
 */
@GrpcService
@Profile("!test")
public class MatchGrpcService extends MatchServiceGrpc.MatchServiceImplBase {

    private final SwipeService swipeService;
    private final QuotaService quotaService;

    public MatchGrpcService(SwipeService swipeService, QuotaService quotaService) {
        this.swipeService = swipeService;
        this.quotaService = quotaService;
    }

    @Override
    public void getTodayFeed(GetTodayFeedReq request, StreamObserver<GetTodayFeedResp> responseObserver) {
        GetTodayFeedResp response = GetTodayFeedResp.newBuilder()
                .setExhausted(false)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void swipe(SwipeReq request, StreamObserver<SwipeResp> responseObserver) {
        SwipeResult result = swipeService.swipe(
                request.getCallerUserId(),
                request.getTargetUserId(),
                request.getDirection());
        SwipeResp response = SwipeResp.newBuilder()
                .setMatchId(result.getMatchId())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void superHi(SuperHiReq request, StreamObserver<SuperHiResp> responseObserver) {
        SuperHiResult result = swipeService.superHi(
                request.getCallerUserId(),
                request.getTargetUserId(),
                request.getClientRequestId());
        SuperHiResp response = SuperHiResp.newBuilder()
                .setMatchId(result.getMatchId())
                .setCoinsUsed(result.getCoinsUsed())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getQuota(GetQuotaReq request, StreamObserver<GetQuotaResp> responseObserver) {
        GetQuotaResp response = quotaService.buildQuotaResponse(request.getCallerUserId());
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void listMatches(ListMatchesReq request, StreamObserver<ListMatchesResp> responseObserver) {
        ListMatchesResp response = ListMatchesResp.newBuilder()
                .setNextPageToken("")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void recordVisit(RecordVisitReq request, StreamObserver<RecordVisitResp> responseObserver) {
        RecordVisitResp response = RecordVisitResp.newBuilder()
                .setSuccess(true)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void listVisits(ListVisitsReq request, StreamObserver<ListVisitsResp> responseObserver) {
        ListVisitsResp response = ListVisitsResp.newBuilder()
                .setNextPageToken("")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
