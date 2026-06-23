package com.dating.gateway.exception;

/**
 * mobile-gateway 业务错误码（10500+）。
 */
public enum GatewayErrorCode {

    TOKEN_INVALID(10501, "访问令牌无效"),
    TOKEN_EXPIRED(10502, "访问令牌已过期"),
    TOKEN_REVOKED(10503, "访问令牌已撤销"),
    REFRESH_TOKEN_REUSED(10504, "刷新令牌已被使用"),
    REFRESH_TOKEN_DEVICE_MISMATCH(10505, "刷新令牌与设备不匹配"),
    REFRESH_TOKEN_INVALID(10506, "刷新令牌无效"),
    REFRESH_TOKEN_EXPIRED(10507, "刷新令牌已过期"),
    REFRESH_TOKEN_REVOKED(10508, "刷新令牌已撤销"),
    SMS_CODE_INVALID(10601, "短信验证码错误"),
    SMS_CODE_EXPIRED(10602, "短信验证码已过期"),
    THIRD_PARTY_TOKEN_INVALID(10603, "三方登录凭证无效"),
    SMS_COOLDOWN(10604, "短信发送过于频繁"),
    INVALID_ARGUMENT(10400, "请求参数非法"),
    USER_NOT_FOUND(10510, "用户不存在"),
    PERMISSION_DENIED(10511, "无权限访问"),
    TARGET_NOT_FOUND(10512, "目标不存在"),
    POST_NOT_FOUND(10513, "帖子不存在"),
    COMMENT_NOT_FOUND(10514, "评论不存在"),
    QUOTA_EXHAUSTED(10520, "配额不足"),
    INSUFFICIENT_COINS(10521, "金币不足"),
    PAYMENT_SERVICE_NOT_READY(10701, "payment-service 尚未就绪"),
    IM_SERVICE_NOT_READY(10801, "im-service token 尚未就绪"),
    CALL_SERVICE_NOT_READY(10802, "call token 服务尚未就绪"),
    CALLBACK_SERVICE_NOT_READY(10803, "OpenIM callback 转发尚未就绪"),
    UPSTREAM_UNAVAILABLE(10901, "上游服务不可用"),
    UPSTREAM_NOT_IMPLEMENTED(10902, "上游能力尚未实现");

    private final int code;
    private final String message;

    GatewayErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
