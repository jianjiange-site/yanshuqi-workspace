package com.dating.gateway.support;

import com.dating.gateway.exception.GatewayBizException;
import com.dating.gateway.exception.GatewayErrorCode;

/**
 * 下游能力未就绪时的统一异常工厂；prod/dev 禁止返回 mock 数据。
 */
public final class GatewayFeatureNotReadySupport {

    private GatewayFeatureNotReadySupport() {
    }

    public static GatewayBizException paymentNotReady() {
        return new GatewayBizException(GatewayErrorCode.PAYMENT_SERVICE_NOT_READY);
    }

    public static GatewayBizException imNotReady() {
        return new GatewayBizException(GatewayErrorCode.IM_SERVICE_NOT_READY);
    }

    public static GatewayBizException callNotReady() {
        return new GatewayBizException(GatewayErrorCode.CALL_SERVICE_NOT_READY);
    }

    public static GatewayBizException callbackNotReady() {
        return new GatewayBizException(GatewayErrorCode.CALLBACK_SERVICE_NOT_READY);
    }
}
