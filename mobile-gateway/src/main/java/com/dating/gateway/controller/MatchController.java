package com.dating.gateway.controller;

import com.dating.gateway.adapter.MatchProtoAdapter;
import com.dating.gateway.common.Result;
import com.dating.gateway.dto.req.SuperHiReq;
import com.dating.gateway.dto.req.SwipeReq;
import com.dating.gateway.dto.vo.MatchFeedVO;
import com.dating.gateway.dto.vo.MatchListVO;
import com.dating.gateway.dto.vo.MatchQuotaVO;
import com.dating.gateway.dto.vo.SuperHiResultVO;
import com.dating.gateway.dto.vo.SwipeResultVO;
import com.dating.gateway.dto.vo.VisitListVO;
import com.dating.gateway.security.CallerUserResolver;
import com.dating.gateway.service.MatchGrpcClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Match REST 契约入口 — 阶段 1 仅做参数接收、callerUserId 解析、gRPC 转发与 VO 转换。
 */
@RestController
@RequestMapping("/api/v1/match")
@Tag(name = "Match", description = "推荐、划卡、匹配、配额、访问")
public class MatchController {

    private static final int DEFAULT_FEED_COUNT = 5;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final CallerUserResolver callerUserResolver;
    private final MatchGrpcClient matchGrpcClient;

    public MatchController(CallerUserResolver callerUserResolver, MatchGrpcClient matchGrpcClient) {
        this.callerUserResolver = callerUserResolver;
        this.matchGrpcClient = matchGrpcClient;
    }

    @GetMapping("/feed")
    @Operation(summary = "拉当日 feed 下一批(App 端固定 count=5;LPOP 即消费 + 二次过滤)")
    public Result<MatchFeedVO> getFeed(HttpServletRequest request,
                                       @RequestParam(value = "count", defaultValue = "5") int count) {
        long callerUserId = callerUserResolver.resolveCallerUserId(request);
        MatchFeedVO data = MatchProtoAdapter.toMatchFeedVO(
                matchGrpcClient.getTodayFeed(callerUserId, count > 0 ? count : DEFAULT_FEED_COUNT));
        return Result.ok(data);
    }

    @PostMapping("/swipe")
    @Operation(summary = "划卡(LEFT / RIGHT)")
    public Result<SwipeResultVO> swipe(HttpServletRequest request, @RequestBody SwipeReq req) {
        long callerUserId = callerUserResolver.resolveCallerUserId(request);
        SwipeResultVO data = MatchProtoAdapter.toSwipeResultVO(matchGrpcClient.swipe(callerUserId, req));
        return Result.ok(data);
    }

    @PostMapping("/super-hi")
    @Operation(summary = "SuperHi(订阅赠送 1 次/天,否则扣 100 金币;BH/DH 一律立即匹配)")
    public Result<SuperHiResultVO> superHi(HttpServletRequest request, @RequestBody SuperHiReq req) {
        long callerUserId = callerUserResolver.resolveCallerUserId(request);
        SuperHiResultVO data = MatchProtoAdapter.toSuperHiResultVO(matchGrpcClient.superHi(callerUserId, req));
        return Result.ok(data);
    }

    @GetMapping("/quota")
    @Operation(summary = "配额查询(订阅档位 + 当日已用 / 剩余)")
    public Result<MatchQuotaVO> getQuota(HttpServletRequest request) {
        long callerUserId = callerUserResolver.resolveCallerUserId(request);
        MatchQuotaVO data = MatchProtoAdapter.toMatchQuotaVO(matchGrpcClient.getQuota(callerUserId));
        return Result.ok(data);
    }

    @GetMapping("/matches")
    @Operation(summary = "我的匹配列表(分页)")
    public Result<MatchListVO> listMatches(HttpServletRequest request,
                                         @RequestParam(value = "pageSize", defaultValue = "20") int pageSize,
                                         @RequestParam(value = "pageToken", required = false) String pageToken) {
        long callerUserId = callerUserResolver.resolveCallerUserId(request);
        MatchListVO data = MatchProtoAdapter.toMatchListVO(
                matchGrpcClient.listMatches(callerUserId, pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE, pageToken));
        return Result.ok(data);
    }

    @PostMapping("/visit/{targetUserId}")
    @Operation(summary = "记录主页访问(App 端打开他人主页时调用;服务端 UPSERT 累加 visit_count,失败 fail-open)")
    public Result<Boolean> recordVisit(HttpServletRequest request, @PathVariable("targetUserId") Long targetUserId) {
        long callerUserId = callerUserResolver.resolveCallerUserId(request);
        boolean success = matchGrpcClient.recordVisit(callerUserId, targetUserId == null ? 0L : targetUserId)
                .getSuccess();
        return Result.ok(success);
    }

    @GetMapping("/visits")
    @Operation(summary = "查询访问过我主页的用户列表(按最近访问倒序,visitCount 为累计访问次数)")
    public Result<VisitListVO> listVisits(HttpServletRequest request,
                                          @RequestParam(value = "pageSize", defaultValue = "20") int pageSize,
                                          @RequestParam(value = "pageToken", required = false) String pageToken) {
        long callerUserId = callerUserResolver.resolveCallerUserId(request);
        VisitListVO data = MatchProtoAdapter.toVisitListVO(
                matchGrpcClient.listVisits(callerUserId, pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE, pageToken));
        return Result.ok(data);
    }
}
