package com.dating.user.service.impl;

import com.dating.user.constant.AccountStatus;
import com.dating.user.dto.UpdateProfileCommand;
import com.dating.user.dto.UpsertOnboardingCommand;
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
import com.dating.user.service.support.ProfileStatusResolver;
import com.dating.user.service.support.ProfileViewConverter;
import com.dating.user.service.support.SlowCallLogger;
import com.dating.user.vo.UserProfileDetailVO;
import com.dating.user.vo.UserProfileViewVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

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
    private final ProfileStatusResolver profileStatusResolver;
    private final ProfileViewConverter profileViewConverter;
    private final UserCacheInvalidationService userCacheInvalidationService;
    private final SlowCallLogger slowCallLogger;

    public UserProfileServiceImpl(UserManager userManager,
                                    UserProfileManager userProfileManager,
                                    ProfileFieldValidator profileFieldValidator,
                                    ProfileCompletionCalculator profileCompletionCalculator,
                                    ProfileJsonSupport profileJsonSupport,
                                    ProfileStatusResolver profileStatusResolver,
                                    ProfileViewConverter profileViewConverter,
                                    UserCacheInvalidationService userCacheInvalidationService,
                                    SlowCallLogger slowCallLogger) {
        this.userManager = userManager;
        this.userProfileManager = userProfileManager;
        this.profileFieldValidator = profileFieldValidator;
        this.profileCompletionCalculator = profileCompletionCalculator;
        this.profileJsonSupport = profileJsonSupport;
        this.profileStatusResolver = profileStatusResolver;
        this.profileViewConverter = profileViewConverter;
        this.userCacheInvalidationService = userCacheInvalidationService;
        this.slowCallLogger = slowCallLogger;
    }

    @Override
    public UserProfileDetailVO getSelfProfile(Long userId) {
        if (userId == null || userId <= 0) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "用户 ID 非法");
        }
        UserEntity userEntity = userManager.findByUserId(userId);
        if (userEntity == null) {
            throw new UserBizException(UserErrorCode.USER_NOT_FOUND);
        }
        UserProfileEntity profileEntity = userProfileManager.findByUserId(userId);
        if (profileEntity == null) {
            throw new UserBizException(UserErrorCode.PROFILE_NOT_FOUND);
        }
        return buildProfileDetailVO(userEntity, profileEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserProfileDetailVO updateProfile(UpdateProfileCommand command) {
        long startNano = System.nanoTime();
        boolean success = true;
        String errorCode = null;
        Long userId = command == null ? null : command.getUserId();
        try {
            profileFieldValidator.validateAndNormalize(command);
            userId = command.getUserId();

            UserEntity userEntity = loadActiveUserForUpdate(userId);
            UserProfileEntity profileEntity = loadProfile(userId);

            applyProfileUpdates(profileEntity, command);
            finalizeProfileWrite(userEntity, profileEntity);

            log.info("用户资料更新成功, userId={}, profileScore={}, profileStatus={}",
                    userId, profileEntity.getProfileScore(), userEntity.getProfileStatus());
            return buildProfileDetailVO(userEntity, profileEntity);
        } catch (UserBizException ex) {
            success = false;
            errorCode = ex.getErrorCode().getCode();
            throw ex;
        } finally {
            slowCallLogger.logIfSlow("updateProfile", startNano, userId, success, errorCode);
        }
    }

    /**
     * UpsertOnboarding：首次登录后补齐关键资料，返回 Swagger UserProfileVO 视图。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserProfileViewVO upsertOnboarding(UpsertOnboardingCommand command) {
        long startNano = System.nanoTime();
        boolean success = true;
        String errorCode = null;
        Long userId = command == null ? null : command.getUserId();
        try {
            profileFieldValidator.validateAndNormalizeOnboarding(command);
            userId = command.getUserId();

            UserEntity userEntity = loadActiveUserForUpdate(userId);
            UserProfileEntity profileEntity = loadProfile(userId);

            applyOnboardingUpdates(profileEntity, command);
            finalizeProfileWrite(userEntity, profileEntity);

            log.info("Onboarding 资料补齐成功, userId={}, profileScore={}, pending={}",
                    userId, profileEntity.getProfileScore(),
                    profileViewConverter.toView(userEntity, profileEntity).isPending());
            return profileViewConverter.toView(userEntity, profileEntity);
        } catch (UserBizException ex) {
            success = false;
            errorCode = ex.getErrorCode().getCode();
            throw ex;
        } finally {
            slowCallLogger.logIfSlow("upsertOnboarding", startNano, userId, success, errorCode);
        }
    }

    @Override
    public UserProfileViewVO getUserProfileView(Long userId) {
        if (userId == null || userId <= 0) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "用户 ID 非法");
        }
        UserEntity userEntity = userManager.findByUserId(userId);
        if (userEntity == null) {
            throw new UserBizException(UserErrorCode.USER_NOT_FOUND);
        }
        UserProfileEntity profileEntity = userProfileManager.findByUserId(userId);
        if (profileEntity == null) {
            throw new UserBizException(UserErrorCode.PROFILE_NOT_FOUND);
        }
        return profileViewConverter.toView(userEntity, profileEntity);
    }

    private UserEntity loadActiveUserForUpdate(long userId) {
        UserEntity userEntity = userManager.findByUserId(userId);
        if (userEntity == null) {
            throw new UserBizException(UserErrorCode.USER_NOT_FOUND);
        }
        validateAccountStatusForUpdate(userEntity.getAccountStatus());
        return userEntity;
    }

    private UserProfileEntity loadProfile(long userId) {
        UserProfileEntity profileEntity = userProfileManager.findByUserId(userId);
        if (profileEntity == null) {
            throw new UserBizException(UserErrorCode.PROFILE_NOT_FOUND);
        }
        return profileEntity;
    }

    private void finalizeProfileWrite(UserEntity userEntity, UserProfileEntity profileEntity) {
        // profile_score / profile_completed 重新计算
        ProfileCompletionCalculator.CompletionResult completionResult =
                profileCompletionCalculator.calculateFromEntity(profileEntity);
        profileEntity.setProfileScore(completionResult.getProfileScore());
        profileEntity.setProfileCompleted(completionResult.getProfileCompleted());
        userProfileManager.updateProfile(profileEntity);

        String profileStatus = profileStatusResolver.resolve(
                completionResult.getProfileCompleted(), profileEntity.getAvatarKey());
        userManager.updateProfileStatus(userEntity.getUserId(), profileStatus);
        userEntity.setProfileStatus(profileStatus);

        // 删除资料相关 Redis 缓存，失败不回滚主事务
        userCacheInvalidationService.evictProfileCache(userEntity.getUserId());
    }

    /**
     * Onboarding 字段写入：birthday 优先落 birth_date，仅无 birthday 时落 age。
     */
    private void applyOnboardingUpdates(UserProfileEntity profileEntity, UpsertOnboardingCommand command) {
        profileEntity.setNickname(command.getNickname());
        profileEntity.setGender(command.getGender());
        LocalDate birthDate = profileFieldValidator.parseOnboardingBirthday(command);
        if (birthDate != null) {
            profileEntity.setBirthDate(birthDate);
            profileEntity.setAge(null);
        } else if (command.getAge() != null) {
            profileEntity.setAge(command.getAge());
        }
        profileEntity.setHeight(command.getHeight());
        profileEntity.setBio(command.getBio());
        profileEntity.setOccupation(command.getOccupation());
        profileEntity.setEducation(command.getEducation());
        profileEntity.setLocation(command.getLocation());
        if (StringUtils.hasText(command.getDefaultAvatarObjectKey())) {
            profileEntity.setAvatarKey(command.getDefaultAvatarObjectKey());
        }
        if (profileEntity.getRegulationStatus() == null) {
            profileEntity.setRegulationStatus(0);
        }
    }

    private void applyProfileUpdates(UserProfileEntity profileEntity, UpdateProfileCommand command) {
        profileEntity.setNickname(command.getNickname());
        // Swagger 日常更新不传 gender / birthday 时保留原值
        if (command.getGender() != null) {
            profileEntity.setGender(command.getGender());
        }
        if (command.getBirthDate() != null) {
            profileEntity.setBirthDate(command.getBirthDate());
        }
        profileEntity.setCountryCode(command.getCountryCode());
        profileEntity.setCityCode(command.getCityCode());
        profileEntity.setLanguageCodes(profileJsonSupport.toJsonArray(command.getLanguageCodes()));
        profileEntity.setBio(command.getBio());
        profileEntity.setInterests(profileJsonSupport.toJsonArray(command.getInterests()));
        if (command.isAgePresent()) {
            profileEntity.setAge(command.getAge());
        }
        if (command.isHeightPresent()) {
            profileEntity.setHeight(command.getHeight());
        }
        if (command.getOccupation() != null) {
            profileEntity.setOccupation(command.getOccupation());
        }
        if (command.getEducation() != null) {
            profileEntity.setEducation(command.getEducation());
        }
        if (command.getLocation() != null) {
            profileEntity.setLocation(command.getLocation());
        }
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
