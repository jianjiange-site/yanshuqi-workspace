package com.dating.user.exception;

/**
 * 用户域业务异常，本阶段仅建立基础结构，不含 gRPC 异常转换。
 */
public class UserBizException extends RuntimeException {

    private final UserErrorCode errorCode;

    /**
     * 根据错误码构造业务异常。
     *
     * @param errorCode 用户域错误码
     */
    public UserBizException(UserErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 根据错误码和自定义消息构造业务异常。
     *
     * @param errorCode 用户域错误码
     * @param message   自定义错误消息
     */
    public UserBizException(UserErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 获取用户域错误码。
     *
     * @return 错误码枚举
     */
    public UserErrorCode getErrorCode() {
        return errorCode;
    }
}
