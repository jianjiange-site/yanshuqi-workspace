package com.dating.post.exception;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

/**
 * Post 业务错误码到 gRPC Status 映射。
 */
public final class PostGrpcStatusMapper {

    private PostGrpcStatusMapper() {
    }

    public static Status toStatus(PostErrorCode errorCode) {
        if (errorCode == null) {
            return Status.INTERNAL;
        }
        return switch (errorCode) {
            case INVALID_ARGUMENT -> Status.INVALID_ARGUMENT;
            case UNAUTHORIZED -> Status.UNAUTHENTICATED;
            case FORBIDDEN -> Status.PERMISSION_DENIED;
            case POST_NOT_FOUND, COMMENT_NOT_FOUND -> Status.NOT_FOUND;
            case INTERNAL_ERROR -> Status.INTERNAL;
        };
    }

    public static StatusRuntimeException toRuntimeException(PostBusinessException exception) {
        PostErrorCode errorCode = exception.getErrorCode();
        return toStatus(errorCode).withDescription(errorCode.getCode()).asRuntimeException();
    }

    public static StatusRuntimeException toInternalException() {
        return Status.INTERNAL
                .withDescription(PostErrorCode.INTERNAL_ERROR.getCode())
                .asRuntimeException();
    }
}
