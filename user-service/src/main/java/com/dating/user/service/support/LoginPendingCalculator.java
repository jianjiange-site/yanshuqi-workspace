package com.dating.user.service.support;

import com.dating.user.constant.ProfileStatus;
import org.springframework.stereotype.Component;

/**
 * 登录响应 pending 字段计算器。
 */
@Component
public class LoginPendingCalculator {

    /**
     * 根据资料状态与 profile_completed 计算 pending。
     * pending=true 表示用户仍需完成 onboarding / 资料补全。
     *
     * @param profileStatus    用户资料状态
     * @param profileCompleted 资料是否完整：0=未完成，1=已完成
     * @return 是否 pending
     */
    public boolean computePending(String profileStatus, Integer profileCompleted) {
        if (ProfileStatus.COMPLETED.name().equals(profileStatus)) {
            return false;
        }
        // onboarding 完成后 profile_completed=1 即视为非 pending
        if (profileCompleted != null && profileCompleted == 1) {
            return false;
        }
        return ProfileStatus.INIT.name().equals(profileStatus)
                || profileCompleted == null
                || profileCompleted == 0;
    }
}
