package com.dating.match.grpc;

import com.dating.match.dto.GetTodayFeedResult;
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
import com.dating.match.grpc.proto.MatchCard;
import com.dating.match.grpc.proto.MatchServiceGrpc;
import com.dating.match.grpc.proto.RecordVisitReq;
import com.dating.match.grpc.proto.RecordVisitResp;
import com.dating.match.grpc.proto.SuperHiReq;
import com.dating.match.grpc.proto.SuperHiResp;
import com.dating.match.grpc.proto.SwipeReq;
import com.dating.match.grpc.proto.SwipeResp;
import com.dating.match.recommend.CandidateProfile;
import com.dating.match.service.FeedService;
import com.dating.match.service.QuotaService;
import com.dating.match.service.SwipeService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.context.annotation.Profile;

/**
 * Match gRPC 服务：Swipe / SuperHi / GetQuota / GetTodayFeed 接入真实逻辑；Match/Visit 仍为 mock。
 */
@GrpcService
@Profile("!test")
public class MatchGrpcService extends MatchServiceGrpc.MatchServiceImplBase {

    private final FeedService feedService;
    private final SwipeService swipeService;
    private final QuotaService quotaService;

    public MatchGrpcService(FeedService feedService,
                            SwipeService swipeService,
                            QuotaService quotaService) {
        this.feedService = feedService;
        this.swipeService = swipeService;
        this.quotaService = quotaService;
    }

    @Override
    public void getTodayFeed(GetTodayFeedReq request, StreamObserver<GetTodayFeedResp> responseObserver) {
        GetTodayFeedResult result = feedService.getTodayFeed(request.getCallerUserId(), request.getCount());
        GetTodayFeedResp.Builder builder = GetTodayFeedResp.newBuilder()
                .setExhausted(result.isExhausted());
        for (CandidateProfile profile : result.getCards()) {
            builder.addCards(toMatchCard(profile));
        }
        responseObserver.onNext(builder.build());
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

    private static MatchCard toMatchCard(CandidateProfile profile) {
        MatchCard.Builder builder = MatchCard.newBuilder()
                .setTargetUserId(profile.getUserId())
                .setTargetUserType(profile.getUserType())
                .setNickname(profile.getNickname() == null ? "" : profile.getNickname())
                .setAge(profile.getAge())
                .setBio(profile.getBio() == null ? "" : profile.getBio())
                .setDistanceKm(profile.getDistanceKm())
                .setStateCode(profile.getStateCode() == null ? "" : profile.getStateCode())
                .setCity(profile.getCity() == null ? "" : profile.getCity());
        if (profile.getPhotoKeys() != null) {
            builder.addAllPhotoKeys(profile.getPhotoKeys());
        }
        return builder.build();
    }
}
