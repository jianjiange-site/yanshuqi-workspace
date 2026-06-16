package com.dating.user.service.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 敏感日志脱敏工具单元测试。
 */
class SensitiveLogUtilTest {

    /**
     * 手机号应脱敏。
     */
    @Test
    void shouldMaskPhone() {
        assertEquals("138****90", LogMaskUtil.maskPhone("13812345690"));
    }

    /**
     * 邮箱应脱敏。
     */
    @Test
    void shouldMaskEmail() {
        assertEquals("t***@example.com", LogMaskUtil.maskEmail("test@example.com"));
    }

    /**
     * objectKey 应缩略展示。
     */
    @Test
    void shouldAbbreviateObjectKey() {
        String key = "avatar/325259949544443904/202606/u05_avatar_v2.jpg";
        String abbreviated = LogMaskUtil.abbreviateObjectKey(key);
        assertTrue(abbreviated.startsWith("..."));
        assertTrue(abbreviated.length() < key.length());
        assertTrue(key.endsWith(abbreviated.substring(3)));
    }

    /**
     * 长字符串应裁剪。
     */
    @Test
    void shouldTruncateLongString() {
        String value = "a".repeat(100);
        assertTrue(LogMaskUtil.truncate(value, 16).endsWith("..."));
    }

    /**
     * 按凭证类型脱敏身份标识。
     */
    @Test
    void shouldMaskIdentityByType() {
        assertEquals("138****90", SensitiveLogUtil.maskIdentity("PHONE", "13812345690"));
        assertEquals("t***@example.com", SensitiveLogUtil.maskIdentity("EMAIL", "test@example.com"));
    }

    /**
     * URL 识别应生效。
     */
    @Test
    void shouldDetectUrlLikeValue() {
        assertTrue(SensitiveLogUtil.looksLikeUrl("https://cdn.example.com/a.jpg"));
        assertFalse(SensitiveLogUtil.looksLikeUrl("avatar/u1/a.jpg"));
    }
}
