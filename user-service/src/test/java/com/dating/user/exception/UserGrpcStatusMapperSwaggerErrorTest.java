package com.dating.user.exception;

import io.grpc.Status;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UserGrpcStatusMapperSwaggerErrorTest {

    @Test
    void allUserErrorCodesShouldMapToGrpcStatus() {
        for (UserErrorCode code : UserErrorCode.values()) {
            Status status = UserGrpcStatusMapper.toStatus(code);
            assertNotNull(status);
            assertNotNull(status.getCode());
        }
    }

    @Test
    void swaggerAuthErrorCodesShouldMapAsExpected() {
        assertEquals(Status.Code.INVALID_ARGUMENT, UserGrpcStatusMapper.toStatus(UserErrorCode.INVALID_DEVICE_ID).getCode());
        assertEquals(Status.Code.INVALID_ARGUMENT, UserGrpcStatusMapper.toStatus(UserErrorCode.INVALID_PLATFORM).getCode());
        assertEquals(Status.Code.INVALID_ARGUMENT, UserGrpcStatusMapper.toStatus(UserErrorCode.INVALID_PHONE).getCode());
        assertEquals(Status.Code.INVALID_ARGUMENT, UserGrpcStatusMapper.toStatus(UserErrorCode.INVALID_SMS_CODE).getCode());
        assertEquals(Status.Code.INVALID_ARGUMENT, UserGrpcStatusMapper.toStatus(UserErrorCode.INVALID_THIRD_PARTY_PLATFORM).getCode());
        assertEquals(Status.Code.UNAUTHENTICATED, UserGrpcStatusMapper.toStatus(UserErrorCode.INVALID_THIRD_PARTY_IDENTITY).getCode());
        assertEquals(Status.Code.ABORTED, UserGrpcStatusMapper.toStatus(UserErrorCode.USER_CONCURRENT_CONFLICT).getCode());
    }

    @Test
    void swaggerProfileErrorCodesShouldMapAsExpected() {
        assertEquals(Status.Code.INVALID_ARGUMENT, UserGrpcStatusMapper.toStatus(UserErrorCode.INVALID_NICKNAME).getCode());
        assertEquals(Status.Code.INVALID_ARGUMENT, UserGrpcStatusMapper.toStatus(UserErrorCode.INVALID_GENDER).getCode());
        assertEquals(Status.Code.INVALID_ARGUMENT, UserGrpcStatusMapper.toStatus(UserErrorCode.INVALID_BIRTHDAY).getCode());
        assertEquals(Status.Code.INVALID_ARGUMENT, UserGrpcStatusMapper.toStatus(UserErrorCode.INVALID_AGE).getCode());
        assertEquals(Status.Code.INVALID_ARGUMENT, UserGrpcStatusMapper.toStatus(UserErrorCode.INVALID_HEIGHT).getCode());
        assertEquals(Status.Code.INVALID_ARGUMENT, UserGrpcStatusMapper.toStatus(UserErrorCode.INVALID_BIO).getCode());
        assertEquals(Status.Code.ABORTED, UserGrpcStatusMapper.toStatus(UserErrorCode.PROFILE_UPDATE_FAILED).getCode());
        assertEquals(Status.Code.INVALID_ARGUMENT, UserGrpcStatusMapper.toStatus(UserErrorCode.PROFILE_UPDATE_INVALID).getCode());
        assertEquals(Status.Code.NOT_FOUND, UserGrpcStatusMapper.toStatus(UserErrorCode.PROFILE_NOT_FOUND).getCode());
    }

    @Test
    void swaggerUploadErrorCodesShouldMapAsExpected() {
        assertEquals(Status.Code.INVALID_ARGUMENT, UserGrpcStatusMapper.toStatus(UserErrorCode.INVALID_AVATAR_EXT).getCode());
        assertEquals(Status.Code.INVALID_ARGUMENT, UserGrpcStatusMapper.toStatus(UserErrorCode.AVATAR_SIZE_EXCEEDED).getCode());
        assertEquals(Status.Code.INVALID_ARGUMENT, UserGrpcStatusMapper.toStatus(UserErrorCode.INVALID_AVATAR_OBJECT_KEY).getCode());
        assertEquals(Status.Code.PERMISSION_DENIED, UserGrpcStatusMapper.toStatus(UserErrorCode.AVATAR_OBJECT_NOT_BELONG_TO_USER).getCode());
        assertEquals(Status.Code.NOT_FOUND, UserGrpcStatusMapper.toStatus(UserErrorCode.AVATAR_OBJECT_NOT_FOUND).getCode());
        assertEquals(Status.Code.UNAVAILABLE, UserGrpcStatusMapper.toStatus(UserErrorCode.AVATAR_OBJECT_STAT_FAILED).getCode());
        assertEquals(Status.Code.UNAVAILABLE, UserGrpcStatusMapper.toStatus(UserErrorCode.AVATAR_PRESIGN_FAILED).getCode());
        assertEquals(Status.Code.ABORTED, UserGrpcStatusMapper.toStatus(UserErrorCode.AVATAR_CONFIRM_FAILED).getCode());
    }

    @Test
    void swaggerHomeCardErrorCodesShouldMapAsExpected() {
        assertEquals(Status.Code.INVALID_ARGUMENT, UserGrpcStatusMapper.toStatus(UserErrorCode.INVALID_USER_ID).getCode());
        assertEquals(Status.Code.INVALID_ARGUMENT, UserGrpcStatusMapper.toStatus(UserErrorCode.INVALID_TARGET_USER_ID).getCode());
        assertEquals(Status.Code.NOT_FOUND, UserGrpcStatusMapper.toStatus(UserErrorCode.TARGET_USER_NOT_FOUND).getCode());
        assertEquals(Status.Code.PERMISSION_DENIED, UserGrpcStatusMapper.toStatus(UserErrorCode.TARGET_USER_UNAVAILABLE).getCode());
        assertEquals(Status.Code.ABORTED, UserGrpcStatusMapper.toStatus(UserErrorCode.HOME_CARD_QUERY_FAILED).getCode());
    }

    @Test
    void commonErrorCodesShouldMapAsExpected() {
        assertEquals(Status.Code.NOT_FOUND, UserGrpcStatusMapper.toStatus(UserErrorCode.USER_NOT_FOUND).getCode());
        assertEquals(Status.Code.PERMISSION_DENIED, UserGrpcStatusMapper.toStatus(UserErrorCode.USER_DISABLED).getCode());
        assertEquals(Status.Code.PERMISSION_DENIED, UserGrpcStatusMapper.toStatus(UserErrorCode.USER_BANNED).getCode());
        assertEquals(Status.Code.INTERNAL, UserGrpcStatusMapper.toStatus(UserErrorCode.INTERNAL_ERROR).getCode());
        assertEquals(Status.Code.INTERNAL, UserGrpcStatusMapper.toStatus(null).getCode());
    }

    @Test
    void objectStorageUnavailableShouldMapToUnavailable() {
        assertEquals(Status.Code.UNAVAILABLE, UserGrpcStatusMapper.toStatus(UserErrorCode.AVATAR_PRESIGN_FAILED).getCode());
    }

    @Test
    void userUnavailableShouldMapToPermissionDenied() {
        assertEquals(Status.Code.PERMISSION_DENIED, UserGrpcStatusMapper.toStatus(UserErrorCode.TARGET_USER_UNAVAILABLE).getCode());
    }
}
