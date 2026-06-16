package com.dating.user.service.impl;

import com.dating.user.constant.AccountStatus;
import com.dating.user.constant.ProfileStatus;
import com.dating.user.constant.RegisterSource;
import com.dating.user.constant.UserType;
import com.dating.user.dto.RegisterCommand;
import com.dating.user.entity.UserAuthIdentityEntity;
import com.dating.user.entity.UserEntity;
import com.dating.user.exception.UserBizException;
import com.dating.user.exception.UserErrorCode;
import com.dating.user.manager.UserAuthIdentityManager;
import com.dating.user.manager.UserDeviceManager;
import com.dating.user.manager.UserManager;
import com.dating.user.manager.UserProfileManager;
import com.dating.user.manager.UserSettingsManager;
import com.dating.user.service.UserAuthService;
import com.dating.user.service.support.BusinessIdGenerator;
import com.dating.user.service.support.IdentityHashService;
import com.dating.user.service.support.PasswordHashService;
import com.dating.user.vo.RegisterResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * 用户认证业务服务实现。
 */
@Service
@Profile("!test")
public class UserAuthServiceImpl implements UserAuthService {

    private static final Logger log = LoggerFactory.getLogger(UserAuthServiceImpl.class);

    private final UserManager userManager;
    private final UserAuthIdentityManager userAuthIdentityManager;
    private final UserProfileManager userProfileManager;
    private final UserSettingsManager userSettingsManager;
    private final IdentityHashService identityHashService;
    private final PasswordHashService passwordHashService;
    private final BusinessIdGenerator businessIdGenerator;

    /**
     * 构造用户认证业务服务。
     *
     * @param userManager              用户主表 Manager
     * @param userAuthIdentityManager  登录凭证 Manager
     * @param userProfileManager       用户资料 Manager
     * @param userSettingsManager      用户设置 Manager
     * @param userDeviceManager        用户设备 Manager
     * @param identityHashService      凭证归一化与哈希服务
     * @param passwordHashService      密码哈希服务
     * @param businessIdGenerator      业务主键生成器
     */
    public UserAuthServiceImpl(UserManager userManager,
                               UserAuthIdentityManager userAuthIdentityManager,
                               UserProfileManager userProfileManager,
                               UserSettingsManager userSettingsManager,
                               IdentityHashService identityHashService,
                               PasswordHashService passwordHashService,
                               BusinessIdGenerator businessIdGenerator) {
        this.userManager = userManager;
        this.userAuthIdentityManager = userAuthIdentityManager;
        this.userProfileManager = userProfileManager;
        this.userSettingsManager = userSettingsManager;
        this.identityHashService = identityHashService;
        this.passwordHashService = passwordHashService;
        this.businessIdGenerator = businessIdGenerator;
    }

    /**
     * 注册新用户，并初始化 users、auth_identities、profiles、settings 四张表基础数据。
     *
     * @param command 注册命令
     * @return 注册结果
     * @throws UserBizException 当凭证已存在、参数非法或数据库写入失败时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RegisterResult register(RegisterCommand command) {
        // 1. 参数校验：检查登录凭证、密码强度、用户类型、注册来源是否合法
        validateRegisterCommand(command);

        // 2. 凭证归一化与哈希：生成 identity_hash，禁止在日志中输出明文凭证
        String identityType = command.getIdentityType().trim().toUpperCase();
        String normalizedIdentity = identityHashService.normalize(identityType, command.getIdentityValue());
        String identityHash = identityHashService.hash(identityType, normalizedIdentity);
        String maskedIdentityValue = identityHashService.maskForStorage(identityType, normalizedIdentity);

        // 3. 幂等检查：同一个 identity_type + identity_hash 只能注册一次
        UserAuthIdentityEntity existing = userAuthIdentityManager.findByIdentityTypeAndHash(identityType, identityHash);
        if (existing != null) {
            throw new UserBizException(UserErrorCode.IDENTITY_ALREADY_EXISTS);
        }

        // 4. 生成业务主键：user_id、auth_id、profile_id、setting_id
        long userId = businessIdGenerator.nextId();
        long authId = businessIdGenerator.nextId();
        long profileId = businessIdGenerator.nextId();
        long settingId = businessIdGenerator.nextId();

        try {
            // 5. 创建 users：初始化 ACTIVE / INIT / token_version=1
            UserEntity userEntity = buildUserEntity(command, userId);
            userManager.createUser(userEntity);

            // 6. 创建 user_auth_identities：保存 identity_hash 与 password_hash
            String passwordHash = passwordHashService.hash(command.getPassword());
            userAuthIdentityManager.createIdentity(
                    authId,
                    userId,
                    identityType,
                    maskedIdentityValue,
                    identityHash,
                    passwordHash
            );

            // 7. 创建 user_profiles 默认记录
            userProfileManager.createDefaultProfile(profileId, userId);

            // 8. 创建 user_settings 默认记录
            userSettingsManager.createDefaultSettings(settingId, userId);

            // 9. 返回注册结果：不返回敏感字段，JWT 由 gateway 负责签发
            RegisterResult result = new RegisterResult();
            result.setUserId(userId);
            result.setAccountStatus(userEntity.getAccountStatus());
            result.setProfileStatus(userEntity.getProfileStatus());
            result.setTokenVersion(userEntity.getTokenVersion());
            log.info("用户注册成功, userId={}, identityType={}", userId, identityType);
            return result;
        } catch (DuplicateKeyException ex) {
            log.warn("注册唯一索引冲突, identityType={}", identityType);
            throw new UserBizException(UserErrorCode.USER_CONCURRENT_CONFLICT);
        }
    }

    private void validateRegisterCommand(RegisterCommand command) {
        if (command == null) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "注册命令不能为空");
        }
        identityHashService.parseSupportedIdentityType(command.getIdentityType());
        if (!StringUtils.hasText(command.getIdentityValue())) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "登录凭证不能为空");
        }
        passwordHashService.validateRegisterPassword(command.getPassword());
        validateUserTypeForRegister(command.getUserType());
        validateRegisterSource(command.getRegisterSource());
    }

    private void validateUserTypeForRegister(String userType) {
        if (!StringUtils.hasText(userType)) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "用户类型不能为空");
        }
        if (!UserType.BH.name().equals(userType.trim().toUpperCase())) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "本阶段仅支持 BH 用户注册");
        }
    }

    private void validateRegisterSource(String registerSource) {
        if (!StringUtils.hasText(registerSource)) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "注册来源不能为空");
        }
        try {
            RegisterSource.valueOf(registerSource.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "注册来源非法");
        }
    }

    private UserEntity buildUserEntity(RegisterCommand command, long userId) {
        UserEntity entity = new UserEntity();
        entity.setUserId(userId);
        entity.setUserType(UserType.BH.name());
        entity.setAccountStatus(AccountStatus.ACTIVE.name());
        entity.setProfileStatus(ProfileStatus.INIT.name());
        entity.setRegisterSource(command.getRegisterSource().trim().toUpperCase());
        entity.setTokenVersion(1);
        return entity;
    }
}
