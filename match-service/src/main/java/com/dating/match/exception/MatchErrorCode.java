package com.dating.match.exception;

/**
 * Match 域业务错误码。
 */
public enum MatchErrorCode {

    INVALID_ARGUMENT("INVALID_ARGUMENT", "参数非法"),
    TARGET_NOT_FOUND("TARGET_NOT_FOUND", "目标用户不存在"),
    DUPLICATE_SWIPE("DUPLICATE_SWIPE", "重复划卡"),
    QUOTA_CARD_EXCEEDED("QUOTA_CARD_EXCEEDED", "今日卡片配额已用尽"),
    QUOTA_RIGHT_SWIPE_EXCEEDED("QUOTA_RIGHT_SWIPE_EXCEEDED", "今日右划配额已用尽"),
    QUOTA_SUPER_HI_EXCEEDED("QUOTA_SUPER_HI_EXCEEDED", "SuperHi 配额已用尽"),
    INSUFFICIENT_COINS("INSUFFICIENT_COINS", "金币不足"),
    CONCURRENT_SWIPE("CONCURRENT_SWIPE", "划卡并发冲突"),
    MATCH_ALREADY_EXISTS("MATCH_ALREADY_EXISTS", "匹配关系已存在"),
    OUTBOX_RETRY_FAILED("OUTBOX_RETRY_FAILED", "Outbox 重试失败"),
    INTERNAL_ERROR("INTERNAL_ERROR", "系统内部错误");

    private final String code;
    private final String message;

    MatchErrorCode(String code, String message) {
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
