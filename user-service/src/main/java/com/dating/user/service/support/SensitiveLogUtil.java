package com.dating.user.service.support;

import org.springframework.util.StringUtils;

/**
 * 敏感日志字段处理工具，统一脱敏规则。
 */
public final class SensitiveLogUtil {

    private SensitiveLogUtil() {
    }

    /**
     * 按凭证类型脱敏身份标识。
     *
     * @param identityType  凭证类型
     * @param identityValue 凭证值
     * @return 脱敏后的凭证展示
     */
    public static String maskIdentity(String identityType, String identityValue) {
        if (!StringUtils.hasText(identityValue)) {
            return "";
        }
        if (!StringUtils.hasText(identityType)) {
            return LogMaskUtil.truncate(identityValue, 8);
        }
        return switch (identityType.trim().toUpperCase()) {
            case "PHONE" -> LogMaskUtil.maskPhone(identityValue);
            case "EMAIL" -> LogMaskUtil.maskEmail(identityValue);
            default -> LogMaskUtil.truncate(identityValue, 8);
        };
    }

    /**
     * 缩略 objectKey 用于日志。
     *
     * @param objectKey object key
     * @return 缩略展示
     */
    public static String safeObjectKey(String objectKey) {
        return LogMaskUtil.abbreviateObjectKey(objectKey);
    }

    /**
     * 判断文本是否疑似完整 URL。
     *
     * @param value 待检查文本
     * @return true 表示疑似完整 URL
     */
    public static boolean looksLikeUrl(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        String lower = value.trim().toLowerCase();
        return lower.startsWith("http://") || lower.startsWith("https://");
    }
}
