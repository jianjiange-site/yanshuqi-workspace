package com.dating.gateway.exception;

/**
 * 网关业务异常，由全局处理器转换为 {@link com.dating.gateway.common.Result}。
 */
public class GatewayBizException extends RuntimeException {

    private final GatewayErrorCode errorCode;
    private final String detailMessage;

    public GatewayBizException(GatewayErrorCode errorCode) {
        this(errorCode, errorCode.getMessage());
    }

    public GatewayBizException(GatewayErrorCode errorCode, String detailMessage) {
        super(detailMessage);
        this.errorCode = errorCode;
        this.detailMessage = detailMessage;
    }

    public GatewayErrorCode getErrorCode() {
        return errorCode;
    }

    public String getDetailMessage() {
        return detailMessage;
    }
}
