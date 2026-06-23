package com.dating.gateway.client;

import com.dating.gateway.exception.GatewayGrpcExceptionMapper;
import com.dating.user.grpc.proto.ConfirmAvatarUploadRequest;
import com.dating.user.grpc.proto.ConfirmAvatarUploadResponse;
import com.dating.user.grpc.proto.GetHomeCardProfileRequest;
import com.dating.user.grpc.proto.GetHomeCardProfileResponse;
import com.dating.user.grpc.proto.PresignAvatarUploadRequest;
import com.dating.user.grpc.proto.PresignAvatarUploadResponse;
import com.dating.user.grpc.proto.UpdateProfileRequest;
import com.dating.user.grpc.proto.UpdateProfileResponse;
import com.dating.user.grpc.proto.UpsertOnboardingRequest;
import com.dating.user.grpc.proto.UpsertOnboardingResponse;
import com.dating.user.grpc.proto.UserAvatarServiceGrpc;
import com.dating.user.grpc.proto.UserProfileServiceGrpc;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * user-service 资料 / 头像 gRPC 客户端；Controller 禁止直接注入 stub。
 */
@Component
@Profile("!test")
public class UserProfileGrpcClient {

    @GrpcClient("user-service")
    private UserProfileServiceGrpc.UserProfileServiceBlockingStub userProfileServiceStub;

    @GrpcClient("user-service")
    private UserAvatarServiceGrpc.UserAvatarServiceBlockingStub userAvatarServiceStub;

    public UpsertOnboardingResponse upsertOnboarding(UpsertOnboardingRequest request) {
        return invokeProfile(() -> userProfileServiceStub.upsertOnboarding(request));
    }

    public UpdateProfileResponse updateProfile(UpdateProfileRequest request) {
        return invokeProfile(() -> userProfileServiceStub.updateProfile(request));
    }

    public GetHomeCardProfileResponse getHomeCardProfile(GetHomeCardProfileRequest request) {
        return invokeProfile(() -> userProfileServiceStub.getHomeCardProfile(request));
    }

    public PresignAvatarUploadResponse presignAvatarUpload(PresignAvatarUploadRequest request) {
        return invokeAvatar(() -> userAvatarServiceStub.presignAvatarUpload(request));
    }

    public ConfirmAvatarUploadResponse confirmAvatarUpload(ConfirmAvatarUploadRequest request) {
        return invokeAvatar(() -> userAvatarServiceStub.confirmAvatarUpload(request));
    }

    private <T> T invokeProfile(GrpcCall<T> call) {
        try {
            return call.execute();
        } catch (StatusRuntimeException ex) {
            throw GatewayGrpcExceptionMapper.toGatewayException(ex);
        }
    }

    private <T> T invokeAvatar(GrpcCall<T> call) {
        try {
            return call.execute();
        } catch (StatusRuntimeException ex) {
            throw GatewayGrpcExceptionMapper.toGatewayException(ex);
        }
    }

    @FunctionalInterface
    private interface GrpcCall<T> {
        T execute();
    }
}
