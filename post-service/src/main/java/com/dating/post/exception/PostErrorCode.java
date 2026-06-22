package com.dating.post.exception;

/**
 * Post 域业务错误码（阶段 1 常量定义，阶段 2+ 随业务扩展）。
 */
public enum PostErrorCode {

    INVALID_ARGUMENT("INVALID_ARGUMENT", "参数非法"),
    UNAUTHORIZED("UNAUTHORIZED", "缺少调用方用户身份"),
    FORBIDDEN("FORBIDDEN", "无权操作该资源"),
    POST_NOT_FOUND("POST_NOT_FOUND", "帖子不存在"),
    COMMENT_NOT_FOUND("COMMENT_NOT_FOUND", "评论不存在"),
    INTERNAL_ERROR("INTERNAL_ERROR", "系统内部错误");

    private final String code;
    private final String message;

    PostErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
