package com.dating.user.service.support;

import org.springframework.util.StringUtils;

/**
 * 日志字段脱敏工具，提供基础字符串裁剪与缩略能力。
 */
public final class LogMaskUtil {

    private static final int DEFAULT_MAX_LENGTH = 32;

    private LogMaskUtil() {
    }

    /**
     * 手机号脱敏，保留前 3 后 2。
     *
     * @param phone 原始手机号
     * @return 脱敏后的手机号
     */
    public static String maskPhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return "";
        }
        String trimmed = phone.trim();
        if (trimmed.length() <= 5) {
            return "***";
        }
        return trimmed.substring(0, 3) + "****" + trimmed.substring(trimmed.length() - 2);
    }

    /**
     * 邮箱脱敏，保留首字符与域名。
     *
     * @param email 原始邮箱
     * @return 脱敏后的邮箱
     */
    public static String maskEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return "";
        }
        String trimmed = email.trim();
        int at = trimmed.indexOf('@');
        if (at <= 0) {
            return truncate(trimmed, 8);
        }
        return trimmed.charAt(0) + "***" + trimmed.substring(at);
    }

    /**
     * objectKey 缩略展示，仅保留末尾片段。
     *
     * @param objectKey MinIO object key
     * @return 缩略后的 objectKey
     */
    public static String abbreviateObjectKey(String objectKey) {
        if (!StringUtils.hasText(objectKey)) {
            return "";
        }
        String trimmed = objectKey.trim();
        if (trimmed.length() <= 16) {
            return trimmed;
        }
        return "..." + trimmed.substring(trimmed.length() - 16);
    }

    /**
     * 普通字符串安全裁剪，避免日志过长。
     *
     * @param value 原始字符串
     * @return 裁剪后的字符串
     */
    public static String truncate(String value) {
        return truncate(value, DEFAULT_MAX_LENGTH);
    }

    /**
     * 按指定长度裁剪字符串。
     *
     * @param value     原始字符串
     * @param maxLength 最大长度
     * @return 裁剪后的字符串
     */
    public static String truncate(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.length() <= maxLength) {
            return trimmed;
        }
        return trimmed.substring(0, maxLength) + "...";
    }
}
