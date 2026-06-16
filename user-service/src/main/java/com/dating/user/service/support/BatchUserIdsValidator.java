package com.dating.user.service.support;

import com.dating.user.constant.AccountStatus;
import com.dating.user.constant.ProfileStatus;
import com.dating.user.entity.UserEntity;
import com.dating.user.exception.UserBizException;
import com.dating.user.exception.UserErrorCode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 批量用户 ID 校验器。
 */
@Component
public class BatchUserIdsValidator {

    private static final int MIN_BATCH_SIZE = 1;

    private static final int MAX_BATCH_SIZE = 100;

    /**
     * 校验并去重用户 ID 列表，保持首次出现顺序。
     *
     * @param userIds 原始用户 ID 列表
     * @return 去重后的用户 ID 列表
     * @throws UserBizException 当列表为空或超过批量上限时
     */
    public List<Long> validateAndDedupe(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "用户 ID 列表不能为空");
        }
        Set<Long> deduped = new LinkedHashSet<>();
        for (Long userId : userIds) {
            if (userId == null || userId <= 0) {
                throw new UserBizException(UserErrorCode.USER_PROFILE_QUERY_INVALID, "用户 ID 非法");
            }
            deduped.add(userId);
        }
        if (deduped.size() < MIN_BATCH_SIZE) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "用户 ID 列表不能为空");
        }
        if (deduped.size() > MAX_BATCH_SIZE) {
            throw new UserBizException(UserErrorCode.USER_BATCH_SIZE_EXCEEDED);
        }
        return new ArrayList<>(deduped);
    }
}
