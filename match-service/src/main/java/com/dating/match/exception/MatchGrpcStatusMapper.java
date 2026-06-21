package com.dating.match.exception;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

/**
 * Match 业务错误码到 gRPC Status 映射。
 */
public final class MatchGrpcStatusMapper {

    private MatchGrpcStatusMapper() {
    }

    public static Status toStatus(MatchErrorCode errorCode) {
        if (errorCode == null) {
            return Status.INTERNAL;
        }
        return switch (errorCode) {
            case INVALID_ARGUMENT -> Status.INVALID_ARGUMENT;
            case TARGET_NOT_FOUND -> Status.NOT_FOUND;
            case QUOTA_CARD_EXCEEDED, QUOTA_RIGHT_SWIPE_EXCEEDED, QUOTA_SUPER_HI_EXCEEDED ->
                    Status.RESOURCE_EXHAUSTED;
            case INSUFFICIENT_COINS -> Status.FAILED_PRECONDITION;
            case CONCURRENT_SWIPE, DUPLICATE_SWIPE -> Status.ABORTED;
            case MATCH_ALREADY_EXISTS -> Status.ALREADY_EXISTS;
            case OUTBOX_RETRY_FAILED -> Status.UNAVAILABLE;
            case INTERNAL_ERROR -> Status.INTERNAL;
        };
    }

    public static StatusRuntimeException toRuntimeException(MatchBizException exception) {
        MatchErrorCode errorCode = exception.getErrorCode();
        return toStatus(errorCode).withDescription(errorCode.getCode()).asRuntimeException();
    }

    public static StatusRuntimeException toInternalException() {
        return Status.INTERNAL
                .withDescription(MatchErrorCode.INTERNAL_ERROR.getCode())
                .asRuntimeException();
    }
}
