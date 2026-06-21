package com.dating.user.service.support;

import org.springframework.util.StringUtils;

/**
 * 敏感日志字段处理工具，统一脱敏规则。
 * 禁止明文输出：phone、smsCode、idToken、pushToken、presignedUrl、password、identityHash 等。
 */
public final class SensitiveLogUtil {

    private static final String MASKED = "***";

    private SensitiveLogUtil() {
    }

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

    public static String safeObjectKey(String objectKey) {
        return LogMaskUtil.abbreviateObjectKey(objectKey);
    }

    /**
     * 短信验证码禁止写入日志，统一返回占位符。
     */
    public static String maskSmsCode(String smsCode) {
        return StringUtils.hasText(smsCode) ? MASKED : "";
    }

    /**
     * Token 类字段（access/refresh/id/push）仅保留前缀片段。
     */
    public static String maskToken(String token) {
        if (!StringUtils.hasText(token)) {
            return "";
        }
        return LogMaskUtil.truncate(token.trim(), 8);
    }

    /**
     * presignedUrl 禁止完整打印，仅输出协议与路径前缀。
     */
    public static String maskPresignedUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return "";
        }
        String trimmed = url.trim();
        if (trimmed.length() <= 32) {
            return LogMaskUtil.truncate(trimmed, 16);
        }
        return LogMaskUtil.truncate(trimmed, 24);
    }

    /**
     * identityHash 仅输出短 hash，避免泄露可逆信息。
     */
    public static String maskIdentityHash(String hash) {
        if (!StringUtils.hasText(hash)) {
            return "";
        }
        String trimmed = hash.trim();
        if (trimmed.length() <= 12) {
            return trimmed;
        }
        return trimmed.substring(0, 8) + "...";
    }

    /**
     * deviceId 缩略展示，避免完整设备指纹泄露。
     */
    public static String maskDeviceId(String deviceId) {
        if (!StringUtils.hasText(deviceId)) {
            return "";
        }
        return LogMaskUtil.truncate(deviceId.trim(), 12);
    }

    public static boolean looksLikeUrl(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        String lower = value.trim().toLowerCase();
        return lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("mock://");
    }

    /**
     * 按字段名选择脱敏策略，供统一日志入口使用。
     */
    public static String safeLogValue(String fieldName, String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        if (!StringUtils.hasText(fieldName)) {
            return LogMaskUtil.truncate(value, 16);
        }
        String name = fieldName.trim().toLowerCase();
        if (name.contains("phone")) {
            return LogMaskUtil.maskPhone(value);
        }
        if (name.contains("sms") && name.contains("code")) {
            return maskSmsCode(value);
        }
        if (name.contains("smscode")) {
            return maskSmsCode(value);
        }
        if (name.contains("idtoken") || name.contains("id_token")) {
            return maskToken(value);
        }
        if (name.contains("pushtoken") || name.contains("push_token")) {
            return maskToken(value);
        }
        if (name.contains("accesstoken") || name.contains("access_token")) {
            return maskToken(value);
        }
        if (name.contains("refreshtoken") || name.contains("refresh_token")) {
            return maskToken(value);
        }
        if (name.contains("presigned") || name.contains("presign")) {
            return maskPresignedUrl(value);
        }
        if (name.contains("password")) {
            return MASKED;
        }
        if (name.contains("identityhash") || name.contains("identity_hash")) {
            return maskIdentityHash(value);
        }
        if (name.contains("deviceid") || name.contains("device_id") || name.contains("device_fingerprint")) {
            return maskDeviceId(value);
        }
        if (looksLikeUrl(value)) {
            return maskPresignedUrl(value);
        }
        return LogMaskUtil.truncate(value, 32);
    }
}
