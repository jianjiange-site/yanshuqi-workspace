package com.dating.user.service.support;

import com.dating.user.constant.IdentityType;
import com.dating.user.exception.UserBizException;
import com.dating.user.exception.UserErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 登录凭证归一化与哈希服务。
 */
@Service
public class IdentityHashService {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{6,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /**
     * 归一化登录凭证，用于哈希与唯一索引查询。
     *
     * @param identityType  凭证类型
     * @param identityValue 凭证原始值
     * @return 归一化后的凭证值
     * @throws UserBizException 当凭证类型或格式非法时
     */
    public String normalize(String identityType, String identityValue) {
        IdentityType type = parseSupportedIdentityType(identityType);
        String trimmed = identityValue == null ? "" : identityValue.trim();
        if (!StringUtils.hasText(trimmed)) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "登录凭证不能为空");
        }
        return switch (type) {
            case PHONE -> normalizePhone(trimmed);
            case EMAIL -> normalizeEmail(trimmed);
            case DEVICE -> normalizeDevice(trimmed);
            default -> throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "不支持的登录凭证类型");
        };
    }

    /**
     * 计算 identity_hash，用于唯一索引与幂等查询。
     *
     * @param identityType      凭证类型
     * @param normalizedValue   归一化后的凭证值
     * @return identity_hash 十六进制字符串
     */
    public String hash(String identityType, String normalizedValue) {
        String payload = identityType.trim().toUpperCase(Locale.ROOT) + ":" + normalizedValue;
        return sha256Hex(payload);
    }

    /**
     * 生成脱敏后的 identity_value，用于入库展示，禁止写入日志明文。
     *
     * @param identityType      凭证类型
     * @param normalizedValue   归一化后的凭证值
     * @return 脱敏后的凭证值
     */
    public String maskForStorage(String identityType, String normalizedValue) {
        IdentityType type = parseSupportedIdentityType(identityType);
        return switch (type) {
            case PHONE -> maskPhone(normalizedValue);
            case EMAIL -> maskEmail(normalizedValue);
            case DEVICE -> maskDevice(normalizedValue);
            default -> "***";
        };
    }

    /**
     * 解析本阶段支持的凭证类型。
     *
     * @param identityType 凭证类型字符串
     * @return 凭证类型枚举
     * @throws UserBizException 当类型非法或不支持时
     */
    public IdentityType parseSupportedIdentityType(String identityType) {
        if (!StringUtils.hasText(identityType)) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "登录凭证类型不能为空");
        }
        try {
            IdentityType type = IdentityType.valueOf(identityType.trim().toUpperCase(Locale.ROOT));
            if (type == IdentityType.GOOGLE || type == IdentityType.APPLE) {
                throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "本阶段不支持第三方 OAuth 注册");
            }
            return type;
        } catch (IllegalArgumentException ex) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "登录凭证类型非法");
        }
    }

    private String normalizePhone(String value) {
        String normalized = value.replace(" ", "").replace("-", "");
        if (!PHONE_PATTERN.matcher(normalized).matches()) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "手机号格式非法");
        }
        return normalized;
    }

    private String normalizeEmail(String value) {
        String normalized = value.toLowerCase(Locale.ROOT);
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "邮箱格式非法");
        }
        return normalized;
    }

    private String normalizeDevice(String value) {
        if (value.length() < 8 || value.length() > 128) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "设备标识格式非法");
        }
        return value;
    }

    private String maskPhone(String normalizedPhone) {
        if (normalizedPhone.length() <= 7) {
            return "***";
        }
        return normalizedPhone.substring(0, 3) + "****" + normalizedPhone.substring(normalizedPhone.length() - 4);
    }

    private String maskEmail(String normalizedEmail) {
        int atIndex = normalizedEmail.indexOf('@');
        if (atIndex <= 1) {
            return "***@" + normalizedEmail.substring(atIndex + 1);
        }
        return normalizedEmail.charAt(0) + "***@" + normalizedEmail.substring(atIndex + 1);
    }

    private String maskDevice(String normalizedDevice) {
        return "device:***" + normalizedDevice.substring(normalizedDevice.length() - 4);
    }

    private String sha256Hex(String payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte value : hash) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new UserBizException(UserErrorCode.INTERNAL_ERROR, "identity 哈希计算失败");
        }
    }

    /**
     * 对推送 token 进行 SHA-256 哈希，用于入库 push_token_hash。
     *
     * @param pushToken 推送 token 明文，禁止写入日志
     * @return push_token_hash 十六进制字符串；pushToken 为空时返回 null
     */
    public String hashPushToken(String pushToken) {
        if (!StringUtils.hasText(pushToken)) {
            return null;
        }
        return sha256Hex("PUSH_TOKEN:" + pushToken.trim());
    }
}
