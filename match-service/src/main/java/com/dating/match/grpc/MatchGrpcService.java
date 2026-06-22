package com.dating.match.grpc;

import com.dating.match.dto.GetTodayFeedResult;
import com.dating.match.dto.ListMatchesResult;
import com.dating.match.dto.ListVisitsResult;
import com.dating.match.dto.MatchInfoDto;
import com.dating.match.dto.SuperHiResult;
import com.dating.match.dto.SwipeResult;
import com.dating.match.dto.VisitInfoDto;
import com.dating.match.grpc.proto.GetQuotaReq;
import com.dating.match.grpc.proto.GetQuotaResp;
import com.dating.match.grpc.proto.GetTodayFeedReq;
import com.dating.match.grpc.proto.GetTodayFeedResp;
import com.dating.match.grpc.proto.ListMatchesReq;
import com.dating.match.grpc.proto.ListMatchesResp;
import com.dating.match.grpc.proto.ListVisitsReq;
import com.dating.match.grpc.proto.ListVisitsResp;
import com.dating.match.grpc.proto.MatchCard;
import com.dating.match.grpc.proto.MatchInfo;
import com.dating.match.grpc.proto.MatchServiceGrpc;
import com.dating.match.grpc.proto.RecordVisitReq;
import com.dating.match.grpc.proto.RecordVisitResp;
import com.dating.match.grpc.proto.SuperHiReq;
import com.dating.match.grpc.proto.SuperHiResp;
import com.dating.match.grpc.proto.SwipeReq;
import com.dating.match.grpc.proto.SwipeResp;
import com.dating.match.grpc.proto.VisitInfo;
import com.dating.match.recommend.CandidateProfile;
import com.dating.match.service.FeedService;
import com.dating.match.service.MatchQueryService;
import com.dating.match.service.ProfileVisitQueryService;
import com.dating.match.service.ProfileVisitService;
import com.dating.match.service.QuotaService;
import com.dating.match.service.SwipeService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.context.annotation.Profile;

/**
 * Match gRPC 服务：除 D1 推荐增强外，Swagger Match 核心接口均已接入真实逻辑。
 */
@GrpcService
@Profile("!test")
public class MatchGrpcService extends MatchServiceGrpc.MatchServiceImplBase {

    private final FeedService feedService;
    private final SwipeService swipeService;
    private final QuotaService quotaService;
    private final MatchQueryService matchQueryService;
    private final ProfileVisitService profileVisitService;
    private final ProfileVisitQueryService profileVisitQueryService;

    public MatchGrpcService(FeedService feedService,
                            SwipeService swipeService,
                            QuotaService quotaService,
                            MatchQueryService matchQueryService,
                            ProfileVisitService profileVisitService,
                            ProfileVisitQueryService profileVisitQueryService) {
        this.feedService = feedService;
        this.swipeService = swipeService;
        this.quotaService = quotaService;
        this.matchQueryService = matchQueryService;
        this.profileVisitService = profileVisitService;
        this.profileVisitQueryService = profileVisitQueryService;
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
        ListMatchesResult result = matchQueryService.listMatches(
                request.getCallerUserId(),
                request.getPageSize(),
                request.getPageToken());
        ListMatchesResp.Builder builder = ListMatchesResp.newBuilder()
                .setNextPageToken(result.getNextPageToken());
        for (MatchInfoDto dto : result.getMatches()) {
            builder.addMatches(toMatchInfo(dto));
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void recordVisit(RecordVisitReq request, StreamObserver<RecordVisitResp> responseObserver) {
        profileVisitService.recordVisit(request.getCallerUserId(), request.getTargetUserId());
        RecordVisitResp response = RecordVisitResp.newBuilder()
                .setSuccess(true)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void listVisits(ListVisitsReq request, StreamObserver<ListVisitsResp> responseObserver) {
        ListVisitsResult result = profileVisitQueryService.listVisits(
                request.getCallerUserId(),
                request.getPageSize(),
                request.getPageToken());
        ListVisitsResp.Builder builder = ListVisitsResp.newBuilder()
                .setNextPageToken(result.getNextPageToken());
        for (VisitInfoDto dto : result.getVisits()) {
            builder.addVisits(toVisitInfo(dto));
        }
        responseObserver.onNext(builder.build());
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

    private static MatchInfo toMatchInfo(MatchInfoDto dto) {
        MatchInfo.Builder builder = MatchInfo.newBuilder()
                .setMatchId(dto.getMatchId())
                .setPartnerUserId(dto.getPartnerUserId())
                .setPartnerNickname(dto.getPartnerNickname() == null ? "" : dto.getPartnerNickname())
                .setMatchedAtMs(dto.getMatchedAtMs())
                .setSource(dto.getSource() == null ? "" : dto.getSource());
        if (dto.getPartnerPhotoKeys() != null) {
            builder.addAllPartnerPhotoKeys(dto.getPartnerPhotoKeys());
        }
        return builder.build();
    }

    private static VisitInfo toVisitInfo(VisitInfoDto dto) {
        return VisitInfo.newBuilder()
                .setVisitId(dto.getVisitId())
                .setFromUserId(dto.getFromUserId())
                .setVisitCount(dto.getVisitCount())
                .setFirstVisitedAtMs(dto.getFirstVisitedAtMs())
                .setLastVisitedAtMs(dto.getLastVisitedAtMs())
                .build();
    }
}
