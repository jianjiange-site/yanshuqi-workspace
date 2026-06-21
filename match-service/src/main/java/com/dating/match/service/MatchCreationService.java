package com.dating.match.service;

import com.dating.match.client.TargetUserTypeResolver;
import com.dating.match.constant.MatchOutboxAction;
import com.dating.match.constant.MatchSourceConstant;
import com.dating.match.constant.UserTypeConstant;
import com.dating.match.dto.MatchInsertResult;
import com.dating.match.exception.MatchBizException;
import com.dating.match.exception.MatchErrorCode;
import com.dating.match.manager.MatchManager;
import com.dating.match.manager.MatchOutboxManager;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * 统一创建 match 并写 outbox；对外返回 match.biz_id。
 */
@Service
@Profile("!test")
public class MatchCreationService {

    private final MatchManager matchManager;
    private final MatchOutboxManager matchOutboxManager;
    private final TargetUserTypeResolver targetUserTypeResolver;

    public MatchCreationService(MatchManager matchManager,
                                MatchOutboxManager matchOutboxManager,
                                TargetUserTypeResolver targetUserTypeResolver) {
        this.matchManager = matchManager;
        this.matchOutboxManager = matchOutboxManager;
        this.targetUserTypeResolver = targetUserTypeResolver;
    }

    /**
     * 幂等创建 match，新创建时写 outbox；重复 pair 返回已有 bizId 且不重复写 outbox。
     */
    public long createMatch(long userIdA, long userIdB, String source) {
        validate(userIdA, userIdB, source);
        MatchInsertResult result = matchManager.insertIfAbsentWithResult(userIdA, userIdB, source);
        if (result.isNewlyCreated()) {
            writeOutbox(result.getEntity().getBizId(), userIdA, userIdB, source);
        }
        return result.getEntity().getBizId();
    }

    private void writeOutbox(long matchBizId, long userIdA, long userIdB, String source) {
        String payload = buildPayload(matchBizId, userIdA, userIdB, source);
        Instant now = Instant.now();
        matchOutboxManager.createPending(matchBizId, MatchOutboxAction.ENSURE_CONVERSATION, payload, now);
        matchOutboxManager.createPending(matchBizId, MatchOutboxAction.SEND_SYSTEM_MESSAGE_TO_USER_A, payload, now);
        matchOutboxManager.createPending(matchBizId, MatchOutboxAction.SEND_SYSTEM_MESSAGE_TO_USER_B, payload, now);
        if (isDhInvolved(userIdA, userIdB)) {
            matchOutboxManager.createPending(matchBizId, MatchOutboxAction.TRIGGER_DH_OPENING, payload, now);
        }
    }

    private boolean isDhInvolved(long userIdA, long userIdB) {
        return targetUserTypeResolver.resolveTargetUserType(userIdA) == UserTypeConstant.DH
                || targetUserTypeResolver.resolveTargetUserType(userIdB) == UserTypeConstant.DH;
    }

    private static String buildPayload(long matchBizId, long userIdA, long userIdB, String source) {
        return "{\"matchBizId\":" + matchBizId
                + ",\"userIdA\":" + userIdA
                + ",\"userIdB\":" + userIdB
                + ",\"source\":\"" + source + "\"}";
    }

    private static void validate(long userIdA, long userIdB, String source) {
        if (userIdA <= 0 || userIdB <= 0) {
            throw new MatchBizException(MatchErrorCode.INVALID_ARGUMENT);
        }
        if (userIdA == userIdB) {
            throw new MatchBizException(MatchErrorCode.INVALID_ARGUMENT);
        }
        if (!MatchSourceConstant.SWIPE_MATCH.equals(source)
                && !MatchSourceConstant.SWIPE_SUPER_HI.equals(source)) {
            throw new MatchBizException(MatchErrorCode.INVALID_ARGUMENT);
        }
    }
}
