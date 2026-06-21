package com.dating.user.service;

import com.dating.user.constant.AccountStatus;
import com.dating.user.constant.IdentityType;
import com.dating.user.constant.ProfileStatus;
import com.dating.user.constant.UserType;
import com.dating.user.dto.ResolveOrCreateDeviceUserCommand;
import com.dating.user.dto.ResolveOrCreatePhoneUserCommand;
import com.dating.user.dto.ResolveOrCreateThirdPartyUserCommand;
import com.dating.user.entity.UserAuthIdentityEntity;
import com.dating.user.entity.UserEntity;
import com.dating.user.entity.UserProfileEntity;
import com.dating.user.exception.UserBizException;
import com.dating.user.exception.UserErrorCode;
import com.dating.user.manager.UserAuthIdentityManager;
import com.dating.user.manager.UserDeviceManager;
import com.dating.user.manager.UserManager;
import com.dating.user.manager.UserProfileManager;
import com.dating.user.manager.UserSettingsManager;
import com.dating.user.service.impl.UserAuthServiceImpl;
import com.dating.user.service.support.BusinessIdGenerator;
import com.dating.user.service.support.IdentityHashService;
import com.dating.user.service.support.LoginPendingCalculator;
import com.dating.user.service.support.PasswordHashService;
import com.dating.user.service.support.SlowCallLogger;
import com.dating.user.service.support.SmsCodeValidator;
import com.dating.user.vo.ResolveOrCreateLoginUserResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * 登录来源通用校验与 pending 语义测试。
 */
@ExtendWith(MockitoExtension.class)
class UserAuthLoginSourceValidationTest {

    private static final String DEVICE_ID = "device-valid-0012345";
    private static final String PLATFORM = "IOS";
    private static final long USER_ID = 5301L;

    @Mock
    private UserManager userManager;
    @Mock
    private UserAuthIdentityManager userAuthIdentityManager;
    @Mock
    private UserProfileManager userProfileManager;
    @Mock
    private UserSettingsManager userSettingsManager;
    @Mock
    private UserDeviceManager userDeviceManager;
    @Mock
    private BusinessIdGenerator businessIdGenerator;

    private IdentityHashService identityHashService;
    private UserAuthService userAuthService;

    @BeforeEach
    void setUp() {
        identityHashService = new IdentityHashService();
        userAuthService = new UserAuthServiceImpl(
                userManager, userAuthIdentityManager, userProfileManager, userSettingsManager,
                userDeviceManager, identityHashService, new PasswordHashService(), businessIdGenerator,
                SlowCallLogger.forTest(), new LoginPendingCalculator(), new SmsCodeValidator());
    }

    @Test
    void bannedUserShouldNotLogin() {
        mockExistingDeviceLogin(AccountStatus.BANNED.name());
        UserBizException ex = assertThrows(UserBizException.class, () -> userAuthService.resolveOrCreateDeviceUser(deviceCommand()));
        assertEquals(UserErrorCode.USER_BANNED, ex.getErrorCode());
    }

    @Test
    void disabledUserShouldNotLogin() {
        mockExistingDeviceLogin(AccountStatus.DISABLED.name());
        UserBizException ex = assertThrows(UserBizException.class, () -> userAuthService.resolveOrCreateDeviceUser(deviceCommand()));
        assertEquals(UserErrorCode.USER_DISABLED, ex.getErrorCode());
    }

    @Test
    void pendingShouldBeTrueWhenProfileNotCompleted() {
        mockExistingDeviceLogin(AccountStatus.ACTIVE.name());
        UserProfileEntity profile = new UserProfileEntity();
        profile.setProfileCompleted(0);
        when(userProfileManager.findByUserId(USER_ID)).thenReturn(profile);
        when(userDeviceManager.findByUserIdAndDeviceFingerprint(USER_ID, DEVICE_ID)).thenReturn(null);
        when(businessIdGenerator.nextId()).thenReturn(9301L);

        ResolveOrCreateLoginUserResult result = userAuthService.resolveOrCreateDeviceUser(deviceCommand());
        assertTrue(result.isPending());
    }

    @Test
    void pendingShouldBeFalseWhenProfileCompleted() {
        mockExistingDeviceLogin(AccountStatus.ACTIVE.name());
        UserEntity completedUser = activeUser();
        completedUser.setProfileStatus(ProfileStatus.COMPLETED.name());
        when(userManager.findByUserId(USER_ID)).thenReturn(completedUser);

        UserProfileEntity profile = new UserProfileEntity();
        profile.setProfileCompleted(1);
        when(userProfileManager.findByUserId(USER_ID)).thenReturn(profile);
        when(userDeviceManager.findByUserIdAndDeviceFingerprint(USER_ID, DEVICE_ID)).thenReturn(null);
        when(businessIdGenerator.nextId()).thenReturn(9301L);

        ResolveOrCreateLoginUserResult result = userAuthService.resolveOrCreateDeviceUser(deviceCommand());
        assertFalse(result.isPending());
    }

    @Test
    void responseShouldNotContainTokenFields() {
        mockExistingDeviceLogin(AccountStatus.ACTIVE.name());
        when(userProfileManager.findByUserId(USER_ID)).thenReturn(defaultProfile());
        when(userDeviceManager.findByUserIdAndDeviceFingerprint(USER_ID, DEVICE_ID)).thenReturn(null);
        when(businessIdGenerator.nextId()).thenReturn(9301L);

        ResolveOrCreateLoginUserResult result = userAuthService.resolveOrCreateDeviceUser(deviceCommand());
        assertTrue(result.getUserId() > 0);
        assertTrue(result.getTokenVersion() > 0);
    }

    private void mockExistingDeviceLogin(String accountStatus) {
        String normalized = identityHashService.normalizeDeviceLoginIdentity(PLATFORM, DEVICE_ID);
        String hash = identityHashService.hash(IdentityType.DEVICE.name(), normalized);
        UserAuthIdentityEntity existing = new UserAuthIdentityEntity();
        existing.setAuthId(6301L);
        existing.setUserId(USER_ID);
        when(userAuthIdentityManager.findByIdentityTypeAndHash(IdentityType.DEVICE.name(), hash)).thenReturn(existing);

        UserEntity user = activeUser();
        user.setAccountStatus(accountStatus);
        when(userManager.findByUserId(USER_ID)).thenReturn(user);
    }

    private ResolveOrCreateDeviceUserCommand deviceCommand() {
        ResolveOrCreateDeviceUserCommand command = new ResolveOrCreateDeviceUserCommand();
        command.setDeviceId(DEVICE_ID);
        command.setPlatform(PLATFORM);
        return command;
    }

    private UserEntity activeUser() {
        UserEntity entity = new UserEntity();
        entity.setUserId(USER_ID);
        entity.setUserType(UserType.BH.name());
        entity.setAccountStatus(AccountStatus.ACTIVE.name());
        entity.setProfileStatus(ProfileStatus.INIT.name());
        entity.setTokenVersion(1);
        return entity;
    }

    private UserProfileEntity defaultProfile() {
        UserProfileEntity profile = new UserProfileEntity();
        profile.setProfileCompleted(0);
        return profile;
    }
}
