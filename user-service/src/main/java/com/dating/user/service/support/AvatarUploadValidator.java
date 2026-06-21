package com.dating.user.service.support;

import com.dating.user.config.AvatarUploadProperties;
import com.dating.user.constant.PhotoType;
import com.dating.user.exception.UserBizException;
import com.dating.user.exception.UserErrorCode;
import com.dating.user.service.storage.ObjectStat;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 头像上传参数校验：ext 白名单、expected_size_bytes 边界。
 */
@Component
public class AvatarUploadValidator {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp");

    private final AvatarUploadProperties avatarUploadProperties;

    private final PhotoObjectKeyValidator photoObjectKeyValidator;

    public AvatarUploadValidator(AvatarUploadProperties avatarUploadProperties,
                                 PhotoObjectKeyValidator photoObjectKeyValidator) {
        this.avatarUploadProperties = avatarUploadProperties;
        this.photoObjectKeyValidator = photoObjectKeyValidator;
    }

    public void validatePresign(Long userId, String ext, Long expectedSizeBytes) {
        if (userId == null || userId <= 0) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "用户 ID 非法");
        }
        validateExpectedSize(expectedSizeBytes);
        normalizeExt(ext);
    }

    public void validateConfirmObjectKey(Long userId, String objectKey) {
        if (userId == null || userId <= 0) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "用户 ID 非法");
        }
        if (!StringUtils.hasText(objectKey)) {
            throw new UserBizException(UserErrorCode.INVALID_AVATAR_OBJECT_KEY, "object key 不能为空");
        }
        String normalized = photoObjectKeyValidator.normalizeObjectKey(objectKey);
        try {
            photoObjectKeyValidator.validate(userId, PhotoType.AVATAR.name(), normalized);
        } catch (UserBizException ex) {
            if (ex.getErrorCode() == UserErrorCode.PHOTO_OBJECT_KEY_INVALID
                    && StringUtils.hasText(ex.getMessage())
                    && ex.getMessage().contains("不属于")) {
                throw new UserBizException(UserErrorCode.AVATAR_OBJECT_NOT_BELONG_TO_USER);
            }
            throw new UserBizException(UserErrorCode.INVALID_AVATAR_OBJECT_KEY, ex.getMessage());
        }
    }

    public void validateStatObject(ObjectStat stat) {
        if (stat == null || stat.getSizeBytes() <= 0) {
            throw new UserBizException(UserErrorCode.AVATAR_OBJECT_NOT_FOUND);
        }
        if (stat.getSizeBytes() > avatarUploadProperties.getMaxSizeBytes()) {
            throw new UserBizException(UserErrorCode.AVATAR_SIZE_EXCEEDED);
        }
        if (StringUtils.hasText(stat.getContentType())
                && !ALLOWED_CONTENT_TYPES.contains(stat.getContentType().toLowerCase(Locale.ROOT))) {
            throw new UserBizException(UserErrorCode.INVALID_AVATAR_OBJECT_KEY, "Content-Type 非法");
        }
    }

    /**
     * ext 转小写并校验白名单，返回不含点后缀。
     */
    public String normalizeExt(String ext) {
        if (!StringUtils.hasText(ext)) {
            throw new UserBizException(UserErrorCode.INVALID_AVATAR_EXT, "ext 不能为空");
        }
        String normalized = ext.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith(".")) {
            normalized = normalized.substring(1);
        }
        Set<String> allowed = Arrays.stream(avatarUploadProperties.getAllowedExt().split(","))
                .map(String::trim)
                .map(s -> s.toLowerCase(Locale.ROOT))
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        if (!allowed.contains(normalized)) {
            throw new UserBizException(UserErrorCode.INVALID_AVATAR_EXT);
        }
        return normalized;
    }

    public String contentTypeForExt(String extWithoutDot) {
        return switch (extWithoutDot.toLowerCase(Locale.ROOT)) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            default -> throw new UserBizException(UserErrorCode.INVALID_AVATAR_EXT);
        };
    }

    private void validateExpectedSize(Long expectedSizeBytes) {
        if (expectedSizeBytes == null || expectedSizeBytes <= 0) {
            throw new UserBizException(UserErrorCode.AVATAR_SIZE_EXCEEDED, "expected_size_bytes 必须大于 0");
        }
        if (expectedSizeBytes > avatarUploadProperties.getMaxSizeBytes()) {
            throw new UserBizException(UserErrorCode.AVATAR_SIZE_EXCEEDED);
        }
    }
}
