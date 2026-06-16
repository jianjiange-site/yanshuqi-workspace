package com.dating.user.grpc;

import com.dating.user.dto.DeviceInfoCommand;
import com.dating.user.dto.LoginCommand;
import com.dating.user.dto.RegisterCommand;
import com.dating.user.grpc.proto.DeviceInfo;
import com.dating.user.grpc.proto.RegisterRequest;
import com.dating.user.grpc.proto.RegisterResponse;
import com.dating.user.grpc.proto.UserAuthServiceGrpc;
import com.dating.user.grpc.proto.VerifyLoginRequest;
import com.dating.user.grpc.proto.VerifyLoginResponse;
import com.dating.user.service.UserAuthService;
import com.dating.user.vo.LoginResult;
import com.dating.user.vo.RegisterResult;
import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.context.annotation.Profile;

import java.time.Instant;

/**
 * 用户认证 gRPC 服务，仅负责参数转换与结果映射。
 */
@GrpcService
@Profile("!test")
public class UserAuthGrpcService extends UserAuthServiceGrpc.UserAuthServiceImplBase {

    private final UserAuthService userAuthService;

    /**
     * 构造用户认证 gRPC 服务。
     *
     * @param userAuthService 用户认证业务服务
     */
    public UserAuthGrpcService(UserAuthService userAuthService) {
        this.userAuthService = userAuthService;
    }

    /**
     * 处理用户注册 gRPC 请求，仅做入参/出参转换。
     *
     * @param request          注册请求
     * @param responseObserver 响应观察者
     */
    @Override
    public void register(RegisterRequest request, StreamObserver<RegisterResponse> responseObserver) {
        // 1. 将 gRPC Request 转换为 Service 层 RegisterCommand
        RegisterCommand command = toRegisterCommand(request);
        // 2. 调用业务服务完成注册
        RegisterResult result = userAuthService.register(command);
        // 3. 将 RegisterResult 转换为 gRPC Response
        RegisterResponse response = toRegisterResponse(result);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * 处理用户登录校验 gRPC 请求，仅做入参/出参转换。
     *
     * @param request          登录校验请求
     * @param responseObserver 响应观察者
     */
    @Override
    public void verifyLogin(VerifyLoginRequest request, StreamObserver<VerifyLoginResponse> responseObserver) {
        // 1. 将 gRPC Request 转换为 Service 层 LoginCommand
        LoginCommand command = toLoginCommand(request);
        // 2. 调用业务服务完成登录校验
        LoginResult result = userAuthService.verifyLogin(command);
        // 3. 将 LoginResult 转换为 gRPC Response
        VerifyLoginResponse response = toVerifyLoginResponse(result);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private RegisterCommand toRegisterCommand(RegisterRequest request) {
        RegisterCommand command = new RegisterCommand();
        command.setIdentityType(request.getIdentityType());
        command.setIdentityValue(request.getIdentityValue());
        command.setPassword(request.getPassword());
        command.setUserType(request.getUserType());
        command.setRegisterSource(request.getRegisterSource());
        return command;
    }

    private LoginCommand toLoginCommand(VerifyLoginRequest request) {
        LoginCommand command = new LoginCommand();
        command.setIdentityType(request.getIdentityType());
        command.setIdentityValue(request.getIdentityValue());
        command.setPassword(request.getPassword());
        if (request.hasDeviceInfo()) {
            command.setDeviceInfo(toDeviceInfoCommand(request.getDeviceInfo()));
        }
        return command;
    }

    private DeviceInfoCommand toDeviceInfoCommand(DeviceInfo deviceInfo) {
        DeviceInfoCommand command = new DeviceInfoCommand();
        command.setPlatform(deviceInfo.getPlatform());
        command.setDeviceFingerprint(deviceInfo.getDeviceFingerprint());
        command.setPushToken(deviceInfo.getPushToken());
        command.setAppVersion(deviceInfo.getAppVersion());
        return command;
    }

    private RegisterResponse toRegisterResponse(RegisterResult result) {
        return RegisterResponse.newBuilder()
                .setUserId(result.getUserId())
                .setAccountStatus(result.getAccountStatus())
                .setProfileStatus(result.getProfileStatus())
                .setTokenVersion(result.getTokenVersion())
                .build();
    }

    private VerifyLoginResponse toVerifyLoginResponse(LoginResult result) {
        VerifyLoginResponse.Builder builder = VerifyLoginResponse.newBuilder()
                .setUserId(result.getUserId())
                .setAccountStatus(result.getAccountStatus())
                .setProfileStatus(result.getProfileStatus())
                .setTokenVersion(result.getTokenVersion());
        if (result.getLastLoginAt() != null) {
            Instant instant = result.getLastLoginAt().toInstant();
            builder.setLastLoginAt(Timestamp.newBuilder()
                    .setSeconds(instant.getEpochSecond())
                    .setNanos(instant.getNano())
                    .build());
        }
        return builder.build();
    }
}
