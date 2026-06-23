package com.dating.gateway.service;

import com.dating.gateway.dto.UpdateProfileReq;
import com.dating.gateway.dto.UpsertOnboardingReq;
import com.dating.gateway.dto.vo.UserProfileVO;

/**
 * Profile BFF：onboarding 与日常资料更新。
 */
public interface ProfileBffService {

    UserProfileVO upsertOnboarding(long callerUserId, UpsertOnboardingReq req);

    boolean updateProfile(long callerUserId, UpdateProfileReq req);
}
