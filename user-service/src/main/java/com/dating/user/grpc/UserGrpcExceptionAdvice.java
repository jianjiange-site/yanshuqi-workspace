package com.dating.user.grpc;

import com.dating.user.exception.UserBizException;
import com.dating.user.exception.UserErrorCode;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

/**
 * gRPC 全局异常转换，将用户域业务异常映射为 gRPC Status。
 */
@GrpcAdvice
public class UserGrpcExceptionAdvice {

    /**
     * 将 UserBizException 转换为 gRPC StatusRuntimeException。
     *
     * @param exception 用户域业务异常
     * @return gRPC 运行时异常
     */
    @GrpcExceptionHandler(UserBizException.class)
    public StatusRuntimeException handleUserBizException(UserBizException exception) {
        UserErrorCode errorCode = exception.getErrorCode();
        Status status = mapErrorCode(errorCode).withDescription(errorCode.getCode());
        return status.asRuntimeException();
    }

    private Status mapErrorCode(UserErrorCode errorCode) {
        return switch (errorCode) {
            case USER_REQUEST_INVALID, INVALID_PARAMETER, PROFILE_UPDATE_INVALID,
                    PHOTO_OBJECT_KEY_INVALID, PHOTO_TYPE_INVALID, PHOTO_LIMIT_EXCEEDED -> Status.INVALID_ARGUMENT;
            case PASSWORD_INVALID, IDENTITY_NOT_FOUND, AUTH_IDENTITY_NOT_FOUND -> Status.UNAUTHENTICATED;
            case USER_DISABLED, USER_BANNED, USER_DELETED -> Status.PERMISSION_DENIED;
            case IDENTITY_ALREADY_EXISTS -> Status.ALREADY_EXISTS;
            case USER_CONCURRENT_CONFLICT -> Status.ABORTED;
            case USER_NOT_FOUND, PROFILE_NOT_FOUND, PHOTO_NOT_FOUND -> Status.NOT_FOUND;
            default -> Status.INTERNAL;
        };
    }
}
