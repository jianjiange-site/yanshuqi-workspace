package com.dating.user.service.impl;

import com.dating.user.constant.AccountStatus;
import com.dating.user.constant.PhotoReviewStatus;
import com.dating.user.constant.PhotoType;
import com.dating.user.dto.BindPhotoCommand;
import com.dating.user.dto.ListUserPhotosQuery;
import com.dating.user.entity.UserEntity;
import com.dating.user.entity.UserPhotoEntity;
import com.dating.user.entity.UserProfileEntity;
import com.dating.user.exception.UserBizException;
import com.dating.user.exception.UserErrorCode;
import com.dating.user.manager.UserManager;
import com.dating.user.manager.UserPhotoManager;
import com.dating.user.manager.UserProfileManager;
import com.dating.user.service.UserCacheInvalidationService;
import com.dating.user.service.UserPhotoService;
import com.dating.user.service.support.BusinessIdGenerator;
import com.dating.user.service.support.PhotoObjectKeyValidator;
import com.dating.user.service.support.ProfileStatusResolver;
import com.dating.user.vo.BindPhotoResult;
import com.dating.user.vo.UserPhotoVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 用户照片业务服务实现。
 */
@Service
@Profile("!test")
public class UserPhotoServiceImpl implements UserPhotoService {

    private static final Logger log = LoggerFactory.getLogger(UserPhotoServiceImpl.class);

    private static final int MAX_ALBUM_PHOTOS = 20;

    private final UserManager userManager;
    private final UserProfileManager userProfileManager;
    private final UserPhotoManager userPhotoManager;
    private final PhotoObjectKeyValidator photoObjectKeyValidator;
    private final ProfileStatusResolver profileStatusResolver;
    private final BusinessIdGenerator businessIdGenerator;
    private final UserCacheInvalidationService userCacheInvalidationService;

    /**
     * 构造用户照片业务服务。
     *
     * @param userManager                  用户主表 Manager
     * @param userProfileManager           用户资料 Manager
     * @param userPhotoManager             用户照片 Manager
     * @param photoObjectKeyValidator      object key 校验器
     * @param profileStatusResolver        资料状态解析器
     * @param businessIdGenerator          业务主键生成器
     * @param userCacheInvalidationService 缓存失效服务
     */
    public UserPhotoServiceImpl(UserManager userManager,
                                UserProfileManager userProfileManager,
                                UserPhotoManager userPhotoManager,
                                PhotoObjectKeyValidator photoObjectKeyValidator,
                                ProfileStatusResolver profileStatusResolver,
                                BusinessIdGenerator businessIdGenerator,
                                UserCacheInvalidationService userCacheInvalidationService) {
        this.userManager = userManager;
        this.userProfileManager = userProfileManager;
        this.userPhotoManager = userPhotoManager;
        this.photoObjectKeyValidator = photoObjectKeyValidator;
        this.profileStatusResolver = profileStatusResolver;
        this.businessIdGenerator = businessIdGenerator;
        this.userCacheInvalidationService = userCacheInvalidationService;
    }

    /**
     * 绑定头像或相册 object key。
     *
     * @param command 绑定命令
     * @return 绑定结果
     * @throws UserBizException 当参数非法、用户状态不允许或 object key 非法时
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BindPhotoResult bindUserPhoto(BindPhotoCommand command) {
        // 1. 参数校验
        validateBindCommand(command);
        Long userId = command.getUserId();
        PhotoType photoType = parsePhotoType(command.getPhotoType());
        String objectKey = photoObjectKeyValidator.normalizeObjectKey(command.getObjectKey());
        int sortOrder = command.getSortOrder() == null ? 0 : command.getSortOrder();

        // 2. 查询用户并校验账号状态
        UserEntity userEntity = userManager.findByUserId(userId);
        if (userEntity == null) {
            throw new UserBizException(UserErrorCode.USER_NOT_FOUND);
        }
        validateAccountStatus(userEntity.getAccountStatus());

        // 3. 查询资料并校验 object key
        UserProfileEntity profileEntity = userProfileManager.findByUserId(userId);
        if (profileEntity == null) {
            throw new UserBizException(UserErrorCode.PROFILE_NOT_FOUND);
        }
        photoObjectKeyValidator.validate(userId, photoType.name(), objectKey);

        try {
            // 4. 幂等检查：同一 user_id + object_key 不重复创建
            UserPhotoEntity existing = userPhotoManager.findByUserIdAndObjectKey(userId, objectKey);
            UserPhotoEntity photoEntity;

            if (photoType == PhotoType.AVATAR) {
                // 5. 头像：禁用当前启用头像，创建或复用记录，更新 avatar_key 与 profile_status
                userPhotoManager.disableCurrentAvatar(userId);
                photoEntity = resolveOrCreatePhoto(existing, userId, photoType, objectKey, sortOrder, 1);
                userProfileManager.updateAvatarKey(userId, objectKey);
                profileEntity.setAvatarKey(objectKey);
            } else {
                // 6. 相册：检查数量上限，创建或复用记录，不更新 avatar_key
                if (existing == null) {
                    long albumCount = userPhotoManager.countEnabledByUserIdAndType(userId, PhotoType.ALBUM.name());
                    if (albumCount >= MAX_ALBUM_PHOTOS) {
                        throw new UserBizException(UserErrorCode.PHOTO_LIMIT_EXCEEDED);
                    }
                }
                photoEntity = resolveOrCreatePhoto(existing, userId, photoType, objectKey, sortOrder, 1);
            }

            // 7. 计算并更新 profile_status
            int profileCompleted = profileEntity.getProfileCompleted() == null ? 0 : profileEntity.getProfileCompleted();
            String profileStatus = profileStatusResolver.resolve(profileCompleted, profileEntity.getAvatarKey());
            userManager.updateProfileStatus(userId, profileStatus);
            userEntity.setProfileStatus(profileStatus);

            // 8. 删除资料相关 Redis 缓存，失败不回滚主事务
            userCacheInvalidationService.evictProfileCache(userId);

            log.info("用户照片绑定成功, userId={}, photoType={}, photoId={}", userId, photoType.name(), photoEntity.getPhotoId());

            BindPhotoResult result = new BindPhotoResult();
            result.setPhoto(toUserPhotoVO(photoEntity));
            result.setAvatarKey(profileEntity.getAvatarKey());
            result.setProfileStatus(profileStatus);
            return result;
        } catch (DuplicateKeyException ex) {
            log.warn("照片绑定唯一索引冲突, userId={}", userId);
            throw new UserBizException(UserErrorCode.USER_CONCURRENT_CONFLICT);
        }
    }

    /**
     * 查询用户照片列表。
     *
     * @param query 查询条件
     * @return 照片 VO 列表
     * @throws UserBizException 当参数非法或用户不存在时
     */
    @Override
    public List<UserPhotoVO> listUserPhotos(ListUserPhotosQuery query) {
        // 1. 参数校验
        if (query == null || query.getUserId() == null || query.getUserId() <= 0) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "用户 ID 非法");
        }
        if (StringUtils.hasText(query.getPhotoType())) {
            parsePhotoType(query.getPhotoType());
        }

        // 2. 查询用户
        UserEntity userEntity = userManager.findByUserId(query.getUserId());
        if (userEntity == null) {
            throw new UserBizException(UserErrorCode.USER_NOT_FOUND);
        }

        // 3. 查询照片列表并转换为 VO
        String photoTypeFilter = StringUtils.hasText(query.getPhotoType())
                ? query.getPhotoType().trim().toUpperCase(Locale.ROOT) : null;
        List<UserPhotoEntity> entities = userPhotoManager.listByUserIdAndType(
                query.getUserId(), photoTypeFilter, query.isIncludeDisabled());
        return entities.stream().map(this::toUserPhotoVO).collect(Collectors.toList());
    }

    private void validateBindCommand(BindPhotoCommand command) {
        if (command == null) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "绑定命令不能为空");
        }
        if (command.getUserId() == null || command.getUserId() <= 0) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "用户 ID 非法");
        }
        if (!StringUtils.hasText(command.getPhotoType())) {
            throw new UserBizException(UserErrorCode.PHOTO_TYPE_INVALID, "照片类型不能为空");
        }
        if (command.getSortOrder() != null && command.getSortOrder() < 0) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "排序值不能为负数");
        }
    }

    private PhotoType parsePhotoType(String photoType) {
        try {
            return PhotoType.valueOf(photoType.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new UserBizException(UserErrorCode.PHOTO_TYPE_INVALID, "照片类型非法");
        }
    }

    private void validateAccountStatus(String accountStatus) {
        if (!StringUtils.hasText(accountStatus)) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "账号状态非法");
        }
        AccountStatus status;
        try {
            status = AccountStatus.valueOf(accountStatus.trim().toUpperCase(Locale.ROOT));
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

    private UserPhotoEntity resolveOrCreatePhoto(UserPhotoEntity existing,
                                                 long userId,
                                                 PhotoType photoType,
                                                 String objectKey,
                                                 int sortOrder,
                                                 int enabled) {
        if (existing != null) {
            userPhotoManager.updatePhotoEnabledOrSort(existing, enabled, sortOrder);
            existing.setEnabled(enabled);
            existing.setSortOrder(sortOrder);
            return existing;
        }
        long photoId = businessIdGenerator.nextId();
        userPhotoManager.createPhoto(
                photoId,
                userId,
                photoType.name(),
                objectKey,
                sortOrder,
                PhotoReviewStatus.PENDING.name(),
                enabled);
        UserPhotoEntity created = userPhotoManager.findByUserIdAndObjectKey(userId, objectKey);
        if (created == null) {
            throw new UserBizException(UserErrorCode.INTERNAL_ERROR, "照片写入后查询失败");
        }
        return created;
    }

    private UserPhotoVO toUserPhotoVO(UserPhotoEntity entity) {
        UserPhotoVO vo = new UserPhotoVO();
        vo.setPhotoId(entity.getPhotoId());
        vo.setUserId(entity.getUserId());
        vo.setPhotoType(entity.getPhotoType());
        vo.setObjectKey(entity.getObjectKey());
        vo.setSortOrder(entity.getSortOrder());
        vo.setReviewStatus(entity.getReviewStatus());
        vo.setEnabled(entity.getEnabled());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }
}
