package com.dating.gateway.converter;

import com.dating.gateway.exception.GatewayBizException;
import com.dating.gateway.exception.GatewayErrorCode;

/**
 * Swagger gender（0/1/2 int）与 user proto gender（MALE/FEMALE/OTHER/UNKNOWN 字符串）互转。
 */
public final class GenderCodec {

    private GenderCodec() {
    }

    /**
     * Swagger → user-service proto 字符串；null 表示调用方未传，不写入 proto。
     */
    public static String toProtoGender(Integer swaggerGender) {
        if (swaggerGender == null) {
            return null;
        }
        return switch (swaggerGender) {
            case 0 -> "UNKNOWN";
            case 1 -> "MALE";
            case 2 -> "FEMALE";
            default -> throw new GatewayBizException(GatewayErrorCode.INVALID_ARGUMENT, "gender 非法，仅支持 0/1/2");
        };
    }

    /**
     * user proto 字符串 → Swagger int；空或未知枚举映射为 0（未指定）。
     */
    public static Integer toSwaggerGender(String protoGender) {
        if (protoGender == null || protoGender.isBlank()) {
            return 0;
        }
        return switch (protoGender.trim().toUpperCase()) {
            case "MALE" -> 1;
            case "FEMALE" -> 2;
            default -> 0;
        };
    }
}
