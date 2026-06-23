package com.dating.gateway.service;

import com.dating.gateway.adapter.MatchProtoAdapter;
import com.dating.gateway.dto.req.SuperHiReq;
import com.dating.gateway.dto.req.SwipeReq;
import com.dating.gateway.exception.GatewayBizException;
import com.dating.gateway.exception.GatewayGrpcExceptionMapper;
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
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * match-service gRPC 客户端：仅负责 request 构建、stub 调用、deadline 与异常转换，不含业务逻辑。
 * <p>
 * callerUserId 由 Controller 经 JWT {@link com.dating.gateway.resolver.CallerUserResolver} 解析后传入。
 * traceId 透传待接入全局 gRPC ClientInterceptor（当前由 logback MDC 记录 HTTP 侧 traceId）。
 */
@Service
@Profile("!test")
public class MatchGrpcClient {

    @GrpcClient("match-service")
    private MatchServiceGrpc.MatchServiceBlockingStub matchServiceStub;

    @Value("${gateway.grpc.deadline-seconds:5}")
    private long deadlineSeconds;

    public GetTodayFeedResp getTodayFeed(long callerUserId, int count) {
        GetTodayFeedReq request = GetTodayFeedReq.newBuilder()
                .setCallerUserId(callerUserId)
                .setCount(count)
                .build();
        return invoke(() -> stubWithDeadline().getTodayFeed(request));
    }

    public SwipeResp swipe(long callerUserId, SwipeReq req) {
        com.dating.match.grpc.proto.SwipeReq request = com.dating.match.grpc.proto.SwipeReq.newBuilder()
                .setCallerUserId(callerUserId)
                .setTargetUserId(req.getTargetUserId())
                .setDirection(MatchProtoAdapter.toSwipeDirection(req.getDirection()))
                .build();
        return invoke(() -> stubWithDeadline().swipe(request));
    }

    /**
     * SuperHi：clientRequestId 原样写入 proto，不做重写或空串兜底。
     */
    public SuperHiResp superHi(long callerUserId, SuperHiReq req) {
        com.dating.match.grpc.proto.SuperHiReq request = com.dating.match.grpc.proto.SuperHiReq.newBuilder()
                .setCallerUserId(callerUserId)
                .setTargetUserId(req.getTargetUserId())
                .setClientRequestId(req.getClientRequestId())
                .build();
        return invoke(() -> stubWithDeadline().superHi(request));
    }

    public GetQuotaResp getQuota(long callerUserId) {
        GetQuotaReq request = GetQuotaReq.newBuilder()
                .setCallerUserId(callerUserId)
                .build();
        return invoke(() -> stubWithDeadline().getQuota(request));
    }

    public ListMatchesResp listMatches(long callerUserId, int pageSize, String pageToken) {
        ListMatchesReq request = ListMatchesReq.newBuilder()
                .setCallerUserId(callerUserId)
                .setPageSize(pageSize)
                .setPageToken(pageToken == null ? "" : pageToken)
                .build();
        return invoke(() -> stubWithDeadline().listMatches(request));
    }

    public RecordVisitResp recordVisit(long callerUserId, long targetUserId) {
        RecordVisitReq request = RecordVisitReq.newBuilder()
                .setCallerUserId(callerUserId)
                .setTargetUserId(targetUserId)
                .build();
        return invoke(() -> stubWithDeadline().recordVisit(request));
    }

    public ListVisitsResp listVisits(long callerUserId, int pageSize, String pageToken) {
        ListVisitsReq request = ListVisitsReq.newBuilder()
                .setCallerUserId(callerUserId)
                .setPageSize(pageSize)
                .setPageToken(pageToken == null ? "" : pageToken)
                .build();
        return invoke(() -> stubWithDeadline().listVisits(request));
    }

    private MatchServiceGrpc.MatchServiceBlockingStub stubWithDeadline() {
        return matchServiceStub.withDeadlineAfter(deadlineSeconds, TimeUnit.SECONDS);
    }

    private <T> T invoke(GrpcCall<T> call) {
        try {
            return call.execute();
        } catch (StatusRuntimeException ex) {
            throw GatewayGrpcExceptionMapper.toGatewayException(ex);
        }
    }

    @FunctionalInterface
    interface GrpcCall<T> {
        T execute();
    }
}
