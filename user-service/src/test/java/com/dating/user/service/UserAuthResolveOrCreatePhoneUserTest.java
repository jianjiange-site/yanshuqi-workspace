package com.dating.user.service;

import com.dating.user.constant.AccountStatus;
import com.dating.user.constant.IdentityType;
import com.dating.user.constant.ProfileStatus;
import com.dating.user.constant.UserType;
import com.dating.user.dto.ResolveOrCreatePhoneUserCommand;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;

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
 * 手机号登录 ResolveOrCreate 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class UserAuthResolveOrCreatePhoneUserTest {

    private static final String PHONE = "13812345678";
    private static final String SMS_CODE = "123456";
    private static final String DEVICE_ID = "device-phone-001234";
    private static final String PLATFORM = "ANDROID";
    private static final long USER_ID = 5101L;

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
    @Mock
    private UserCacheInvalidationService userCacheInvalidationService;

    private IdentityHashService identityHashService;
    private UserAuthService userAuthService;
    private String identityHash;

    @BeforeEach
    void setUp() {
        identityHashService = new IdentityHashService();
        userAuthService = new UserAuthServiceImpl(
                userManager, userAuthIdentityManager, userProfileManager, userSettingsManager,
                userDeviceManager, identityHashService, new PasswordHashService(), businessIdGenerator,
                SlowCallLogger.forTest(), new LoginPendingCalculator(), new SmsCodeValidator(),
                userCacheInvalidationService);
        String normalizedPhone = identityHashService.normalizePhoneLoginIdentity(PHONE);
        identityHash = identityHashService.hash(IdentityType.PHONE.name(), normalizedPhone);
    }

    @Test
    void firstLoginShouldCreateUser() {
        when(userAuthIdentityManager.findByIdentityTypeAndHash(IdentityType.PHONE.name(), identityHash)).thenReturn(null);
        when(businessIdGenerator.nextId()).thenReturn(USER_ID, 6101L, 7101L, 8101L, 9101L);
        when(userDeviceManager.findByUserIdAndDeviceFingerprint(USER_ID, DEVICE_ID)).thenReturn(null);
        when(userProfileManager.findByUserId(USER_ID)).thenReturn(defaultProfile());

        ResolveOrCreateLoginUserResult result = userAuthService.resolveOrCreatePhoneUser(buildCommand());

        assertEquals(USER_ID, result.getUserId());
        assertTrue(result.isNewlyCreated());
        verify(userManager).createUser(any(UserEntity.class));
        verify(userAuthIdentityManager).createLoginIdentity(anyLong(), eq(USER_ID), eq(IdentityType.PHONE.name()), anyString(), eq(identityHash));
    }

    @Test
    void samePhoneShouldReturnSameUserId() {
        UserAuthIdentityEntity existing = new UserAuthIdentityEntity();
        existing.setAuthId(6101L);
        existing.setUserId(USER_ID);
        when(userAuthIdentityManager.findByIdentityTypeAndHash(IdentityType.PHONE.name(), identityHash)).thenReturn(existing);
        when(userManager.findByUserId(USER_ID)).thenReturn(activeUser());
        when(userDeviceManager.findByUserIdAndDeviceFingerprint(USER_ID, DEVICE_ID)).thenReturn(null);
        when(businessIdGenerator.nextId()).thenReturn(9101L);
        when(userProfileManager.findByUserId(USER_ID)).thenReturn(defaultProfile());

        ResolveOrCreateLoginUserResult result = userAuthService.resolveOrCreatePhoneUser(buildCommand());

        assertEquals(USER_ID, result.getUserId());
        assertFalse(result.isNewlyCreated());
        verify(userManager, never()).createUser(any());
    }

    @Test
    void identityLookupShouldNotUsePlainPhone() {
        when(userAuthIdentityManager.findByIdentityTypeAndHash(IdentityType.PHONE.name(), identityHash)).thenReturn(null);
        when(businessIdGenerator.nextId()).thenReturn(USER_ID, 6101L, 7101L, 8101L, 9101L);
        when(userDeviceManager.findByUserIdAndDeviceFingerprint(USER_ID, DEVICE_ID)).thenReturn(null);
        when(userProfileManager.findByUserId(USER_ID)).thenReturn(defaultProfile());

        userAuthService.resolveOrCreatePhoneUser(buildCommand());

        ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
        verify(userAuthIdentityManager).findByIdentityTypeAndHash(eq(IdentityType.PHONE.name()), hashCaptor.capture());
        assertFalse(hashCaptor.getValue().contains(PHONE));
    }

    @Test
    void invalidPhoneShouldFail() {
        ResolveOrCreatePhoneUserCommand command = buildCommand();
        command.setPhone("abc");
        UserBizException ex = assertThrows(UserBizException.class, () -> userAuthService.resolveOrCreatePhoneUser(command));
        assertEquals(UserErrorCode.INVALID_PHONE, ex.getErrorCode());
    }

    @Test
    void emptySmsCodeShouldFail() {
        ResolveOrCreatePhoneUserCommand command = buildCommand();
        command.setSmsCode("");
        UserBizException ex = assertThrows(UserBizException.class, () -> userAuthService.resolveOrCreatePhoneUser(command));
        assertEquals(UserErrorCode.INVALID_SMS_CODE, ex.getErrorCode());
    }

    private ResolveOrCreatePhoneUserCommand buildCommand() {
        ResolveOrCreatePhoneUserCommand command = new ResolveOrCreatePhoneUserCommand();
        command.setPhone(PHONE);
        command.setSmsCode(SMS_CODE);
        command.setDeviceId(DEVICE_ID);
        command.setPlatform(PLATFORM);
        command.setAppVersion("2.0.0");
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
