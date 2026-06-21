package com.dating.user.exception;

import io.grpc.Status;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * gRPC 状态映射单元测试。
 */
class UserGrpcStatusMapperTest {

    /**
     * 批量超限应映射 INVALID_ARGUMENT。
     */
    @Test
    void batchSizeExceededShouldMapToInvalidArgument() {
        assertEquals(Status.Code.INVALID_ARGUMENT, UserGrpcStatusMapper.toStatus(UserErrorCode.USER_BATCH_SIZE_EXCEEDED).getCode());
    }

    /**
     * 密码错误应映射 UNAUTHENTICATED。
     */
    @Test
    void passwordInvalidShouldMapToUnauthenticated() {
        assertEquals(Status.Code.UNAUTHENTICATED, UserGrpcStatusMapper.toStatus(UserErrorCode.PASSWORD_INVALID).getCode());
    }

    /**
     * 封禁用户应映射 PERMISSION_DENIED。
     */
    @Test
    void userBannedShouldMapToPermissionDenied() {
        assertEquals(Status.Code.PERMISSION_DENIED, UserGrpcStatusMapper.toStatus(UserErrorCode.USER_BANNED).getCode());
    }

    /**
     * 用户不存在应映射 NOT_FOUND。
     */
    @Test
    void userNotFoundShouldMapToNotFound() {
        assertEquals(Status.Code.NOT_FOUND, UserGrpcStatusMapper.toStatus(UserErrorCode.USER_NOT_FOUND).getCode());
    }

    /**
     * object key 非法应映射 INVALID_ARGUMENT。
     */
    @Test
    void photoObjectKeyInvalidShouldMapToInvalidArgument() {
        assertEquals(Status.Code.INVALID_ARGUMENT, UserGrpcStatusMapper.toStatus(UserErrorCode.PHOTO_OBJECT_KEY_INVALID).getCode());
    }

    /**
     * 并发冲突应映射 ABORTED。
     */
    @Test
    void concurrentConflictShouldMapToAborted() {
        assertEquals(Status.Code.ABORTED, UserGrpcStatusMapper.toStatus(UserErrorCode.USER_CONCURRENT_CONFLICT).getCode());
    }

    /**
     * 非法设备 ID 应映射 INVALID_ARGUMENT。
     */
    @Test
    void invalidDeviceIdShouldMapToInvalidArgument() {
        assertEquals(Status.Code.INVALID_ARGUMENT, UserGrpcStatusMapper.toStatus(UserErrorCode.INVALID_DEVICE_ID).getCode());
    }

    /**
     * 三方身份非法应映射 UNAUTHENTICATED。
     */
    @Test
    void invalidThirdPartyIdentityShouldMapToUnauthenticated() {
        assertEquals(Status.Code.UNAUTHENTICATED, UserGrpcStatusMapper.toStatus(UserErrorCode.INVALID_THIRD_PARTY_IDENTITY).getCode());
    }

    /**
     * 未知异常映射 INTERNAL 且不泄露堆栈描述。
     */
    @Test
    void internalExceptionShouldNotExposeStackTrace() {
        assertEquals(Status.Code.INTERNAL, UserGrpcStatusMapper.toInternalException().getStatus().getCode());
        assertEquals(UserErrorCode.INTERNAL_ERROR.getCode(), UserGrpcStatusMapper.toInternalException().getStatus().getDescription());
    }
}
