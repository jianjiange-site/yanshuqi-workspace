package com.dating.user.exception;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

/**
 * 用户域业务错误码到 gRPC Status 的集中映射器。
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
                    PHOTO_REVIEW_NOT_APPROVED, USER_BATCH_SIZE_EXCEEDED, USER_PROFILE_QUERY_INVALID ->
                    Status.INVALID_ARGUMENT;
            case PASSWORD_INVALID, IDENTITY_NOT_FOUND, AUTH_IDENTITY_NOT_FOUND -> Status.UNAUTHENTICATED;
            case USER_DISABLED, USER_BANNED, USER_DELETED -> Status.PERMISSION_DENIED;
            case IDENTITY_ALREADY_EXISTS -> Status.ALREADY_EXISTS;
            case USER_CONCURRENT_CONFLICT -> Status.ABORTED;
            case USER_NOT_FOUND, PROFILE_NOT_FOUND, PHOTO_NOT_FOUND -> Status.NOT_FOUND;
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
