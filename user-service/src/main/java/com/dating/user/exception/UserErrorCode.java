package com.dating.user.exception;

/**
 * 用户域业务错误码定义。
 */
public enum UserErrorCode {

    /** 用户不存在。 */
    USER_NOT_FOUND("USER_NOT_FOUND", "用户不存在"),

    /** 登录凭证不存在。 */
    AUTH_IDENTITY_NOT_FOUND("AUTH_IDENTITY_NOT_FOUND", "登录凭证不存在"),

    /** 登录凭证未找到（登录场景）。 */
    IDENTITY_NOT_FOUND("IDENTITY_NOT_FOUND", "登录凭证不存在"),

    /** 用户资料不存在。 */
    PROFILE_NOT_FOUND("PROFILE_NOT_FOUND", "用户资料不存在"),

    /** 参数非法。 */
    INVALID_PARAMETER("INVALID_PARAMETER", "参数非法"),

    /** 注册请求参数非法。 */
    USER_REQUEST_INVALID("USER_REQUEST_INVALID", "请求参数非法"),

    /** 登录凭证已存在。 */
    IDENTITY_ALREADY_EXISTS("IDENTITY_ALREADY_EXISTS", "登录凭证已存在"),

    /** 密码非法或校验失败。 */
    PASSWORD_INVALID("PASSWORD_INVALID", "密码校验失败"),

    /** 用户已停用。 */
    USER_DISABLED("USER_DISABLED", "用户已停用"),

    /** 用户已封禁。 */
    USER_BANNED("USER_BANNED", "用户已封禁"),

    /** 用户已注销。 */
    USER_DELETED("USER_DELETED", "用户已注销"),

    /** 并发冲突。 */
    USER_CONCURRENT_CONFLICT("USER_CONCURRENT_CONFLICT", "操作并发冲突"),

    /** 资料更新参数非法。 */
    PROFILE_UPDATE_INVALID("PROFILE_UPDATE_INVALID", "资料更新参数非法"),

    /** 照片 object key 非法。 */
    PHOTO_OBJECT_KEY_INVALID("PHOTO_OBJECT_KEY_INVALID", "照片 object key 非法"),

    /** 照片审核未通过。 */
    PHOTO_REVIEW_NOT_APPROVED("PHOTO_REVIEW_NOT_APPROVED", "照片审核未通过"),

    /** 照片不存在。 */
    PHOTO_NOT_FOUND("PHOTO_NOT_FOUND", "照片不存在"),

    /** 照片类型非法。 */
    PHOTO_TYPE_INVALID("PHOTO_TYPE_INVALID", "照片类型非法"),

    /** 照片数量超限。 */
    PHOTO_LIMIT_EXCEEDED("PHOTO_LIMIT_EXCEEDED", "照片数量超限"),

    /** 批量查询用户数量超限。 */
    USER_BATCH_SIZE_EXCEEDED("USER_BATCH_SIZE_EXCEEDED", "批量查询用户数量超限"),

    /** 用户资料批量查询参数非法。 */
    USER_PROFILE_QUERY_INVALID("USER_PROFILE_QUERY_INVALID", "用户资料批量查询参数非法"),

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
