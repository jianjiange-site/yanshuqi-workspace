package com.dating.gateway.support;

import com.dating.gateway.adapter.MatchProtoAdapter;
import com.dating.gateway.dto.req.SuperHiReq;
import com.dating.gateway.dto.req.SwipeReq;
import com.dating.gateway.exception.GatewayBizException;
import com.dating.gateway.exception.GatewayErrorCode;
import com.dating.match.grpc.proto.SwipeDirection;
import org.springframework.util.StringUtils;

/**
 * Match REST 入参边界与轻量校验；业务规则（配额扣减、匹配判定）仍在 match-service。
 */
public final class MatchParamSupport {

    public static final int DEFAULT_FEED_COUNT = 5;
    public static final int MIN_FEED_COUNT = 1;
    public static final int MAX_FEED_COUNT = 20;

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MIN_PAGE_SIZE = 1;
    public static final int MAX_PAGE_SIZE = 50;

    private MatchParamSupport() {
    }

    /** feed count 限制在 1～20，非法或缺省回退默认值 5。 */
    public static int clampFeedCount(int count) {
        if (count <= 0) {
            return DEFAULT_FEED_COUNT;
        }
        return Math.min(Math.max(count, MIN_FEED_COUNT), MAX_FEED_COUNT);
    }

    /** 分页 pageSize 限制在 1～50，非法或缺省回退默认值 20。 */
    public static int clampPageSize(int pageSize) {
        if (pageSize <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(Math.max(pageSize, MIN_PAGE_SIZE), MAX_PAGE_SIZE);
    }

    /** 校验划卡请求：targetUserId、direction 必填，direction 仅允许 LEFT/RIGHT。 */
    public static void validateSwipeRequest(long callerUserId, SwipeReq req) {
        if (req == null) {
            throw new GatewayBizException(GatewayErrorCode.INVALID_ARGUMENT, "请求体不能为空");
        }
        validateTargetUserId(req.getTargetUserId());
        if (callerUserId == req.getTargetUserId()) {
            throw new GatewayBizException(GatewayErrorCode.INVALID_ARGUMENT, "不能对自己划卡");
        }
        SwipeDirection direction = MatchProtoAdapter.toSwipeDirection(req.getDirection());
        if (direction == SwipeDirection.SWIPE_DIRECTION_UNSPECIFIED) {
            throw new GatewayBizException(GatewayErrorCode.INVALID_ARGUMENT, "direction 仅支持 LEFT / RIGHT");
        }
    }

    /**
     * 校验 SuperHi 请求：clientRequestId 必填且原样透传，gateway 不生成兜底 ID。
     */
    public static void validateSuperHiRequest(long callerUserId, SuperHiReq req) {
        if (req == null) {
            throw new GatewayBizException(GatewayErrorCode.INVALID_ARGUMENT, "请求体不能为空");
        }
        validateTargetUserId(req.getTargetUserId());
        if (callerUserId == req.getTargetUserId()) {
            throw new GatewayBizException(GatewayErrorCode.INVALID_ARGUMENT, "不能对自己 SuperHi");
        }
        if (!StringUtils.hasText(req.getClientRequestId())) {
            throw new GatewayBizException(GatewayErrorCode.INVALID_ARGUMENT, "clientRequestId 不能为空");
        }
    }

    /** visit / home 等场景的目标用户 ID 校验。 */
    public static long validateTargetUserId(Long targetUserId) {
        if (targetUserId == null || targetUserId <= 0) {
            throw new GatewayBizException(GatewayErrorCode.INVALID_ARGUMENT, "targetUserId 非法");
        }
        return targetUserId;
    }
}
