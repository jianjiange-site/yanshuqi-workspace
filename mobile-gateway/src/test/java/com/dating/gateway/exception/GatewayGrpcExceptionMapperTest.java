package com.dating.gateway.exception;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GatewayGrpcExceptionMapperTest {

    @Test
    void resourceExhausted_shouldMapToQuotaExhausted() {
        GatewayBizException ex = GatewayGrpcExceptionMapper.toGatewayException(
                Status.RESOURCE_EXHAUSTED.withDescription("QUOTA_CARD_EXCEEDED").asRuntimeException());
        assertEquals(GatewayErrorCode.QUOTA_EXHAUSTED, ex.getErrorCode());
    }

    @Test
    void unavailable_shouldMapToUpstreamUnavailable() {
        GatewayBizException ex = GatewayGrpcExceptionMapper.toGatewayException(
                Status.UNAVAILABLE.asRuntimeException());
        assertEquals(GatewayErrorCode.UPSTREAM_UNAVAILABLE, ex.getErrorCode());
    }
}
