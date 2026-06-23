package com.dating.gateway.client;

import com.dating.gateway.exception.GatewayGrpcExceptionMapper;
import com.dating.user.grpc.proto.ResolveOrCreateDeviceUserRequest;
import com.dating.user.grpc.proto.ResolveOrCreateLoginUserResponse;
import com.dating.user.grpc.proto.ResolveOrCreatePhoneUserRequest;
import com.dating.user.grpc.proto.ResolveOrCreateThirdPartyUserRequest;
import com.dating.user.grpc.proto.UserAuthServiceGrpc;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * user-service 认证 gRPC 客户端；Controller 禁止直接注入 stub。
 */
@Component
@Profile("!test")
public class UserAuthGrpcClient {

    @GrpcClient("user-service")
    private UserAuthServiceGrpc.UserAuthServiceBlockingStub userAuthServiceStub;

    public ResolveOrCreateLoginUserResponse resolveOrCreateDeviceUser(ResolveOrCreateDeviceUserRequest request) {
        return invoke(() -> userAuthServiceStub.resolveOrCreateDeviceUser(request));
    }

    public ResolveOrCreateLoginUserResponse resolveOrCreatePhoneUser(ResolveOrCreatePhoneUserRequest request) {
        return invoke(() -> userAuthServiceStub.resolveOrCreatePhoneUser(request));
    }

    public ResolveOrCreateLoginUserResponse resolveOrCreateThirdPartyUser(ResolveOrCreateThirdPartyUserRequest request) {
        return invoke(() -> userAuthServiceStub.resolveOrCreateThirdPartyUser(request));
    }

    private ResolveOrCreateLoginUserResponse invoke(GrpcCall call) {
        try {
            return call.execute();
        } catch (StatusRuntimeException ex) {
            throw GatewayGrpcExceptionMapper.toGatewayException(ex);
        }
    }

    @FunctionalInterface
    private interface GrpcCall {
        ResolveOrCreateLoginUserResponse execute();
    }
}
