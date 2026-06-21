package com.dating.user.service.impl;

import com.dating.user.config.AvatarUploadProperties;
import com.dating.user.constant.AccountStatus;
import com.dating.user.constant.PhotoType;
import com.dating.user.dto.BindPhotoCommand;
import com.dating.user.dto.ConfirmAvatarUploadCommand;
import com.dating.user.dto.PresignAvatarUploadCommand;
import com.dating.user.entity.UserEntity;
import com.dating.user.exception.UserBizException;
import com.dating.user.exception.UserErrorCode;
import com.dating.user.manager.UserManager;
import com.dating.user.service.UserAvatarService;
import com.dating.user.service.UserPhotoService;
import com.dating.user.service.storage.ObjectStorageService;
import com.dating.user.service.storage.ObjectStat;
import com.dating.user.service.storage.PresignPutResult;
import com.dating.user.service.support.AvatarObjectKeyGenerator;
import com.dating.user.service.support.AvatarUploadValidator;
import com.dating.user.service.support.AvatarViewConverter;
import com.dating.user.service.support.LogMaskUtil;
import com.dating.user.service.support.SlowCallLogger;
import com.dating.user.vo.AvatarViewVO;
import com.dating.user.vo.PresignAvatarUploadResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 用户头像上传业务实现。
 */
@Service
@Profile("!test")
public class UserAvatarServiceImpl implements UserAvatarService {

    private static final Logger log = LoggerFactory.getLogger(UserAvatarServiceImpl.class);

    private final UserManager userManager;
    private final AvatarUploadValidator avatarUploadValidator;
    private final AvatarObjectKeyGenerator avatarObjectKeyGenerator;
    private final AvatarUploadProperties avatarUploadProperties;
    private final ObjectStorageService objectStorageService;
    private final UserPhotoService userPhotoService;
    private final AvatarViewConverter avatarViewConverter;
    private final SlowCallLogger slowCallLogger;

    public UserAvatarServiceImpl(UserManager userManager,
                                 AvatarUploadValidator avatarUploadValidator,
                                 AvatarObjectKeyGenerator avatarObjectKeyGenerator,
                                 AvatarUploadProperties avatarUploadProperties,
                                 ObjectStorageService objectStorageService,
                                 UserPhotoService userPhotoService,
                                 AvatarViewConverter avatarViewConverter,
                                 SlowCallLogger slowCallLogger) {
        this.userManager = userManager;
        this.avatarUploadValidator = avatarUploadValidator;
        this.avatarObjectKeyGenerator = avatarObjectKeyGenerator;
        this.avatarUploadProperties = avatarUploadProperties;
        this.objectStorageService = objectStorageService;
        this.userPhotoService = userPhotoService;
        this.avatarViewConverter = avatarViewConverter;
        this.slowCallLogger = slowCallLogger;
    }

    @Override
    public PresignAvatarUploadResult presignAvatarUpload(PresignAvatarUploadCommand command) {
        long startNano = System.nanoTime();
        boolean success = true;
        String errorCode = null;
        Long userId = command == null ? null : command.getUserId();
        try {
            if (command == null) {
                throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "请求不能为空");
            }
            avatarUploadValidator.validatePresign(command.getUserId(), command.getExt(), command.getExpectedSizeBytes());
            userId = command.getUserId();
            loadActiveUser(userId);

            String ext = avatarUploadValidator.normalizeExt(command.getExt());
            // 服务端生成 objectKey，不允许客户端自定义完整路径
            String objectKey = avatarObjectKeyGenerator.generate(userId, ext);
            String contentType = avatarUploadValidator.contentTypeForExt(ext);

            // presign 只签发 URL，不写 user_photos、不更新 avatar_key、不删缓存
            PresignPutResult presign = objectStorageService.presignPutObject(
                    objectKey,
                    contentType,
                    avatarUploadProperties.getPresignExpireSeconds(),
                    command.getExpectedSizeBytes());

            log.info("头像 presign 成功, userId={}, objectKey={}",
                    userId, LogMaskUtil.abbreviateObjectKey(objectKey));

            PresignAvatarUploadResult result = new PresignAvatarUploadResult();
            result.setPresignedUrl(presign.getPresignedUrl());
            result.setObjectKey(objectKey);
            result.setExpiresAtMs(presign.getExpiresAtMs());
            return result;
        } catch (UserBizException ex) {
            success = false;
            errorCode = ex.getErrorCode().getCode();
            throw ex;
        } finally {
            slowCallLogger.logIfSlow("presignAvatarUpload", startNano, userId, success, errorCode);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AvatarViewVO confirmAvatarUpload(ConfirmAvatarUploadCommand command) {
        long startNano = System.nanoTime();
        boolean success = true;
        String errorCode = null;
        Long userId = command == null ? null : command.getUserId();
        try {
            if (command == null || command.getUserId() == null || command.getUserId() <= 0) {
                throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "用户 ID 非法");
            }
            userId = command.getUserId();
            loadActiveUser(userId);

            String objectKey = command.getObjectKey();
            // objectKey 归属与前缀校验，防止路径穿越与跨用户引用
            avatarUploadValidator.validateConfirmObjectKey(userId, objectKey);
            objectKey = objectKey.trim();

            ObjectStat stat = objectStorageService.statObject(objectKey);
            avatarUploadValidator.validateStatObject(stat);

            // confirm 才落库：复用 BindUserPhoto 写入 user_photos 并更新 avatar_key、删缓存
            BindPhotoCommand bindCommand = new BindPhotoCommand();
            bindCommand.setUserId(userId);
            bindCommand.setPhotoType(PhotoType.AVATAR.name());
            bindCommand.setObjectKey(objectKey);
            bindCommand.setSortOrder(0);
            try {
                userPhotoService.bindUserPhoto(bindCommand);
            } catch (UserBizException ex) {
                throw new UserBizException(UserErrorCode.AVATAR_CONFIRM_FAILED, ex.getMessage());
            }

            log.info("头像 confirm 成功, userId={}, objectKey={}",
                    userId, LogMaskUtil.abbreviateObjectKey(objectKey));

            return avatarViewConverter.fromObjectKey(objectKey, stat.getWidth(), stat.getHeight());
        } catch (UserBizException ex) {
            success = false;
            errorCode = ex.getErrorCode().getCode();
            throw ex;
        } finally {
            slowCallLogger.logIfSlow("confirmAvatarUpload", startNano, userId, success, errorCode);
        }
    }

    private UserEntity loadActiveUser(long userId) {
        UserEntity userEntity = userManager.findByUserId(userId);
        if (userEntity == null) {
            throw new UserBizException(UserErrorCode.USER_NOT_FOUND);
        }
        validateAccountStatus(userEntity.getAccountStatus());
        return userEntity;
    }

    private void validateAccountStatus(String accountStatus) {
        if (!StringUtils.hasText(accountStatus)) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "账号状态非法");
        }
        AccountStatus status;
        try {
            status = AccountStatus.valueOf(accountStatus.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "账号状态非法");
        }
        switch (status) {
            case ACTIVE -> {
                return;
            }
            case DISABLED -> throw new UserBizException(UserErrorCode.USER_DISABLED);
            case BANNED -> throw new UserBizException(UserErrorCode.USER_BANNED);
            case DELETED -> throw new UserBizException(UserErrorCode.USER_DELETED);
            default -> throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "账号状态非法");
        }
    }
}
