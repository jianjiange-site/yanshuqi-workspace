package com.dating.match.service;

import com.dating.match.client.TargetUserTypeResolver;
import com.dating.match.constant.MatchSourceConstant;
import com.dating.match.constant.SwipeDirectionConstant;
import com.dating.match.constant.UserTypeConstant;
import com.dating.match.dto.SuperHiResult;
import com.dating.match.dto.SwipeResult;
import com.dating.match.exception.MatchBizException;
import com.dating.match.exception.MatchErrorCode;
import com.dating.match.grpc.proto.SwipeDirection;
import com.dating.match.manager.SwipeHistoryManager;
import com.dating.match.repository.SwipedSetRepository;
import com.dating.match.service.support.SwipeLockExecutor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 划卡业务：配额、幂等、历史、匹配触发；对外 matchId 使用 match.biz_id。
 */
@Service
@Profile("!test")
public class SwipeService {

    private static final long NO_MATCH_ID = 0L;

    private final SwipeLockExecutor swipeLockExecutor;
    private final SwipeHistoryManager swipeHistoryManager;
    private final QuotaService quotaService;
    private final SwipedSetRepository swipedSetRepository;
    private final SuperHiIdempotencyStore superHiIdempotencyStore;
    private final TargetUserTypeResolver targetUserTypeResolver;
    private final MatchCreationService matchCreationService;
    private final DhDelayedMatchService dhDelayedMatchService;

    public SwipeService(SwipeLockExecutor swipeLockExecutor,
                        SwipeHistoryManager swipeHistoryManager,
                        QuotaService quotaService,
                        SwipedSetRepository swipedSetRepository,
                        SuperHiIdempotencyStore superHiIdempotencyStore,
                        TargetUserTypeResolver targetUserTypeResolver,
                        MatchCreationService matchCreationService,
                        DhDelayedMatchService dhDelayedMatchService) {
        this.swipeLockExecutor = swipeLockExecutor;
        this.swipeHistoryManager = swipeHistoryManager;
        this.quotaService = quotaService;
        this.swipedSetRepository = swipedSetRepository;
        this.superHiIdempotencyStore = superHiIdempotencyStore;
        this.targetUserTypeResolver = targetUserTypeResolver;
        this.matchCreationService = matchCreationService;
        this.dhDelayedMatchService = dhDelayedMatchService;
    }

    public SwipeResult swipe(long callerUserId, long targetUserId, SwipeDirection direction) {
        validateSwipeParams(callerUserId, targetUserId, direction);
        return swipeLockExecutor.executeWithLock(callerUserId, targetUserId, () -> {
            if (swipeHistoryManager.findByUserIdAndTargetUserId(callerUserId, targetUserId).isPresent()) {
                return new SwipeResult(NO_MATCH_ID);
            }
            if (direction == SwipeDirection.LEFT) {
                quotaService.consumeLeftSwipe(callerUserId);
                persistSwipe(callerUserId, targetUserId, SwipeDirectionConstant.LEFT);
                return new SwipeResult(NO_MATCH_ID);
            }
            quotaService.consumeRightSwipe(callerUserId);
            persistSwipe(callerUserId, targetUserId, SwipeDirectionConstant.RIGHT);
            return new SwipeResult(resolveRightMatchId(callerUserId, targetUserId));
        });
    }

    public SuperHiResult superHi(long callerUserId, long targetUserId, String clientRequestId) {
        validateSuperHiParams(callerUserId, targetUserId, clientRequestId);
        return superHiIdempotencyStore.find(callerUserId, clientRequestId)
                .orElseGet(() -> swipeLockExecutor.executeWithLock(callerUserId, targetUserId, () -> {
                    SuperHiResult cached = superHiIdempotencyStore.find(callerUserId, clientRequestId).orElse(null);
                    if (cached != null) {
                        return cached;
                    }
                    if (swipeHistoryManager.findByUserIdAndTargetUserId(callerUserId, targetUserId).isPresent()) {
                        SuperHiResult result = new SuperHiResult(NO_MATCH_ID, 0);
                        superHiIdempotencyStore.save(callerUserId, clientRequestId, result);
                        return result;
                    }
                    int coinsUsed = quotaService.consumeSuperHi(callerUserId, clientRequestId);
                    persistSwipe(callerUserId, targetUserId, SwipeDirectionConstant.SUPER_HI);
                    long matchId = matchCreationService.createMatch(
                            callerUserId, targetUserId, MatchSourceConstant.SWIPE_SUPER_HI);
                    SuperHiResult result = new SuperHiResult(matchId, coinsUsed);
                    superHiIdempotencyStore.save(callerUserId, clientRequestId, result);
                    return result;
                }));
    }

    private long resolveRightMatchId(long callerUserId, long targetUserId) {
        int targetType = targetUserTypeResolver.resolveTargetUserType(targetUserId);
        if (targetType == UserTypeConstant.BH) {
            if (swipeHistoryManager.hasPositiveSwipe(targetUserId, callerUserId)) {
                return matchCreationService.createMatch(
                        callerUserId, targetUserId, MatchSourceConstant.SWIPE_MATCH);
            }
            return NO_MATCH_ID;
        }
        if (targetType == UserTypeConstant.DH) {
            dhDelayedMatchService.scheduleDelayedMatch(callerUserId, targetUserId);
            return NO_MATCH_ID;
        }
        return NO_MATCH_ID;
    }

    private void persistSwipe(long callerUserId, long targetUserId, int direction) {
        int targetType = targetUserTypeResolver.resolveTargetUserType(targetUserId);
        swipeHistoryManager.insertIfAbsent(callerUserId, targetUserId, targetType, direction, null);
        swipedSetRepository.addSwiped(callerUserId, targetUserId);
    }

    private void validateSwipeParams(long callerUserId, long targetUserId, SwipeDirection direction) {
        if (callerUserId <= 0 || targetUserId <= 0) {
            throw new MatchBizException(MatchErrorCode.INVALID_ARGUMENT);
        }
        if (callerUserId == targetUserId) {
            throw new MatchBizException(MatchErrorCode.INVALID_ARGUMENT);
        }
        if (direction != SwipeDirection.LEFT && direction != SwipeDirection.RIGHT) {
            throw new MatchBizException(MatchErrorCode.INVALID_ARGUMENT);
        }
    }

    private void validateSuperHiParams(long callerUserId, long targetUserId, String clientRequestId) {
        if (callerUserId <= 0 || targetUserId <= 0) {
            throw new MatchBizException(MatchErrorCode.INVALID_ARGUMENT);
        }
        if (callerUserId == targetUserId) {
            throw new MatchBizException(MatchErrorCode.INVALID_ARGUMENT);
        }
        if (!StringUtils.hasText(clientRequestId)) {
            throw new MatchBizException(MatchErrorCode.INVALID_ARGUMENT);
        }
    }
}
