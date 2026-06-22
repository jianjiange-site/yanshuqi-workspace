package com.dating.match.service;

import com.dating.match.exception.MatchBizException;
import com.dating.match.exception.MatchErrorCode;
import com.dating.match.manager.ProfileVisitManager;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * 主页访问记录：UPSERT 累加 visit_count。
 * <p>
 * 数据库异常不在本服务吞掉，便于测试与排查；gateway fail-open 由后续阶段处理。
 */
@Service
@Profile("!test")
public class ProfileVisitService {

    private final ProfileVisitManager profileVisitManager;

    public ProfileVisitService(ProfileVisitManager profileVisitManager) {
        this.profileVisitManager = profileVisitManager;
    }

    public void recordVisit(long callerUserId, long targetUserId) {
        if (callerUserId <= 0 || targetUserId <= 0) {
            throw new MatchBizException(MatchErrorCode.INVALID_ARGUMENT);
        }
        if (callerUserId == targetUserId) {
            throw new MatchBizException(MatchErrorCode.INVALID_ARGUMENT);
        }
        profileVisitManager.upsertVisit(callerUserId, targetUserId);
    }
}
