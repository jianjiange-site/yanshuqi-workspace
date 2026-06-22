package com.dating.post.exception;

/**
 * Post 域业务异常基类。
 */
public class PostBusinessException extends RuntimeException {

    private final PostErrorCode errorCode;

    public PostBusinessException(PostErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public PostBusinessException(PostErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public PostErrorCode getErrorCode() {
        return errorCode;
    }
}
