package com.dating.user.service.support;

import com.dating.user.constant.DevicePlatform;
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
        IdentityType type;
        try {
            type = parseSupportedIdentityType(identityType);
        } catch (UserBizException ex) {
            type = parseLoginIdentityType(identityType);
        }
        return switch (type) {
            case PHONE -> maskPhone(normalizedValue);
            case EMAIL -> maskEmail(normalizedValue);
            case DEVICE -> maskDevice(normalizedValue);
            case GOOGLE, APPLE, FACEBOOK -> type.name().toLowerCase(Locale.ROOT) + ":***";
            default -> "***";
        };
    }

    /**
     * 归一化设备匿名登录身份：platform + deviceId 组合作为唯一身份。
     *
     * @param platform 设备平台
     * @param deviceId 设备标识，禁止明文入库或打印
     * @return 归一化后的设备身份
     * @throws UserBizException 当 platform 或 deviceId 非法时
     */
    public String normalizeDeviceLoginIdentity(String platform, String deviceId) {
        String normalizedPlatform = parsePlatform(platform).name();
        String normalizedDeviceId = normalizeDeviceId(deviceId);
        return normalizedPlatform + ":" + normalizedDeviceId;
    }

    /**
     * 归一化手机号登录身份。
     *
     * @param phone 手机号明文，禁止写入日志
     * @return 归一化后的手机号
     * @throws UserBizException 当手机号非法时
     */
    public String normalizePhoneLoginIdentity(String phone) {
        if (!StringUtils.hasText(phone)) {
            throw new UserBizException(UserErrorCode.INVALID_PHONE);
        }
        try {
            return normalizePhone(phone.trim());
        } catch (UserBizException ex) {
            if (ex.getErrorCode() == UserErrorCode.USER_REQUEST_INVALID) {
                throw new UserBizException(UserErrorCode.INVALID_PHONE);
            }
            throw ex;
        }
    }

    /**
     * 归一化三方登录身份：对 idToken 做 SHA-256，禁止明文入库或打印。
     *
     * @param idToken 三方 idToken 明文，禁止写入日志
     * @return idToken 哈希十六进制字符串
     * @throws UserBizException 当 idToken 为空时
     */
    public String normalizeThirdPartyIdentity(String idToken) {
        if (!StringUtils.hasText(idToken)) {
            throw new UserBizException(UserErrorCode.INVALID_THIRD_PARTY_IDENTITY);
        }
        return sha256Hex("OAUTH_ID_TOKEN:" + idToken.trim());
    }

    /**
     * 解析登录来源凭证类型，支持 DEVICE / PHONE / GOOGLE / APPLE / FACEBOOK。
     *
     * @param identityType 凭证类型字符串
     * @return 凭证类型枚举
     * @throws UserBizException 当类型非法时
     */
    public IdentityType parseLoginIdentityType(String identityType) {
        if (!StringUtils.hasText(identityType)) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "登录凭证类型不能为空");
        }
        try {
            return IdentityType.valueOf(identityType.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "登录凭证类型非法");
        }
    }

    /**
     * 解析设备平台。
     *
     * @param platform 平台字符串
     * @return 设备平台枚举
     * @throws UserBizException 当平台非法时
     */
    public DevicePlatform parsePlatform(String platform) {
        if (!StringUtils.hasText(platform)) {
            throw new UserBizException(UserErrorCode.INVALID_PLATFORM);
        }
        try {
            return DevicePlatform.valueOf(platform.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new UserBizException(UserErrorCode.INVALID_PLATFORM);
        }
    }

    /**
     * 校验并归一化 deviceId，用于设备匿名登录与 user_devices upsert。
     *
     * @param deviceId 设备标识
     * @return 归一化后的 deviceId
     * @throws UserBizException 当 deviceId 非法时
     */
    public String normalizeDeviceId(String deviceId) {
        if (!StringUtils.hasText(deviceId)) {
            throw new UserBizException(UserErrorCode.INVALID_DEVICE_ID);
        }
        String normalized = deviceId.trim();
        if (normalized.length() < 8 || normalized.length() > 128) {
            throw new UserBizException(UserErrorCode.INVALID_DEVICE_ID);
        }
        return normalized;
    }

    /**
     * 对 deviceId 做脱敏，用于日志输出。
     *
     * @param deviceId 设备标识
     * @return 脱敏后的 deviceId
     */
    public String maskDeviceIdForLog(String deviceId) {
        if (!StringUtils.hasText(deviceId)) {
            return "***";
        }
        String normalized = deviceId.trim();
        if (normalized.length() <= 4) {
            return "device:***";
        }
        return "device:***" + normalized.substring(normalized.length() - 4);
    }

    /**
     * 解析 Register 阶段支持的凭证类型。
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
            if (type == IdentityType.GOOGLE || type == IdentityType.APPLE || type == IdentityType.FACEBOOK) {
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
