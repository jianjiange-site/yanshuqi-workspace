package com.dating.user.service;

import com.dating.user.constant.AccountStatus;
import com.dating.user.constant.IdentityType;
import com.dating.user.constant.ProfileStatus;
import com.dating.user.constant.UserType;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 三方登录 ResolveOrCreate 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class UserAuthResolveOrCreateThirdPartyUserTest {

    private static final String ID_TOKEN = "google-id-token-test-value-001";
    private static final String DEVICE_ID = "device-oauth-0012345";
    private static final String PLATFORM = "IOS";
    private static final long USER_ID = 5201L;

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
    private String googleIdentityHash;

    @BeforeEach
    void setUp() {
        identityHashService = new IdentityHashService();
        userAuthService = new UserAuthServiceImpl(
                userManager, userAuthIdentityManager, userProfileManager, userSettingsManager,
                userDeviceManager, identityHashService, new PasswordHashService(), businessIdGenerator,
                SlowCallLogger.forTest(), new LoginPendingCalculator(), new SmsCodeValidator());
        String normalized = identityHashService.normalizeThirdPartyIdentity(ID_TOKEN);
        googleIdentityHash = identityHashService.hash(IdentityType.GOOGLE.name(), normalized);
    }

    @Test
    void googleFirstLoginShouldCreateUser() {
        when(userAuthIdentityManager.findByIdentityTypeAndHash(IdentityType.GOOGLE.name(), googleIdentityHash)).thenReturn(null);
        when(businessIdGenerator.nextId()).thenReturn(USER_ID, 6201L, 7201L, 8201L, 9201L);
        when(userDeviceManager.findByUserIdAndDeviceFingerprint(USER_ID, DEVICE_ID)).thenReturn(null);
        when(userProfileManager.findByUserId(USER_ID)).thenReturn(defaultProfile());

        ResolveOrCreateLoginUserResult result = userAuthService.resolveOrCreateThirdPartyUser(buildCommand(1));

        assertEquals(USER_ID, result.getUserId());
        assertTrue(result.isNewlyCreated());
        verify(userAuthIdentityManager).createLoginIdentity(anyLong(), eq(USER_ID), eq(IdentityType.GOOGLE.name()), anyString(), eq(googleIdentityHash));
    }

    @Test
    void appleAndFacebookPlatformShouldBeValid() {
        mockExistingIdentity(IdentityType.APPLE, 2);
        ResolveOrCreateLoginUserResult apple = userAuthService.resolveOrCreateThirdPartyUser(buildCommand(2));
        assertEquals(USER_ID, apple.getUserId());

        mockExistingIdentity(IdentityType.FACEBOOK, 3);
        ResolveOrCreateLoginUserResult facebook = userAuthService.resolveOrCreateThirdPartyUser(buildCommand(3));
        assertEquals(USER_ID, facebook.getUserId());
    }

    @Test
    void sameIdTokenShouldReturnSameUserId() {
        UserAuthIdentityEntity existing = new UserAuthIdentityEntity();
        existing.setAuthId(6201L);
        existing.setUserId(USER_ID);
        when(userAuthIdentityManager.findByIdentityTypeAndHash(IdentityType.GOOGLE.name(), googleIdentityHash)).thenReturn(existing);
        when(userManager.findByUserId(USER_ID)).thenReturn(activeUser());
        when(userDeviceManager.findByUserIdAndDeviceFingerprint(USER_ID, DEVICE_ID)).thenReturn(null);
        when(businessIdGenerator.nextId()).thenReturn(9201L);
        when(userProfileManager.findByUserId(USER_ID)).thenReturn(defaultProfile());

        ResolveOrCreateLoginUserResult result = userAuthService.resolveOrCreateThirdPartyUser(buildCommand(1));

        assertEquals(USER_ID, result.getUserId());
        assertFalse(result.isNewlyCreated());
        verify(userManager, never()).createUser(any());
    }

    @Test
    void invalidThirdPartyPlatformShouldFail() {
        UserBizException ex = assertThrows(UserBizException.class,
                () -> userAuthService.resolveOrCreateThirdPartyUser(buildCommand(99)));
        assertEquals(UserErrorCode.INVALID_THIRD_PARTY_PLATFORM, ex.getErrorCode());
    }

    @Test
    void emptyIdTokenShouldFail() {
        ResolveOrCreateThirdPartyUserCommand command = buildCommand(1);
        command.setIdToken("");
        UserBizException ex = assertThrows(UserBizException.class, () -> userAuthService.resolveOrCreateThirdPartyUser(command));
        assertEquals(UserErrorCode.INVALID_THIRD_PARTY_IDENTITY, ex.getErrorCode());
    }

    private void mockExistingIdentity(IdentityType identityType, int platformCode) {
        String normalized = identityHashService.normalizeThirdPartyIdentity(ID_TOKEN + platformCode);
        String hash = identityHashService.hash(identityType.name(), normalized);
        UserAuthIdentityEntity existing = new UserAuthIdentityEntity();
        existing.setAuthId(6201L);
        existing.setUserId(USER_ID);
        when(userAuthIdentityManager.findByIdentityTypeAndHash(identityType.name(), hash)).thenReturn(existing);
        when(userManager.findByUserId(USER_ID)).thenReturn(activeUser());
        when(userDeviceManager.findByUserIdAndDeviceFingerprint(USER_ID, DEVICE_ID)).thenReturn(null);
        when(businessIdGenerator.nextId()).thenReturn(9201L);
        when(userProfileManager.findByUserId(USER_ID)).thenReturn(defaultProfile());
    }

    private ResolveOrCreateThirdPartyUserCommand buildCommand(int platform) {
        ResolveOrCreateThirdPartyUserCommand command = new ResolveOrCreateThirdPartyUserCommand();
        command.setThirdPartyPlatform(platform);
        command.setIdToken(platform == 1 ? ID_TOKEN : ID_TOKEN + platform);
        command.setGoogleEmail("user@example.com");
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
        profile.setUserId(USER_ID);
        profile.setProfileCompleted(0);
        return profile;
    }
}
