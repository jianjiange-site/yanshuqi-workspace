package com.dating.user.service.support;

import com.dating.user.constant.AccountStatus;
import com.dating.user.constant.ProfileStatus;
import com.dating.user.entity.UserEntity;
import com.dating.user.exception.UserErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 用户可用性判断器。
 */
@Component
public class UserAvailabilityEvaluator {

    /**
     * 可用性判断结果。
     */
    public static final class AvailabilityResult {

        private final boolean available;

        private final String reason;

        private AvailabilityResult(boolean available, String reason) {
            this.available = available;
            this.reason = reason;
        }

        /**
         * 构造可用结果。
         *
         * @return 可用结果
         */
        public static AvailabilityResult available() {
            return new AvailabilityResult(true, null);
        }

        /**
         * 构造不可用结果。
         *
         * @param reason 不可用原因码
         * @return 不可用结果
         */
        public static AvailabilityResult unavailable(String reason) {
            return new AvailabilityResult(false, reason);
        }

        /**
         * 是否可用。
         *
         * @return true 表示可用
         */
        public boolean isAvailable() {
            return available;
        }

        /**
         * 获取不可用原因码。
         *
         * @return 不可用原因
         */
        public String getReason() {
            return reason;
        }
    }

    /**
     * 根据用户主表记录判断可用性。
     *
     * @param userEntity 用户实体，不存在时传 null
     * @return 可用性判断结果
     */
    public AvailabilityResult evaluate(UserEntity userEntity) {
        if (userEntity == null) {
            return AvailabilityResult.unavailable(UserErrorCode.USER_NOT_FOUND.getCode());
        }
        if (userEntity.getDeleted() != null && userEntity.getDeleted() == 1) {
            return AvailabilityResult.unavailable(UserErrorCode.USER_DELETED.getCode());
        }
        String accountStatus = userEntity.getAccountStatus();
        if (!StringUtils.hasText(accountStatus)) {
            return AvailabilityResult.unavailable(UserErrorCode.USER_DISABLED.getCode());
        }
        AccountStatus status;
        try {
            status = AccountStatus.valueOf(accountStatus.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return AvailabilityResult.unavailable(UserErrorCode.USER_DISABLED.getCode());
        }
        switch (status) {
            case ACTIVE -> {
                if (ProfileStatus.BLOCKED.name().equalsIgnoreCase(userEntity.getProfileStatus())) {
                    return AvailabilityResult.unavailable("PROFILE_BLOCKED");
                }
                return AvailabilityResult.available();
            }
            case DISABLED -> {
                return AvailabilityResult.unavailable(UserErrorCode.USER_DISABLED.getCode());
            }
            case BANNED -> {
                return AvailabilityResult.unavailable(UserErrorCode.USER_BANNED.getCode());
            }
            case DELETED -> {
                return AvailabilityResult.unavailable(UserErrorCode.USER_DELETED.getCode());
            }
            default -> {
                return AvailabilityResult.unavailable(UserErrorCode.USER_DISABLED.getCode());
            }
        }
    }
}
