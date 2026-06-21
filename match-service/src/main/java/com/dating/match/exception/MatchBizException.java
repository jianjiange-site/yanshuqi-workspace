package com.dating.match.exception;

/**
 * Match 域业务异常。
 */
public class MatchBizException extends RuntimeException {

    private final MatchErrorCode errorCode;

    public MatchBizException(MatchErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public MatchBizException(MatchErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public MatchErrorCode getErrorCode() {
        return errorCode;
    }
}
