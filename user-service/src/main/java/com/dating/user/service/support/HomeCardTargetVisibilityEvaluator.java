package com.dating.user.service.support;

import com.dating.user.entity.UserEntity;
import com.dating.user.entity.UserProfileEntity;
import com.dating.user.exception.UserBizException;
import com.dating.user.exception.UserErrorCode;
import org.springframework.stereotype.Component;

/**
 * HomeCard target 用户可见性校验。
 */
@Component
public class HomeCardTargetVisibilityEvaluator {

    /** regulation_status < 0 视为禁止对外展示。 */
    private static final int REGULATION_DISPLAY_BLOCKED_THRESHOLD = 0;

    private final UserAvailabilityEvaluator userAvailabilityEvaluator;

    public HomeCardTargetVisibilityEvaluator(UserAvailabilityEvaluator userAvailabilityEvaluator) {
        this.userAvailabilityEvaluator = userAvailabilityEvaluator;
    }

    /**
     * 校验 target 用户与资料是否允许在 HomeCard 中展示。
     * 本阶段不做 match / visit / block 关系判断。
     */
    public void validateTargetVisible(UserEntity targetUser, UserProfileEntity targetProfile) {
        if (targetUser == null) {
            throw new UserBizException(UserErrorCode.TARGET_USER_NOT_FOUND);
        }
        UserAvailabilityEvaluator.AvailabilityResult availability =
                userAvailabilityEvaluator.evaluate(targetUser);
        if (!availability.isAvailable()) {
            String reason = availability.getReason();
            if (UserErrorCode.USER_NOT_FOUND.getCode().equals(reason)) {
                throw new UserBizException(UserErrorCode.TARGET_USER_NOT_FOUND);
            }
            // target 不可用时不泄露封禁细节，统一返回 TARGET_USER_UNAVAILABLE
            throw new UserBizException(UserErrorCode.TARGET_USER_UNAVAILABLE);
        }
        if (targetProfile == null) {
            throw new UserBizException(UserErrorCode.PROFILE_NOT_FOUND);
        }
        Integer regulationStatus = targetProfile.getRegulationStatus();
        if (regulationStatus != null && regulationStatus < REGULATION_DISPLAY_BLOCKED_THRESHOLD) {
            throw new UserBizException(UserErrorCode.TARGET_USER_UNAVAILABLE);
        }
    }
}
