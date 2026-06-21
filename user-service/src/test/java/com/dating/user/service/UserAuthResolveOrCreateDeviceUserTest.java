package com.dating.user.service;

import com.dating.user.constant.AccountStatus;
import com.dating.user.constant.IdentityType;
import com.dating.user.constant.ProfileStatus;
import com.dating.user.constant.UserType;
import com.dating.user.dto.ResolveOrCreateDeviceUserCommand;
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
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 设备匿名登录 ResolveOrCreate 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class UserAuthResolveOrCreateDeviceUserTest {

    private static final String DEVICE_ID = "device-test-00123456";
    private static final String PLATFORM = "IOS";
    private static final long USER_ID = 5001L;

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
        String normalized = identityHashService.normalizeDeviceLoginIdentity(PLATFORM, DEVICE_ID);
        identityHash = identityHashService.hash(IdentityType.DEVICE.name(), normalized);
    }

    @Test
    void firstLoginShouldCreateUser() {
        when(userAuthIdentityManager.findByIdentityTypeAndHash(IdentityType.DEVICE.name(), identityHash)).thenReturn(null);
        when(businessIdGenerator.nextId()).thenReturn(USER_ID, 6001L, 7001L, 8001L, 9001L);
        when(userDeviceManager.findByUserIdAndDeviceFingerprint(USER_ID, DEVICE_ID)).thenReturn(null);
        when(userProfileManager.findByUserId(USER_ID)).thenReturn(defaultProfile());

        ResolveOrCreateLoginUserResult result = userAuthService.resolveOrCreateDeviceUser(buildCommand());

        assertEquals(USER_ID, result.getUserId());
        assertTrue(result.isNewlyCreated());
        assertTrue(result.isPending());
        verify(userManager).createUser(any(UserEntity.class));
        verify(userAuthIdentityManager).createLoginIdentity(anyLong(), eq(USER_ID), eq(IdentityType.DEVICE.name()), anyString(), eq(identityHash));
        verify(userProfileManager).createDefaultProfile(anyLong(), eq(USER_ID));
        verify(userSettingsManager).createDefaultSettings(anyLong(), eq(USER_ID));
        verify(userManager).updateLastLoginAt(eq(USER_ID), any(OffsetDateTime.class));
        verify(userCacheInvalidationService).evictProfileCache(USER_ID);
    }

    @Test
    void sameDeviceShouldReturnSameUserId() {
        UserAuthIdentityEntity existing = new UserAuthIdentityEntity();
        existing.setAuthId(6001L);
        existing.setUserId(USER_ID);
        when(userAuthIdentityManager.findByIdentityTypeAndHash(IdentityType.DEVICE.name(), identityHash)).thenReturn(existing);
        when(userManager.findByUserId(USER_ID)).thenReturn(activeUser());
        when(userDeviceManager.findByUserIdAndDeviceFingerprint(USER_ID, DEVICE_ID)).thenReturn(existingDevice());
        when(userProfileManager.findByUserId(USER_ID)).thenReturn(defaultProfile());

        ResolveOrCreateLoginUserResult first = userAuthService.resolveOrCreateDeviceUser(buildCommand());
        ResolveOrCreateLoginUserResult second = userAuthService.resolveOrCreateDeviceUser(buildCommand());

        assertEquals(USER_ID, first.getUserId());
        assertEquals(USER_ID, second.getUserId());
        assertFalse(second.isNewlyCreated());
        verify(userManager, never()).createUser(any());
        verify(userCacheInvalidationService, never()).evictProfileCache(anyLong());
    }

    @Test
    void emptyDeviceIdShouldFail() {
        ResolveOrCreateDeviceUserCommand command = buildCommand();
        command.setDeviceId("");
        UserBizException ex = assertThrows(UserBizException.class, () -> userAuthService.resolveOrCreateDeviceUser(command));
        assertEquals(UserErrorCode.INVALID_DEVICE_ID, ex.getErrorCode());
    }

    @Test
    void invalidPlatformShouldFail() {
        ResolveOrCreateDeviceUserCommand command = buildCommand();
        command.setPlatform("INVALID");
        UserBizException ex = assertThrows(UserBizException.class, () -> userAuthService.resolveOrCreateDeviceUser(command));
        assertEquals(UserErrorCode.INVALID_PLATFORM, ex.getErrorCode());
    }

    @Test
    void shouldUpsertDeviceAndUpdateLastLoginAt() {
        UserAuthIdentityEntity existing = new UserAuthIdentityEntity();
        existing.setAuthId(6001L);
        existing.setUserId(USER_ID);
        when(userAuthIdentityManager.findByIdentityTypeAndHash(IdentityType.DEVICE.name(), identityHash)).thenReturn(existing);
        when(userManager.findByUserId(USER_ID)).thenReturn(activeUser());
        when(userDeviceManager.findByUserIdAndDeviceFingerprint(USER_ID, DEVICE_ID)).thenReturn(null);
        when(businessIdGenerator.nextId()).thenReturn(9001L);
        when(userProfileManager.findByUserId(USER_ID)).thenReturn(defaultProfile());

        userAuthService.resolveOrCreateDeviceUser(buildCommand());

        verify(userDeviceManager).createDevice(eq(9001L), eq(USER_ID), eq(PLATFORM), eq(DEVICE_ID), any(), any(), any(OffsetDateTime.class));
        verify(userManager).updateLastLoginAt(eq(USER_ID), any(OffsetDateTime.class));
        verify(userAuthIdentityManager).updateLastLoginAt(eq(6001L), any(OffsetDateTime.class));
    }

    @Test
    void identityLookupShouldUseHashNotPlainDeviceId() {
        when(userAuthIdentityManager.findByIdentityTypeAndHash(IdentityType.DEVICE.name(), identityHash)).thenReturn(null);
        when(businessIdGenerator.nextId()).thenReturn(USER_ID, 6001L, 7001L, 8001L, 9001L);
        when(userDeviceManager.findByUserIdAndDeviceFingerprint(USER_ID, DEVICE_ID)).thenReturn(null);
        when(userProfileManager.findByUserId(USER_ID)).thenReturn(defaultProfile());

        userAuthService.resolveOrCreateDeviceUser(buildCommand());

        ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
        verify(userAuthIdentityManager, times(1))
                .findByIdentityTypeAndHash(eq(IdentityType.DEVICE.name()), hashCaptor.capture());
        assertEquals(identityHash, hashCaptor.getValue());
        assertFalse(hashCaptor.getValue().contains(DEVICE_ID));
    }

    private ResolveOrCreateDeviceUserCommand buildCommand() {
        ResolveOrCreateDeviceUserCommand command = new ResolveOrCreateDeviceUserCommand();
        command.setDeviceId(DEVICE_ID);
        command.setPlatform(PLATFORM);
        command.setAppVersion("1.0.0");
        command.setPushToken("push-token-test-value");
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

    private UserDeviceEntity existingDevice() {
        UserDeviceEntity device = new UserDeviceEntity();
        device.setDeviceId(9001L);
        device.setUserId(USER_ID);
        device.setDeviceFingerprint(DEVICE_ID);
        return device;
    }
}
