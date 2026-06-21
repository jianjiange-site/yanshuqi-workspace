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

    /** 设备标识非法。 */
    INVALID_DEVICE_ID("INVALID_DEVICE_ID", "设备标识非法"),

    /** 设备平台非法。 */
    INVALID_PLATFORM("INVALID_PLATFORM", "设备平台非法"),

    /** 手机号非法。 */
    INVALID_PHONE("INVALID_PHONE", "手机号非法"),

    /** 短信验证码非法。 */
    INVALID_SMS_CODE("INVALID_SMS_CODE", "短信验证码非法"),

    /** 三方平台非法。 */
    INVALID_THIRD_PARTY_PLATFORM("INVALID_THIRD_PARTY_PLATFORM", "三方平台非法"),

    /** 三方身份非法。 */
    INVALID_THIRD_PARTY_IDENTITY("INVALID_THIRD_PARTY_IDENTITY", "三方身份非法"),

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

    /** 昵称非法。 */
    INVALID_NICKNAME("INVALID_NICKNAME", "昵称非法"),

    /** 性别非法。 */
    INVALID_GENDER("INVALID_GENDER", "性别非法"),

    /** 生日非法。 */
    INVALID_BIRTHDAY("INVALID_BIRTHDAY", "生日非法"),

    /** 年龄非法。 */
    INVALID_AGE("INVALID_AGE", "年龄非法"),

    /** 身高非法。 */
    INVALID_HEIGHT("INVALID_HEIGHT", "身高非法"),

    /** 简介非法。 */
    INVALID_BIO("INVALID_BIO", "简介非法"),

    /** 资料更新失败。 */
    PROFILE_UPDATE_FAILED("PROFILE_UPDATE_FAILED", "资料更新失败"),

    /** 照片 object key 非法。 */
    PHOTO_OBJECT_KEY_INVALID("PHOTO_OBJECT_KEY_INVALID", "照片 object key 非法"),

    /** 头像 ext 非法。 */
    INVALID_AVATAR_EXT("INVALID_AVATAR_EXT", "头像扩展名非法"),

    /** 头像大小超限。 */
    AVATAR_SIZE_EXCEEDED("AVATAR_SIZE_EXCEEDED", "头像大小超限"),

    /** 头像 object key 非法。 */
    INVALID_AVATAR_OBJECT_KEY("INVALID_AVATAR_OBJECT_KEY", "头像 object key 非法"),

    /** 头像 object 不属于当前用户。 */
    AVATAR_OBJECT_NOT_BELONG_TO_USER("AVATAR_OBJECT_NOT_BELONG_TO_USER", "头像 object 不属于当前用户"),

    /** 头像 object 不存在。 */
    AVATAR_OBJECT_NOT_FOUND("AVATAR_OBJECT_NOT_FOUND", "头像 object 不存在"),

    /** 头像 object stat 失败。 */
    AVATAR_OBJECT_STAT_FAILED("AVATAR_OBJECT_STAT_FAILED", "头像 object 查询失败"),

    /** 头像 presign 失败。 */
    AVATAR_PRESIGN_FAILED("AVATAR_PRESIGN_FAILED", "头像 presign 失败"),

    /** 头像 confirm 失败。 */
    AVATAR_CONFIRM_FAILED("AVATAR_CONFIRM_FAILED", "头像 confirm 失败"),

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

    /** 用户 ID 非法。 */
    INVALID_USER_ID("INVALID_USER_ID", "用户 ID 非法"),

    /** 目标用户 ID 非法。 */
    INVALID_TARGET_USER_ID("INVALID_TARGET_USER_ID", "目标用户 ID 非法"),

    /** 目标用户不存在。 */
    TARGET_USER_NOT_FOUND("TARGET_USER_NOT_FOUND", "目标用户不存在"),

    /** 目标用户不可展示。 */
    TARGET_USER_UNAVAILABLE("TARGET_USER_UNAVAILABLE", "目标用户不可展示"),

    /** 主页卡片查询失败。 */
    HOME_CARD_QUERY_FAILED("HOME_CARD_QUERY_FAILED", "主页卡片查询失败"),

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
