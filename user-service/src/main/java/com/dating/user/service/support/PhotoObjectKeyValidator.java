package com.dating.user.service.support;

import com.dating.user.constant.PhotoType;
import com.dating.user.exception.UserBizException;
import com.dating.user.exception.UserErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 照片 object key 校验器，校验归属与格式，拒绝完整 URL 与路径穿越。
 */
@Component
public class PhotoObjectKeyValidator {

    private static final int MAX_OBJECT_KEY_LENGTH = 512;

    private static final Pattern HTTP_URL_PREFIX = Pattern.compile("^https?://", Pattern.CASE_INSENSITIVE);

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp");

    /**
     * 校验 object key 是否合法且属于指定用户与照片类型。
     *
     * @param userId    用户业务主键
     * @param photoType 照片类型
     * @param objectKey MinIO object key
     * @throws UserBizException 当 object key 非法时
     */
    public void validate(Long userId, String photoType, String objectKey) {
        if (userId == null || userId <= 0) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "用户 ID 非法");
        }
        PhotoType type = parsePhotoType(photoType);
        String normalizedKey = normalizeObjectKey(objectKey);
        validateCommonRules(normalizedKey);
        validateExtension(normalizedKey);
        validateOwnership(userId, type, normalizedKey);
    }

    /**
     * 规范化 object key，去除首尾空白。
     *
     * @param objectKey 原始 object key
     * @return 规范化后的 object key
     */
    public String normalizeObjectKey(String objectKey) {
        if (!StringUtils.hasText(objectKey)) {
            throw new UserBizException(UserErrorCode.PHOTO_OBJECT_KEY_INVALID, "object key 不能为空");
        }
        return objectKey.trim();
    }

    private PhotoType parsePhotoType(String photoType) {
        if (!StringUtils.hasText(photoType)) {
            throw new UserBizException(UserErrorCode.PHOTO_TYPE_INVALID, "照片类型不能为空");
        }
        try {
            return PhotoType.valueOf(photoType.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new UserBizException(UserErrorCode.PHOTO_TYPE_INVALID, "照片类型非法");
        }
    }

    private void validateCommonRules(String objectKey) {
        if (objectKey.length() > MAX_OBJECT_KEY_LENGTH) {
            throw new UserBizException(UserErrorCode.PHOTO_OBJECT_KEY_INVALID, "object key 长度超限");
        }
        if (HTTP_URL_PREFIX.matcher(objectKey).find()) {
            throw new UserBizException(UserErrorCode.PHOTO_OBJECT_KEY_INVALID, "object key 不能是完整 URL");
        }
        if (objectKey.startsWith("/")) {
            throw new UserBizException(UserErrorCode.PHOTO_OBJECT_KEY_INVALID, "object key 不能以 / 开头");
        }
        if (objectKey.contains("..")) {
            throw new UserBizException(UserErrorCode.PHOTO_OBJECT_KEY_INVALID, "object key 不能包含路径穿越");
        }
        if (objectKey.contains("//")) {
            throw new UserBizException(UserErrorCode.PHOTO_OBJECT_KEY_INVALID, "object key 不能包含连续 //");
        }
    }

    private void validateExtension(String objectKey) {
        String lower = objectKey.toLowerCase(Locale.ROOT);
        boolean matched = ALLOWED_EXTENSIONS.stream().anyMatch(lower::endsWith);
        if (!matched) {
            throw new UserBizException(UserErrorCode.PHOTO_OBJECT_KEY_INVALID, "object key 后缀非法");
        }
    }

    private void validateOwnership(Long userId, PhotoType photoType, String objectKey) {
        String expectedPrefix = switch (photoType) {
            case AVATAR -> "avatar/" + userId + "/";
            case ALBUM -> "album/" + userId + "/";
        };
        if (!objectKey.startsWith(expectedPrefix)) {
            throw new UserBizException(UserErrorCode.PHOTO_OBJECT_KEY_INVALID, "object key 不属于当前用户");
        }
    }
}
