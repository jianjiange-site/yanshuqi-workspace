package com.dating.user.exception;

/**
 * 用户域业务错误码定义，本阶段仅建立基础结构。
 */
public enum UserErrorCode {

    /** 用户不存在。 */
    USER_NOT_FOUND("USER_NOT_FOUND", "用户不存在"),

    /** 登录凭证不存在。 */
    AUTH_IDENTITY_NOT_FOUND("AUTH_IDENTITY_NOT_FOUND", "登录凭证不存在"),

    /** 用户资料不存在。 */
    PROFILE_NOT_FOUND("PROFILE_NOT_FOUND", "用户资料不存在"),

    /** 参数非法。 */
    INVALID_PARAMETER("INVALID_PARAMETER", "参数非法"),

    /** 系统内部错误。 */
    INTERNAL_ERROR("INTERNAL_ERROR", "系统内部错误");

    private final String code;

    private final String message;

    /**
     * 构造错误码枚举。
     *
     * @param code    错误码
     * @param message 错误描述
     */
    UserErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 获取错误码。
     *
     * @return 错误码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取错误描述。
     *
     * @return 错误描述
     */
    public String getMessage() {
        return message;
    }
}
