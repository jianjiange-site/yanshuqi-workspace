package com.dating.user.grpc;

import com.dating.user.dto.ResolveOrCreateDeviceUserCommand;
import com.dating.user.dto.ResolveOrCreatePhoneUserCommand;
import com.dating.user.dto.ResolveOrCreateThirdPartyUserCommand;
import com.dating.user.grpc.proto.ResolveOrCreateDeviceUserRequest;
import com.dating.user.grpc.proto.ResolveOrCreateLoginUserResponse;
import com.dating.user.grpc.proto.ResolveOrCreatePhoneUserRequest;
import com.dating.user.grpc.proto.ResolveOrCreateThirdPartyUserRequest;
import com.dating.user.service.UserAuthService;
import com.dating.user.vo.ResolveOrCreateLoginUserResult;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 登录来源 gRPC 映射单元测试。
 */
@ExtendWith(MockitoExtension.class)
class UserAuthLoginSourceGrpcTest {

    @Mock
    private UserAuthService userAuthService;

    @Mock
    private StreamObserver<ResolveOrCreateLoginUserResponse> responseObserver;

    private UserAuthGrpcService grpcService;

    @BeforeEach
    void setUp() {
        grpcService = new UserAuthGrpcService(userAuthService);
    }

    @Test
    void resolveOrCreateDeviceUserShouldMapResponse() {
        when(userAuthService.resolveOrCreateDeviceUser(any(ResolveOrCreateDeviceUserCommand.class))).thenReturn(sampleResult());

        grpcService.resolveOrCreateDeviceUser(
                ResolveOrCreateDeviceUserRequest.newBuilder()
                        .setDeviceId("device-grpc-0012345")
                        .setPlatform("IOS")
                        .build(),
                responseObserver);

        ArgumentCaptor<ResolveOrCreateLoginUserResponse> captor = ArgumentCaptor.forClass(ResolveOrCreateLoginUserResponse.class);
        verify(responseObserver).onNext(captor.capture());
        ResolveOrCreateLoginUserResponse response = captor.getValue();
        assertEquals(5301L, response.getUserId());
        assertTrue(response.getNewlyCreated());
        assertTrue(response.getPending());
        assertEquals("ACTIVE", response.getAccountStatus());
        assertEquals(1, response.getTokenVersion());
    }

    @Test
    void resolveOrCreatePhoneUserShouldDelegateToService() {
        when(userAuthService.resolveOrCreatePhoneUser(any(ResolveOrCreatePhoneUserCommand.class))).thenReturn(sampleResult());

        grpcService.resolveOrCreatePhoneUser(
                ResolveOrCreatePhoneUserRequest.newBuilder()
                        .setPhone("13812345678")
                        .setSmsCode("123456")
                        .setDeviceId("device-grpc-0012345")
                        .setPlatform("ANDROID")
                        .build(),
                responseObserver);

        verify(userAuthService).resolveOrCreatePhoneUser(any(ResolveOrCreatePhoneUserCommand.class));
        verify(responseObserver).onCompleted();
    }

    @Test
    void resolveOrCreateThirdPartyUserShouldDelegateToService() {
        when(userAuthService.resolveOrCreateThirdPartyUser(any(ResolveOrCreateThirdPartyUserCommand.class))).thenReturn(sampleResult());

        grpcService.resolveOrCreateThirdPartyUser(
                ResolveOrCreateThirdPartyUserRequest.newBuilder()
                        .setThirdPartyPlatform(1)
                        .setIdToken("token-value")
                        .setDeviceId("device-grpc-0012345")
                        .setPlatform("IOS")
                        .build(),
                responseObserver);

        verify(userAuthService).resolveOrCreateThirdPartyUser(any(ResolveOrCreateThirdPartyUserCommand.class));
    }

    private ResolveOrCreateLoginUserResult sampleResult() {
        ResolveOrCreateLoginUserResult result = new ResolveOrCreateLoginUserResult();
        result.setUserId(5301L);
        result.setNewlyCreated(true);
        result.setPending(true);
        result.setAccountStatus("ACTIVE");
        result.setProfileStatus("INIT");
        result.setTokenVersion(1);
        result.setLastLoginAt(OffsetDateTime.now(ZoneOffset.UTC));
        return result;
    }
}
