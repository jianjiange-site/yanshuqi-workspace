package com.dating.match.exception;

import io.grpc.Status;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MatchGrpcStatusMapperTest {

    @Test
    void shouldMapInvalidArgument() {
        assertEquals(Status.Code.INVALID_ARGUMENT,
                MatchGrpcStatusMapper.toStatus(MatchErrorCode.INVALID_ARGUMENT).getCode());
    }

    @Test
    void shouldMapTargetNotFound() {
        assertEquals(Status.Code.NOT_FOUND,
                MatchGrpcStatusMapper.toStatus(MatchErrorCode.TARGET_NOT_FOUND).getCode());
    }

    @Test
    void shouldMapQuotaExceeded() {
        assertEquals(Status.Code.RESOURCE_EXHAUSTED,
                MatchGrpcStatusMapper.toStatus(MatchErrorCode.QUOTA_CARD_EXCEEDED).getCode());
        assertEquals(Status.Code.RESOURCE_EXHAUSTED,
                MatchGrpcStatusMapper.toStatus(MatchErrorCode.QUOTA_RIGHT_SWIPE_EXCEEDED).getCode());
        assertEquals(Status.Code.RESOURCE_EXHAUSTED,
                MatchGrpcStatusMapper.toStatus(MatchErrorCode.QUOTA_SUPER_HI_EXCEEDED).getCode());
    }

    @Test
    void shouldMapInsufficientCoins() {
        assertEquals(Status.Code.FAILED_PRECONDITION,
                MatchGrpcStatusMapper.toStatus(MatchErrorCode.INSUFFICIENT_COINS).getCode());
    }

    @Test
    void shouldMapConcurrentSwipe() {
        assertEquals(Status.Code.ABORTED,
                MatchGrpcStatusMapper.toStatus(MatchErrorCode.CONCURRENT_SWIPE).getCode());
    }

    @Test
    void shouldMapInternalError() {
        assertEquals(Status.Code.INTERNAL,
                MatchGrpcStatusMapper.toStatus(MatchErrorCode.INTERNAL_ERROR).getCode());
    }

    @Test
    void shouldConvertBizExceptionToRuntimeException() {
        MatchBizException ex = new MatchBizException(MatchErrorCode.DUPLICATE_SWIPE);
        assertEquals(Status.Code.ABORTED,
                MatchGrpcStatusMapper.toRuntimeException(ex).getStatus().getCode());
    }
}
