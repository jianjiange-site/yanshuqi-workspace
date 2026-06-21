package com.dating.user.service.support;

import com.dating.user.constant.AccountStatus;
import com.dating.user.entity.UserEntity;
import com.dating.user.exception.UserBizException;
import com.dating.user.exception.UserErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * HomeCard self 用户校验：查看者必须存在且 ACTIVE。
 */
@Component
public class HomeCardSelfValidator {

    /**
     * 校验 self 用户存在且账号 ACTIVE，BANNED/DISABLED 不可发起查询。
     */
    public void validateSelfViewer(UserEntity selfUser) {
        if (selfUser == null) {
            throw new UserBizException(UserErrorCode.USER_NOT_FOUND);
        }
        if (selfUser.getDeleted() != null && selfUser.getDeleted() == 1) {
            throw new UserBizException(UserErrorCode.USER_DELETED);
        }
        AccountStatus status = parseAccountStatus(selfUser.getAccountStatus());
        switch (status) {
            case ACTIVE -> {
                return;
            }
            case DISABLED -> throw new UserBizException(UserErrorCode.USER_DISABLED);
            case BANNED -> throw new UserBizException(UserErrorCode.USER_BANNED);
            case DELETED -> throw new UserBizException(UserErrorCode.USER_DELETED);
            default -> throw new UserBizException(UserErrorCode.USER_DISABLED);
        }
    }

    private AccountStatus parseAccountStatus(String accountStatus) {
        if (!StringUtils.hasText(accountStatus)) {
            throw new UserBizException(UserErrorCode.USER_DISABLED);
        }
        try {
            return AccountStatus.valueOf(accountStatus.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new UserBizException(UserErrorCode.USER_DISABLED);
        }
    }
}
