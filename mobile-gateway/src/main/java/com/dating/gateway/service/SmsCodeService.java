package com.dating.gateway.service;

import com.dating.gateway.exception.GatewayBizException;
import com.dating.gateway.exception.GatewayErrorCode;
import com.dating.gateway.security.AuthRedisKeys;
import com.dating.gateway.security.SmsProperties;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * 短信验证码写入 Redis；本阶段不接入真实短信通道。
 */
@Service
public class SmsCodeService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final StringRedisTemplate stringRedisTemplate;
    private final SmsProperties smsProperties;
    private final Environment environment;

    public SmsCodeService(StringRedisTemplate stringRedisTemplate,
                          SmsProperties smsProperties,
                          Environment environment) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.smsProperties = smsProperties;
        this.environment = environment;
    }

    public String sendCode(String rawPhone) {
        String phone = normalizePhone(rawPhone);
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(AuthRedisKeys.smsCooldownKey(phone)))) {
            throw new GatewayBizException(GatewayErrorCode.SMS_COOLDOWN);
        }
        String code = smsProperties.isEnabled() ? randomSixDigitCode() : smsProperties.getMockCode();
        stringRedisTemplate.opsForValue().set(
                AuthRedisKeys.smsCodeKey(phone),
                code,
                smsProperties.getCodeTtlSeconds(),
                TimeUnit.SECONDS);
        stringRedisTemplate.opsForValue().set(
                AuthRedisKeys.smsCooldownKey(phone),
                "1",
                smsProperties.getCooldownSeconds(),
                TimeUnit.SECONDS);
        return isDevOrTestProfile() ? code : null;
    }

    public void verifyAndConsume(String rawPhone, String smsCode) {
        String phone = normalizePhone(rawPhone);
        String cached = stringRedisTemplate.opsForValue().get(AuthRedisKeys.smsCodeKey(phone));
        if (!StringUtils.hasText(cached)) {
            throw new GatewayBizException(GatewayErrorCode.SMS_CODE_EXPIRED);
        }
        if (!cached.equals(smsCode)) {
            throw new GatewayBizException(GatewayErrorCode.SMS_CODE_INVALID);
        }
        stringRedisTemplate.delete(AuthRedisKeys.smsCodeKey(phone));
    }

    private String normalizePhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            throw new GatewayBizException(GatewayErrorCode.INVALID_ARGUMENT, "phone 不能为空");
        }
        String normalized = phone.trim();
        if (!normalized.startsWith("+") || normalized.length() < 8) {
            throw new GatewayBizException(GatewayErrorCode.INVALID_ARGUMENT, "phone 格式非法");
        }
        return normalized;
    }

    private String randomSixDigitCode() {
        int value = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(value);
    }

    private boolean isDevOrTestProfile() {
        String[] profiles = environment.getActiveProfiles();
        if (profiles.length == 0) {
            return true;
        }
        return Arrays.stream(profiles).anyMatch(p -> "dev".equals(p) || "test".equals(p));
    }
}
