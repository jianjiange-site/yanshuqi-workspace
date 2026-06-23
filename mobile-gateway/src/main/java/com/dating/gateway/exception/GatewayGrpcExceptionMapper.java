package com.dating.gateway.exception;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.springframework.util.StringUtils;

/**
 * gRPC StatusRuntimeException → {@link GatewayBizException} 统一映射，供各下游 gRPC Client 复用。
 * <p>
 * Controller 层禁止捕获 StatusRuntimeException；所有转换在此完成。
 */
public final class GatewayGrpcExceptionMapper {

    private GatewayGrpcExceptionMapper() {
    }

    public static GatewayBizException toGatewayException(StatusRuntimeException ex) {
        Status.Code code = ex.getStatus().getCode();
        String description = ex.getStatus().getDescription();
        return switch (code) {
            case NOT_FOUND -> new GatewayBizException(GatewayErrorCode.USER_NOT_FOUND, detailOrDefault(description,
                    GatewayErrorCode.USER_NOT_FOUND.getMessage()));
            case PERMISSION_DENIED -> new GatewayBizException(GatewayErrorCode.PERMISSION_DENIED, detailOrDefault(
                    description, GatewayErrorCode.PERMISSION_DENIED.getMessage()));
            case RESOURCE_EXHAUSTED -> new GatewayBizException(GatewayErrorCode.QUOTA_EXHAUSTED, detailOrDefault(
                    description, GatewayErrorCode.QUOTA_EXHAUSTED.getMessage()));
            case FAILED_PRECONDITION -> new GatewayBizException(GatewayErrorCode.INSUFFICIENT_COINS, detailOrDefault(
                    description, GatewayErrorCode.INSUFFICIENT_COINS.getMessage()));
            case INVALID_ARGUMENT -> new GatewayBizException(GatewayErrorCode.INVALID_ARGUMENT, detailOrDefault(
                    description, GatewayErrorCode.INVALID_ARGUMENT.getMessage()));
            case UNAUTHENTICATED -> new GatewayBizException(GatewayErrorCode.TOKEN_INVALID, detailOrDefault(
                    description, GatewayErrorCode.TOKEN_INVALID.getMessage()));
            case UNAVAILABLE, DEADLINE_EXCEEDED -> new GatewayBizException(GatewayErrorCode.UPSTREAM_UNAVAILABLE);
            default -> new GatewayBizException(GatewayErrorCode.UPSTREAM_UNAVAILABLE);
        };
    }

    private static String detailOrDefault(String description, String defaultMessage) {
        return StringUtils.hasText(description) ? description : defaultMessage;
    }
}
