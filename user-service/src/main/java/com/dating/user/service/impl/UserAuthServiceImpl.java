package com.dating.user.service.impl;

import com.dating.user.constant.AccountStatus;
import com.dating.user.constant.DevicePlatform;
import com.dating.user.constant.ProfileStatus;
import com.dating.user.constant.IdentityType;
import com.dating.user.constant.RegisterSource;
import com.dating.user.constant.ThirdPartyPlatform;
import com.dating.user.constant.UserType;
import com.dating.user.dto.DeviceInfoCommand;
import com.dating.user.dto.LoginCommand;
import com.dating.user.dto.RegisterCommand;
import com.dating.user.dto.ResolveOrCreateDeviceUserCommand;
import com.dating.user.dto.ResolveOrCreatePhoneUserCommand;
import com.dating.user.dto.ResolveOrCreateThirdPartyUserCommand;
import com.dating.user.entity.UserAuthIdentityEntity;
import com.dating.user.entity.UserDeviceEntity;
import com.dating.user.entity.UserEntity;
import com.dating.user.entity.UserProfileEntity;
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
import com.dating.user.service.support.LoginPendingCalculator;
import com.dating.user.service.support.PasswordHashService;
import com.dating.user.service.support.SlowCallLogger;
import com.dating.user.service.support.SmsCodeValidator;
import com.dating.user.vo.LoginResult;
import com.dating.user.vo.RegisterResult;
import com.dating.user.vo.ResolveOrCreateLoginUserResult;
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
    private final UserDeviceManager userDeviceManager;
    private final IdentityHashService identityHashService;
    private final PasswordHashService passwordHashService;
    private final BusinessIdGenerator businessIdGenerator;
    private final SlowCallLogger slowCallLogger;
    private final LoginPendingCalculator loginPendingCalculator;
    private final SmsCodeValidator smsCodeValidator;

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
     * @param slowCallLogger           慢调用日志记录器
     * @param loginPendingCalculator   pending 计算器
     * @param smsCodeValidator         短信验证码校验入口
     */
    public UserAuthServiceImpl(UserManager userManager,
                               UserAuthIdentityManager userAuthIdentityManager,
                               UserProfileManager userProfileManager,
                               UserSettingsManager userSettingsManager,
                               UserDeviceManager userDeviceManager,
                               IdentityHashService identityHashService,
                               PasswordHashService passwordHashService,
                               BusinessIdGenerator businessIdGenerator,
                               SlowCallLogger slowCallLogger,
                               LoginPendingCalculator loginPendingCalculator,
                               SmsCodeValidator smsCodeValidator) {
        this.userManager = userManager;
        this.userAuthIdentityManager = userAuthIdentityManager;
        this.userProfileManager = userProfileManager;
        this.userSettingsManager = userSettingsManager;
        this.userDeviceManager = userDeviceManager;
        this.identityHashService = identityHashService;
        this.passwordHashService = passwordHashService;
        this.businessIdGenerator = businessIdGenerator;
        this.slowCallLogger = slowCallLogger;
        this.loginPendingCalculator = loginPendingCalculator;
        this.smsCodeValidator = smsCodeValidator;
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
        long startNano = System.nanoTime();
        boolean success = true;
        String errorCode = null;
        Long resultUserId = null;
        try {
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
                resultUserId = userId;
                log.info("用户注册成功, userId={}, identityType={}", userId, identityType);
                return result;
            } catch (DuplicateKeyException ex) {
                log.warn("注册唯一索引冲突, identityType={}, errorCode={}",
                        identityType, UserErrorCode.USER_CONCURRENT_CONFLICT.getCode());
                throw new UserBizException(UserErrorCode.USER_CONCURRENT_CONFLICT);
            }
        } catch (UserBizException ex) {
            success = false;
            errorCode = ex.getErrorCode().getCode();
            throw ex;
        } finally {
            slowCallLogger.logIfSlow("register", startNano, resultUserId, success, errorCode);
        }
    }

    /**
     * 校验登录凭证与密码，更新设备与最近登录时间，不签发 JWT。
     *
     * @param command 登录命令
     * @return 登录结果
     * @throws UserBizException 当凭证不存在、密码错误、账号状态非法或写入失败时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResult verifyLogin(LoginCommand command) {
        long startNano = System.nanoTime();
        boolean success = true;
        String errorCode = null;
        Long resultUserId = null;
        try {
            // 1. 参数校验：检查登录凭证、密码、设备信息是否合法
            validateLoginCommand(command);

            // 2. 凭证归一化与哈希：与注册阶段使用同一套规则
            String identityType = command.getIdentityType().trim().toUpperCase();
            String normalizedIdentity = identityHashService.normalize(identityType, command.getIdentityValue());
            String identityHash = identityHashService.hash(identityType, normalizedIdentity);

            // 3. 查询登录凭证
            UserAuthIdentityEntity authIdentity = userAuthIdentityManager.findByIdentityTypeAndHash(identityType, identityHash);
            if (authIdentity == null) {
                throw new UserBizException(UserErrorCode.IDENTITY_NOT_FOUND);
            }

            // 4. 密码校验：使用 BCrypt 比对，禁止明文比较
            if (!passwordHashService.matches(command.getPassword(), authIdentity.getPasswordHash())) {
                throw new UserBizException(UserErrorCode.PASSWORD_INVALID);
            }

            // 5. 查询用户主表
            UserEntity userEntity = userManager.findByUserId(authIdentity.getUserId());
            if (userEntity == null) {
                throw new UserBizException(UserErrorCode.USER_NOT_FOUND);
            }

            // 6. 校验账号状态与用户类型
            validateAccountStatus(userEntity.getAccountStatus());
            validateLoginUserType(userEntity.getUserType());

            // 7. 设备信息处理：push_token 转 hash，device_fingerprint 不打印明文
            DeviceInfoCommand deviceInfo = command.getDeviceInfo();
            String platform = deviceInfo.getPlatform().trim().toUpperCase();
            String deviceFingerprint = normalizeDeviceFingerprint(deviceInfo.getDeviceFingerprint());
            String pushTokenHash = identityHashService.hashPushToken(deviceInfo.getPushToken());
            String appVersion = StringUtils.hasText(deviceInfo.getAppVersion()) ? deviceInfo.getAppVersion().trim() : null;

            OffsetDateTime lastLoginAt = OffsetDateTime.now(ZoneOffset.UTC);
            long userId = userEntity.getUserId();

            try {
                // 8. upsert user_devices：同 user_id + device_fingerprint 不重复创建
                upsertUserDevice(userId, platform, deviceFingerprint, pushTokenHash, appVersion, lastLoginAt);

                // 9. 更新 users 与 auth_identity 的 last_login_at
                userManager.updateLastLoginAt(userId, lastLoginAt);
                userAuthIdentityManager.updateLastLoginAt(authIdentity.getAuthId(), lastLoginAt);
            } catch (DuplicateKeyException ex) {
                log.warn("登录设备唯一索引冲突, userId={}, errorCode={}",
                        userId, UserErrorCode.USER_CONCURRENT_CONFLICT.getCode());
                throw new UserBizException(UserErrorCode.USER_CONCURRENT_CONFLICT);
            }

            // 10. 构建 LoginResult：不返回敏感字段，JWT 由 gateway 负责签发
            LoginResult result = new LoginResult();
            result.setUserId(userId);
            result.setAccountStatus(userEntity.getAccountStatus());
            result.setProfileStatus(userEntity.getProfileStatus());
            result.setTokenVersion(userEntity.getTokenVersion());
            result.setLastLoginAt(lastLoginAt);
            resultUserId = userId;
            log.info("用户登录校验成功, userId={}, identityType={}", userId, identityType);
            return result;
        } catch (UserBizException ex) {
            success = false;
            errorCode = ex.getErrorCode().getCode();
            throw ex;
        } finally {
            slowCallLogger.logIfSlow("verifyLogin", startNano, resultUserId, success, errorCode);
        }
    }

    /**
     * 设备匿名登录：按 deviceId + platform 解析或创建用户，不签发 JWT。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResolveOrCreateLoginUserResult resolveOrCreateDeviceUser(ResolveOrCreateDeviceUserCommand command) {
        long startNano = System.nanoTime();
        boolean success = true;
        String errorCode = null;
        Long resultUserId = null;
        try {
            if (command == null) {
                throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "设备登录命令不能为空");
            }
            // 1. 凭证归一化：deviceId + platform 组合作为 DEVICE 身份，禁止明文作为查询主键
            String normalizedDeviceId = identityHashService.normalizeDeviceId(command.getDeviceId());
            identityHashService.parsePlatform(command.getPlatform());
            String normalizedIdentity = identityHashService.normalizeDeviceLoginIdentity(command.getPlatform(), normalizedDeviceId);
            String identityType = IdentityType.DEVICE.name();
            String identityHash = identityHashService.hash(identityType, normalizedIdentity);
            DeviceInfoCommand deviceInfo = toDeviceInfo(command.getPlatform(), normalizedDeviceId,
                    command.getPushToken(), command.getAppVersion());

            ResolveOrCreateLoginUserResult result = resolveOrCreateIdentityUser(
                    identityType, normalizedIdentity, identityHash, RegisterSource.DEVICE, deviceInfo);
            resultUserId = result.getUserId();
            log.info("设备匿名登录成功, userId={}, identityType={}, newlyCreated={}, deviceId={}",
                    resultUserId, identityType, result.isNewlyCreated(),
                    identityHashService.maskDeviceIdForLog(normalizedDeviceId));
            return result;
        } catch (UserBizException ex) {
            success = false;
            errorCode = ex.getErrorCode().getCode();
            throw ex;
        } finally {
            slowCallLogger.logIfSlow("resolveOrCreateDeviceUser", startNano, resultUserId, success, errorCode);
        }
    }

    /**
     * 手机号登录：解析或创建 PHONE 身份用户，不校验真实短信，不签发 JWT。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResolveOrCreateLoginUserResult resolveOrCreatePhoneUser(ResolveOrCreatePhoneUserCommand command) {
        long startNano = System.nanoTime();
        boolean success = true;
        String errorCode = null;
        Long resultUserId = null;
        try {
            if (command == null) {
                throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "手机号登录命令不能为空");
            }
            // 1. 短信验证码校验入口，本阶段不做真实短信校验
            smsCodeValidator.validate(command.getSmsCode());
            // 2. 手机号归一化后生成 identity_hash，禁止明文查询
            String normalizedPhone = identityHashService.normalizePhoneLoginIdentity(command.getPhone());
            String identityType = IdentityType.PHONE.name();
            String identityHash = identityHashService.hash(identityType, normalizedPhone);
            String normalizedDeviceId = identityHashService.normalizeDeviceId(command.getDeviceId());
            identityHashService.parsePlatform(command.getPlatform());
            DeviceInfoCommand deviceInfo = toDeviceInfo(command.getPlatform(), normalizedDeviceId,
                    command.getPushToken(), command.getAppVersion());

            ResolveOrCreateLoginUserResult result = resolveOrCreateIdentityUser(
                    identityType, normalizedPhone, identityHash, RegisterSource.PHONE, deviceInfo);
            resultUserId = result.getUserId();
            log.info("手机号登录成功, userId={}, identityType={}, newlyCreated={}",
                    resultUserId, identityType, result.isNewlyCreated());
            return result;
        } catch (UserBizException ex) {
            success = false;
            errorCode = ex.getErrorCode().getCode();
            throw ex;
        } finally {
            slowCallLogger.logIfSlow("resolveOrCreatePhoneUser", startNano, resultUserId, success, errorCode);
        }
    }

    /**
     * 三方登录：解析或创建 GOOGLE / APPLE / FACEBOOK 身份用户，不校验真实 OAuth，不签发 JWT。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResolveOrCreateLoginUserResult resolveOrCreateThirdPartyUser(ResolveOrCreateThirdPartyUserCommand command) {
        long startNano = System.nanoTime();
        boolean success = true;
        String errorCode = null;
        Long resultUserId = null;
        try {
            if (command == null) {
                throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "三方登录命令不能为空");
            }
            ThirdPartyPlatform thirdPartyPlatform = ThirdPartyPlatform.fromPlatformCode(command.getThirdPartyPlatform());
            // 1. idToken 哈希作为稳定 identity，禁止明文入库或打印；googleEmail 仅辅助，不参与主键
            String normalizedIdentity = identityHashService.normalizeThirdPartyIdentity(command.getIdToken());
            String identityType = thirdPartyPlatform.getIdentityType().name();
            String identityHash = identityHashService.hash(identityType, normalizedIdentity);
            String normalizedDeviceId = identityHashService.normalizeDeviceId(command.getDeviceId());
            identityHashService.parsePlatform(command.getPlatform());
            DeviceInfoCommand deviceInfo = toDeviceInfo(command.getPlatform(), normalizedDeviceId,
                    command.getPushToken(), command.getAppVersion());

            ResolveOrCreateLoginUserResult result = resolveOrCreateIdentityUser(
                    identityType, normalizedIdentity, identityHash,
                    thirdPartyPlatform.getRegisterSource(), deviceInfo);
            resultUserId = result.getUserId();
            log.info("三方登录成功, userId={}, identityType={}, newlyCreated={}",
                    resultUserId, identityType, result.isNewlyCreated());
            return result;
        } catch (UserBizException ex) {
            success = false;
            errorCode = ex.getErrorCode().getCode();
            throw ex;
        } finally {
            slowCallLogger.logIfSlow("resolveOrCreateThirdPartyUser", startNano, resultUserId, success, errorCode);
        }
    }

    /**
     * 登录来源统一编排：查询或创建身份用户、upsert 设备、更新 last_login_at、计算 pending。
     */
    private ResolveOrCreateLoginUserResult resolveOrCreateIdentityUser(String identityType,
                                                                         String normalizedIdentity,
                                                                         String identityHash,
                                                                         RegisterSource registerSource,
                                                                         DeviceInfoCommand deviceInfo) {
        String maskedIdentityValue = identityHashService.maskForStorage(identityType, normalizedIdentity);
        UserAuthIdentityEntity authIdentity = userAuthIdentityManager.findByIdentityTypeAndHash(identityType, identityHash);
        boolean newlyCreated = false;
        UserEntity userEntity;
        long authId;

        if (authIdentity == null) {
            // 首次登录：本地事务创建 users / auth / profile / settings
            newlyCreated = true;
            long userId = businessIdGenerator.nextId();
            authId = businessIdGenerator.nextId();
            long profileId = businessIdGenerator.nextId();
            long settingId = businessIdGenerator.nextId();
            try {
                userEntity = buildLoginUserEntity(userId, registerSource);
                userManager.createUser(userEntity);
                userAuthIdentityManager.createLoginIdentity(authId, userId, identityType, maskedIdentityValue, identityHash);
                userProfileManager.createDefaultProfile(profileId, userId);
                userSettingsManager.createDefaultSettings(settingId, userId);
            } catch (DuplicateKeyException ex) {
                log.warn("登录来源创建唯一索引冲突, identityType={}, errorCode={}",
                        identityType, UserErrorCode.USER_CONCURRENT_CONFLICT.getCode());
                authIdentity = userAuthIdentityManager.findByIdentityTypeAndHash(identityType, identityHash);
                if (authIdentity == null) {
                    throw new UserBizException(UserErrorCode.USER_CONCURRENT_CONFLICT);
                }
                newlyCreated = false;
                userEntity = loadUserOrThrow(authIdentity.getUserId());
                authId = authIdentity.getAuthId();
            }
        } else {
            userEntity = loadUserOrThrow(authIdentity.getUserId());
            authId = authIdentity.getAuthId();
        }

        validateAccountStatus(userEntity.getAccountStatus());

        String platform = deviceInfo.getPlatform().trim().toUpperCase();
        String deviceFingerprint = deviceInfo.getDeviceFingerprint();
        String pushTokenHash = identityHashService.hashPushToken(deviceInfo.getPushToken());
        String appVersion = StringUtils.hasText(deviceInfo.getAppVersion()) ? deviceInfo.getAppVersion().trim() : null;
        OffsetDateTime lastLoginAt = OffsetDateTime.now(ZoneOffset.UTC);
        long userId = userEntity.getUserId();

        try {
            // upsert user_devices：同 user_id + device_fingerprint 不重复创建
            upsertUserDevice(userId, platform, deviceFingerprint, pushTokenHash, appVersion, lastLoginAt);
            userManager.updateLastLoginAt(userId, lastLoginAt);
            userAuthIdentityManager.updateLastLoginAt(authId, lastLoginAt);
        } catch (DuplicateKeyException ex) {
            log.warn("登录来源设备唯一索引冲突, userId={}, errorCode={}",
                    userId, UserErrorCode.USER_CONCURRENT_CONFLICT.getCode());
            throw new UserBizException(UserErrorCode.USER_CONCURRENT_CONFLICT);
        }

        // pending 根据 profile_status / profile_completed 计算
        UserProfileEntity profileEntity = userProfileManager.findByUserId(userId);
        Integer profileCompleted = profileEntity != null ? profileEntity.getProfileCompleted() : 0;
        boolean pending = loginPendingCalculator.computePending(userEntity.getProfileStatus(), profileCompleted);

        ResolveOrCreateLoginUserResult result = new ResolveOrCreateLoginUserResult();
        result.setUserId(userId);
        result.setNewlyCreated(newlyCreated);
        result.setPending(pending);
        result.setAccountStatus(userEntity.getAccountStatus());
        result.setProfileStatus(userEntity.getProfileStatus());
        result.setTokenVersion(userEntity.getTokenVersion());
        result.setLastLoginAt(lastLoginAt);
        return result;
    }

    private UserEntity loadUserOrThrow(long userId) {
        UserEntity userEntity = userManager.findByUserId(userId);
        if (userEntity == null) {
            throw new UserBizException(UserErrorCode.USER_NOT_FOUND);
        }
        return userEntity;
    }

    private DeviceInfoCommand toDeviceInfo(String platform, String deviceId, String pushToken, String appVersion) {
        DeviceInfoCommand deviceInfo = new DeviceInfoCommand();
        deviceInfo.setPlatform(platform);
        deviceInfo.setDeviceFingerprint(deviceId);
        deviceInfo.setPushToken(pushToken);
        deviceInfo.setAppVersion(appVersion);
        return deviceInfo;
    }

    private UserEntity buildLoginUserEntity(long userId, RegisterSource registerSource) {
        UserEntity entity = new UserEntity();
        entity.setUserId(userId);
        entity.setUserType(UserType.BH.name());
        entity.setAccountStatus(AccountStatus.ACTIVE.name());
        entity.setProfileStatus(ProfileStatus.INIT.name());
        entity.setRegisterSource(registerSource.name());
        entity.setTokenVersion(1);
        return entity;
    }

    private void upsertUserDevice(long userId,
                                  String platform,
                                  String deviceFingerprint,
                                  String pushTokenHash,
                                  String appVersion,
                                  OffsetDateTime lastSeenAt) {
        UserDeviceEntity existing = userDeviceManager.findByUserIdAndDeviceFingerprint(userId, deviceFingerprint);
        if (existing == null) {
            long deviceId = businessIdGenerator.nextId();
            userDeviceManager.createDevice(deviceId, userId, platform, deviceFingerprint, pushTokenHash, appVersion, lastSeenAt);
            return;
        }
        userDeviceManager.updateDeviceSeen(existing, platform, pushTokenHash, appVersion, lastSeenAt);
    }

    private void validateLoginCommand(LoginCommand command) {
        if (command == null) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "登录命令不能为空");
        }
        identityHashService.parseSupportedIdentityType(command.getIdentityType());
        if (!StringUtils.hasText(command.getIdentityValue())) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "登录凭证不能为空");
        }
        passwordHashService.validateLoginPassword(command.getPassword());
        validateDeviceInfo(command.getDeviceInfo());
    }

    private void validateDeviceInfo(DeviceInfoCommand deviceInfo) {
        if (deviceInfo == null) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "设备信息不能为空");
        }
        if (!StringUtils.hasText(deviceInfo.getPlatform())) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "设备平台不能为空");
        }
        try {
            DevicePlatform.valueOf(deviceInfo.getPlatform().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "设备平台非法");
        }
        normalizeDeviceFingerprint(deviceInfo.getDeviceFingerprint());
    }

    private String normalizeDeviceFingerprint(String deviceFingerprint) {
        if (!StringUtils.hasText(deviceFingerprint)) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "设备指纹不能为空");
        }
        String normalized = deviceFingerprint.trim();
        if (normalized.length() < 8 || normalized.length() > 128) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "设备指纹格式非法");
        }
        return normalized;
    }

    private void validateAccountStatus(String accountStatus) {
        if (!StringUtils.hasText(accountStatus)) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "账号状态非法");
        }
        final AccountStatus status;
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

    private void validateLoginUserType(String userType) {
        if (!StringUtils.hasText(userType)) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "用户类型非法");
        }
        if (UserType.DH.name().equals(userType.trim().toUpperCase())) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "本阶段不支持 DH 用户普通登录");
        }
        if (!UserType.BH.name().equals(userType.trim().toUpperCase())) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "用户类型非法");
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
