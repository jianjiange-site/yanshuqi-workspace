package com.dating.gateway.security;

import com.dating.gateway.exception.GatewayBizException;
import com.dating.gateway.exception.GatewayErrorCode;

/**
 * Swagger platform 整型与 user-service 字符串平台之间的转换。
 * <p>
 * 1=IOS, 2=ANDROID, 3=WEB（与 App Swagger 约定一致）。
 */
public final class PlatformCodec {

    private PlatformCodec() {
    }

    public static String toUserServicePlatform(int platformCode) {
        return switch (parsePlatformCode(platformCode)) {
            case 1 -> "IOS";
            case 2 -> "ANDROID";
            case 3 -> "WEB";
            default -> throw new GatewayBizException(GatewayErrorCode.INVALID_ARGUMENT, "platform 非法，仅支持 1/2/3");
        };
    }

    public static int parsePlatformCode(int platformCode) {
        if (platformCode < 1 || platformCode > 3) {
            throw new GatewayBizException(GatewayErrorCode.INVALID_ARGUMENT, "platform 非法，仅支持 1/2/3");
        }
        return platformCode;
    }
}
