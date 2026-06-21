package com.dating.user.exception;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

/**
 * 用户域业务错误码到 gRPC Status 的集中映射器。
 * <p>
 * 分层原则：参数非法 → INVALID_ARGUMENT；身份非法 → UNAUTHENTICATED；
 * 资源不存在 → NOT_FOUND；账号/对象不可用 → PERMISSION_DENIED；
 * 对象存储不可用 → UNAVAILABLE；并发/写冲突 → ABORTED；其余 → INTERNAL。
 */
public final class UserGrpcStatusMapper {

    private UserGrpcStatusMapper() {
    }

    /**
     * 将用户域错误码映射为 gRPC Status。
     *
     * @param errorCode 用户域错误码
     * @return gRPC Status
     */
    public static Status toStatus(UserErrorCode errorCode) {
        if (errorCode == null) {
            return Status.INTERNAL;
        }
        return switch (errorCode) {
            case USER_REQUEST_INVALID, INVALID_PARAMETER, PROFILE_UPDATE_INVALID,
                    PHOTO_OBJECT_KEY_INVALID, PHOTO_TYPE_INVALID, PHOTO_LIMIT_EXCEEDED,
                    PHOTO_REVIEW_NOT_APPROVED, USER_BATCH_SIZE_EXCEEDED, USER_PROFILE_QUERY_INVALID,
                    INVALID_DEVICE_ID, INVALID_PLATFORM, INVALID_PHONE, INVALID_SMS_CODE,
                    INVALID_THIRD_PARTY_PLATFORM, INVALID_NICKNAME, INVALID_GENDER, INVALID_BIRTHDAY,
                    INVALID_AGE, INVALID_HEIGHT, INVALID_BIO, INVALID_AVATAR_EXT, AVATAR_SIZE_EXCEEDED,
                    INVALID_AVATAR_OBJECT_KEY, INVALID_USER_ID, INVALID_TARGET_USER_ID ->
                    Status.INVALID_ARGUMENT;
            case PASSWORD_INVALID, IDENTITY_NOT_FOUND, AUTH_IDENTITY_NOT_FOUND,
                    INVALID_THIRD_PARTY_IDENTITY -> Status.UNAUTHENTICATED;
            case USER_DISABLED, USER_BANNED, USER_DELETED, AVATAR_OBJECT_NOT_BELONG_TO_USER,
                    TARGET_USER_UNAVAILABLE ->
                    Status.PERMISSION_DENIED;
            case IDENTITY_ALREADY_EXISTS -> Status.ALREADY_EXISTS;
            case USER_CONCURRENT_CONFLICT, PROFILE_UPDATE_FAILED, AVATAR_CONFIRM_FAILED,
                    HOME_CARD_QUERY_FAILED -> Status.ABORTED;
            case USER_NOT_FOUND, PROFILE_NOT_FOUND, PHOTO_NOT_FOUND, AVATAR_OBJECT_NOT_FOUND,
                    TARGET_USER_NOT_FOUND ->
                    Status.NOT_FOUND;
            case AVATAR_OBJECT_STAT_FAILED, AVATAR_PRESIGN_FAILED -> Status.UNAVAILABLE;
            case INTERNAL_ERROR -> Status.INTERNAL;
        };
    }

    /**
     * 将 UserBizException 转换为 gRPC StatusRuntimeException，不向调用方泄露堆栈。
     *
     * @param exception 用户域业务异常
     * @return gRPC 运行时异常
     */
    public static StatusRuntimeException toRuntimeException(UserBizException exception) {
        UserErrorCode errorCode = exception.getErrorCode();
        Status status = toStatus(errorCode).withDescription(errorCode.getCode());
        return status.asRuntimeException();
    }

    /**
     * 将未知异常映射为 INTERNAL，描述仅返回统一错误码。
     *
     * @return gRPC 运行时异常
     */
    public static StatusRuntimeException toInternalException() {
        return Status.INTERNAL
                .withDescription(UserErrorCode.INTERNAL_ERROR.getCode())
                .asRuntimeException();
    }
}
