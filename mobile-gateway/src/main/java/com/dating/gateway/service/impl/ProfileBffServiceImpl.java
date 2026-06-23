package com.dating.gateway.service.impl;

import com.dating.gateway.client.UserProfileGrpcClient;
import com.dating.gateway.converter.UserProfileProtoAdapter;
import com.dating.gateway.converter.UserProfileReqBuilder;
import com.dating.gateway.dto.UpdateProfileReq;
import com.dating.gateway.dto.UpsertOnboardingReq;
import com.dating.gateway.dto.vo.UserProfileVO;
import com.dating.gateway.service.ProfileBffService;
import com.dating.user.grpc.proto.UpdateProfileResponse;
import com.dating.user.grpc.proto.UpsertOnboardingResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Profile BFF 实现：DTO → proto → gRPC → VO。
 */
@Service
@Profile("!test")
public class ProfileBffServiceImpl implements ProfileBffService {

    private final UserProfileGrpcClient userProfileGrpcClient;

    public ProfileBffServiceImpl(UserProfileGrpcClient userProfileGrpcClient) {
        this.userProfileGrpcClient = userProfileGrpcClient;
    }

    @Override
    public UserProfileVO upsertOnboarding(long callerUserId, UpsertOnboardingReq req) {
        UpsertOnboardingResponse resp = userProfileGrpcClient.upsertOnboarding(
                UserProfileReqBuilder.buildUpsertOnboarding(callerUserId, req));
        return UserProfileProtoAdapter.toUserProfileVO(resp.getProfile());
    }

    @Override
    public boolean updateProfile(long callerUserId, UpdateProfileReq req) {
        UpdateProfileResponse resp = userProfileGrpcClient.updateProfile(
                UserProfileReqBuilder.buildUpdateProfile(callerUserId, req));
        // Swagger 约定返回 ResultBoolean；user-service 详情对象不在 REST 层暴露
        return resp.getSuccess();
    }
}
