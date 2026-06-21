package com.dating.gateway.service;

import com.dating.gateway.adapter.MatchProtoAdapter;
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
import com.dating.match.grpc.proto.SuperHiResp;
import com.dating.match.grpc.proto.SwipeResp;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * match-service gRPC 客户端封装。
 * <p>
 * 阶段 1 通过真实 gRPC stub 调用 match-service 契约骨架；测试 profile 下由 MockBean 替代。
 */
@Service
@Profile("!test")
public class MatchGrpcClient {

    @GrpcClient("match-service")
    private MatchServiceGrpc.MatchServiceBlockingStub matchServiceStub;

    public GetTodayFeedResp getTodayFeed(long callerUserId, int count) {
        GetTodayFeedReq request = GetTodayFeedReq.newBuilder()
                .setCallerUserId(callerUserId)
                .setCount(count)
                .build();
        return matchServiceStub.getTodayFeed(request);
    }

    public SwipeResp swipe(long callerUserId, com.dating.gateway.dto.req.SwipeReq req) {
        com.dating.match.grpc.proto.SwipeReq request = com.dating.match.grpc.proto.SwipeReq.newBuilder()
                .setCallerUserId(callerUserId)
                .setTargetUserId(req.getTargetUserId() == null ? 0L : req.getTargetUserId())
                .setDirection(MatchProtoAdapter.toSwipeDirection(req.getDirection()))
                .build();
        return matchServiceStub.swipe(request);
    }

    public SuperHiResp superHi(long callerUserId, com.dating.gateway.dto.req.SuperHiReq req) {
        com.dating.match.grpc.proto.SuperHiReq request = com.dating.match.grpc.proto.SuperHiReq.newBuilder()
                .setCallerUserId(callerUserId)
                .setTargetUserId(req.getTargetUserId() == null ? 0L : req.getTargetUserId())
                .setClientRequestId(StringUtils.hasText(req.getClientRequestId()) ? req.getClientRequestId() : "")
                .build();
        return matchServiceStub.superHi(request);
    }

    public GetQuotaResp getQuota(long callerUserId) {
        GetQuotaReq request = GetQuotaReq.newBuilder()
                .setCallerUserId(callerUserId)
                .build();
        return matchServiceStub.getQuota(request);
    }

    public ListMatchesResp listMatches(long callerUserId, int pageSize, String pageToken) {
        ListMatchesReq request = ListMatchesReq.newBuilder()
                .setCallerUserId(callerUserId)
                .setPageSize(pageSize)
                .setPageToken(pageToken == null ? "" : pageToken)
                .build();
        return matchServiceStub.listMatches(request);
    }

    public RecordVisitResp recordVisit(long callerUserId, long targetUserId) {
        RecordVisitReq request = RecordVisitReq.newBuilder()
                .setCallerUserId(callerUserId)
                .setTargetUserId(targetUserId)
                .build();
        return matchServiceStub.recordVisit(request);
    }

    public ListVisitsResp listVisits(long callerUserId, int pageSize, String pageToken) {
        ListVisitsReq request = ListVisitsReq.newBuilder()
                .setCallerUserId(callerUserId)
                .setPageSize(pageSize)
                .setPageToken(pageToken == null ? "" : pageToken)
                .build();
        return matchServiceStub.listVisits(request);
    }
}
