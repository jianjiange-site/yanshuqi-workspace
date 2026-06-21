package com.dating.user.service.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserSensitiveLogMaskTest {

  @Test
  void phoneShouldBeMasked() {
    String masked = SensitiveLogUtil.maskIdentity("PHONE", "13912345678");
    assertNotEquals("13912345678", masked);
    assertTrue(masked.contains("***") || masked.length() < 11);
  }

  @Test
  void smsCodeShouldNotAppearInPlainText() {
    assertEquals("***", SensitiveLogUtil.maskSmsCode("123456"));
  }

  @Test
  void idTokenShouldBeTruncated() {
    String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.payload";
    String masked = SensitiveLogUtil.maskToken(token);
    assertTrue(masked.length() < token.length());
  }

  @Test
  void pushTokenShouldBeTruncated() {
    String push = "push-token-abcdefgh-ijklmnop";
    String masked = SensitiveLogUtil.maskToken(push);
    assertTrue(masked.length() < push.length());
  }

  @Test
  void presignedUrlShouldNotBeFullLength() {
    String url = "https://minio.example.com/bucket/object?X-Amz-Signature=verylongsignaturevalue";
    String masked = SensitiveLogUtil.maskPresignedUrl(url);
    assertTrue(masked.length() < url.length());
  }

  @Test
  void identityHashShouldBeShortened() {
    String hash = "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6";
    String masked = SensitiveLogUtil.maskIdentityHash(hash);
    assertTrue(masked.endsWith("..."));
    assertTrue(masked.length() < hash.length());
  }

  @Test
  void deviceIdShouldBeMasked() {
    String deviceId = "device-fingerprint-abcdefgh-12345678";
    String masked = SensitiveLogUtil.maskDeviceId(deviceId);
    assertTrue(masked.length() < deviceId.length());
  }

  @Test
  void safeLogValueShouldMaskSensitiveFieldNames() {
    assertEquals("***", SensitiveLogUtil.safeLogValue("password", "secret"));
    assertEquals("***", SensitiveLogUtil.safeLogValue("smsCode", "654321"));
    assertFalse(SensitiveLogUtil.safeLogValue("idToken", "long-id-token-value").contains("long-id-token-value"));
  }
}
