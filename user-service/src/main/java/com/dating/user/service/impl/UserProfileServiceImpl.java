package com.dating.user.service.impl;

import com.dating.user.constant.AccountStatus;
import com.dating.user.dto.UpdateProfileCommand;
import com.dating.user.entity.UserEntity;
import com.dating.user.entity.UserProfileEntity;
import com.dating.user.exception.UserBizException;
import com.dating.user.exception.UserErrorCode;
import com.dating.user.manager.UserManager;
import com.dating.user.manager.UserProfileManager;
import com.dating.user.service.UserCacheInvalidationService;
import com.dating.user.service.UserProfileService;
import com.dating.user.service.support.ProfileCompletionCalculator;
import com.dating.user.service.support.ProfileFieldValidator;
import com.dating.user.service.support.ProfileJsonSupport;
import com.dating.user.vo.UserProfileDetailVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 用户资料业务服务实现。
 */
@Service
@Profile("!test")
public class UserProfileServiceImpl implements UserProfileService {

    private static final Logger log = LoggerFactory.getLogger(UserProfileServiceImpl.class);

    private final UserManager userManager;
    private final UserProfileManager userProfileManager;
    private final ProfileFieldValidator profileFieldValidator;
    private final ProfileCompletionCalculator profileCompletionCalculator;
    private final ProfileJsonSupport profileJsonSupport;
    private final UserCacheInvalidationService userCacheInvalidationService;

    /**
     * 构造用户资料业务服务。
     *
     * @param userManager                  用户主表 Manager
     * @param userProfileManager           用户资料 Manager
     * @param profileFieldValidator        资料字段校验器
     * @param profileCompletionCalculator  资料完整度计算器
     * @param profileJsonSupport           资料 JSON 支持
     * @param userCacheInvalidationService 缓存失效服务
     */
    public UserProfileServiceImpl(UserManager userManager,
                                    UserProfileManager userProfileManager,
                                    ProfileFieldValidator profileFieldValidator,
                                    ProfileCompletionCalculator profileCompletionCalculator,
                                    ProfileJsonSupport profileJsonSupport,
                                    UserCacheInvalidationService userCacheInvalidationService) {
        this.userManager = userManager;
        this.userProfileManager = userProfileManager;
        this.profileFieldValidator = profileFieldValidator;
        this.profileCompletionCalculator = profileCompletionCalculator;
        this.profileJsonSupport = profileJsonSupport;
        this.userCacheInvalidationService = userCacheInvalidationService;
    }

    /**
     * 查询本人资料详情。
     *
     * @param userId 用户业务主键
     * @return 用户资料详情 VO
     * @throws UserBizException 当用户或资料不存在时
     */
    @Override
    public UserProfileDetailVO getSelfProfile(Long userId) {
        // 1. 参数校验：userId 不能为空
        if (userId == null || userId <= 0) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "用户 ID 非法");
        }
        // 2. 查询用户主表
        UserEntity userEntity = userManager.findByUserId(userId);
        if (userEntity == null) {
            throw new UserBizException(UserErrorCode.USER_NOT_FOUND);
        }
        // 3. 查询用户资料
        UserProfileEntity profileEntity = userProfileManager.findByUserId(userId);
        if (profileEntity == null) {
            throw new UserBizException(UserErrorCode.PROFILE_NOT_FOUND);
        }
        // 4. 组装返回 VO，不包含敏感字段
        return buildProfileDetailVO(userEntity, profileEntity);
    }

    /**
     * 更新本人基础资料，并计算完整度与 profile_status。
     *
     * @param command 更新资料命令
     * @return 更新后的用户资料详情 VO
     * @throws UserBizException 当用户不存在、账号状态非法或字段校验失败时
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserProfileDetailVO updateProfile(UpdateProfileCommand command) {
        // 1. 参数校验与字段规范化
        profileFieldValidator.validateAndNormalize(command);

        // 2. 查询用户主表并校验账号状态
        UserEntity userEntity = userManager.findByUserId(command.getUserId());
        if (userEntity == null) {
            throw new UserBizException(UserErrorCode.USER_NOT_FOUND);
        }
        validateAccountStatusForUpdate(userEntity.getAccountStatus());

        // 3. 查询资料记录
        UserProfileEntity profileEntity = userProfileManager.findByUserId(command.getUserId());
        if (profileEntity == null) {
            throw new UserBizException(UserErrorCode.PROFILE_NOT_FOUND);
        }

        // 4. 计算 profile_score / profile_completed / profile_status
        ProfileCompletionCalculator.CompletionResult completionResult = profileCompletionCalculator.calculate(command);

        // 5. 更新 user_profiles，保留 avatar_key 不变
        applyProfileUpdates(profileEntity, command, completionResult);
        userProfileManager.updateProfile(profileEntity);

        // 6. 更新 users.profile_status
        userManager.updateProfileStatus(command.getUserId(), completionResult.getProfileStatus());
        userEntity.setProfileStatus(completionResult.getProfileStatus());

        // 7. 删除资料相关 Redis 缓存，失败不回滚主事务
        userCacheInvalidationService.evictProfileCache(command.getUserId());

        log.info("用户资料更新成功, userId={}, profileScore={}", command.getUserId(), completionResult.getProfileScore());
        return buildProfileDetailVO(userEntity, profileEntity);
    }

    private void validateAccountStatusForUpdate(String accountStatus) {
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

    private void applyProfileUpdates(UserProfileEntity profileEntity,
                                     UpdateProfileCommand command,
                                     ProfileCompletionCalculator.CompletionResult completionResult) {
        profileEntity.setNickname(command.getNickname());
        profileEntity.setGender(command.getGender());
        profileEntity.setBirthDate(command.getBirthDate());
        profileEntity.setCountryCode(command.getCountryCode());
        profileEntity.setCityCode(command.getCityCode());
        profileEntity.setLanguageCodes(profileJsonSupport.toJsonArray(command.getLanguageCodes()));
        profileEntity.setBio(command.getBio());
        profileEntity.setInterests(profileJsonSupport.toJsonArray(command.getInterests()));
        profileEntity.setProfileScore(completionResult.getProfileScore());
        profileEntity.setProfileCompleted(completionResult.getProfileCompleted());
    }

    private UserProfileDetailVO buildProfileDetailVO(UserEntity userEntity, UserProfileEntity profileEntity) {
        UserProfileDetailVO vo = new UserProfileDetailVO();
        vo.setUserId(userEntity.getUserId());
        vo.setUserType(userEntity.getUserType());
        vo.setAccountStatus(userEntity.getAccountStatus());
        vo.setProfileStatus(userEntity.getProfileStatus());
        vo.setNickname(profileEntity.getNickname());
        vo.setGender(profileEntity.getGender());
        vo.setBirthDate(profileEntity.getBirthDate());
        vo.setCountryCode(profileEntity.getCountryCode());
        vo.setCityCode(profileEntity.getCityCode());
        vo.setLanguageCodes(profileJsonSupport.fromJsonArray(profileEntity.getLanguageCodes()));
        vo.setBio(profileEntity.getBio());
        vo.setAvatarKey(profileEntity.getAvatarKey());
        vo.setInterests(profileJsonSupport.fromJsonArray(profileEntity.getInterests()));
        vo.setProfileScore(profileEntity.getProfileScore());
        vo.setProfileCompleted(profileEntity.getProfileCompleted());
        vo.setCreatedAt(profileEntity.getCreatedAt());
        vo.setUpdatedAt(profileEntity.getUpdatedAt());
        return vo;
    }
}
