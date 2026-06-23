package com.dating.gateway.support;

import com.dating.gateway.exception.GatewayBizException;
import com.dating.gateway.exception.GatewayErrorCode;
import org.springframework.util.StringUtils;

/**
 * Payment REST 入参边界校验。
 */
public final class PaymentParamSupport {

    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_SIZE = 20;
    public static final int MIN_PAGE = 1;
    public static final int MAX_SIZE = 100;

    private PaymentParamSupport() {
    }

    public static int clampPage(int page) {
        return page <= 0 ? DEFAULT_PAGE : Math.max(page, MIN_PAGE);
    }

    public static int clampSize(int size) {
        if (size <= 0) {
            return DEFAULT_SIZE;
        }
        return Math.min(Math.max(size, 1), MAX_SIZE);
    }

    public static void requireText(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new GatewayBizException(GatewayErrorCode.INVALID_ARGUMENT, fieldName + " 不能为空");
        }
    }
}
