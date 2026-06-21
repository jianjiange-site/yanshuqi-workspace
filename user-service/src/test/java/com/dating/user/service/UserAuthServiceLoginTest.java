package com.dating.user.service;

import com.dating.user.constant.AccountStatus;
import com.dating.user.constant.ProfileStatus;
import com.dating.user.constant.UserType;
import com.dating.user.dto.DeviceInfoCommand;
import com.dating.user.dto.LoginCommand;
import com.dating.user.entity.UserAuthIdentityEntity;
import com.dating.user.entity.UserDeviceEntity;
import com.dating.user.entity.UserEntity;
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
import com.dating.user.vo.LoginResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 用户登录校验业务单元测试。
 */
@ExtendWith(MockitoExtension.class)
class UserAuthServiceLoginTest {

    private static final String TEST_PASSWORD = "Passw0rd1";
    private static final String DEVICE_FINGERPRINT = "device-fp-test-001";
    private static final long USER_ID = 1001L;
    private static final long AUTH_ID = 2001L;

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

    private PasswordHashService passwordHashService;

    private UserAuthService userAuthService;

    private String passwordHash;

    /**
     * 初始化测试依赖。
     */
    @BeforeEach
    void setUp() {
        identityHashService = new IdentityHashService();
        passwordHashService = new PasswordHashService();
        passwordHash = passwordHashService.hash(TEST_PASSWORD);
        userAuthService = new UserAuthServiceImpl(
                userManager,
                userAuthIdentityManager,
                userProfileManager,
                userSettingsManager,
                userDeviceManager,
                identityHashService,
                passwordHashService,
                businessIdGenerator,
                SlowCallLogger.forTest(),
                new LoginPendingCalculator(),
                new SmsCodeValidator()
        );
    }

    /**
     * 正确密码登录成功。
     */
    @Test
    void verifyLoginShouldSucceedWithCorrectPassword() {
        mockSuccessfulLoginLookup(AccountStatus.ACTIVE.name(), UserType.BH.name());
        when(userDeviceManager.findByUserIdAndDeviceFingerprint(USER_ID, DEVICE_FINGERPRINT)).thenReturn(null);
        when(businessIdGenerator.nextId()).thenReturn(3001L);

        LoginResult result = userAuthService.verifyLogin(buildValidLoginCommand());

        assertEquals(USER_ID, result.getUserId());
        assertEquals(AccountStatus.ACTIVE.name(), result.getAccountStatus());
        assertEquals(ProfileStatus.INIT.name(), result.getProfileStatus());
        assertEquals(1, result.getTokenVersion());
        assertNotNull(result.getLastLoginAt());
    }

    /**
     * 错误密码登录失败。
     */
    @Test
    void verifyLoginShouldFailWithWrongPassword() {
        when(userAuthIdentityManager.findByIdentityTypeAndHash(anyString(), anyString()))
                .thenReturn(buildAuthIdentity(passwordHash));

        LoginCommand command = buildValidLoginCommand();
        command.setPassword("WrongPass1");

        UserBizException exception = assertThrows(UserBizException.class, () -> userAuthService.verifyLogin(command));
        assertEquals(UserErrorCode.PASSWORD_INVALID, exception.getErrorCode());
    }

    /**
     * 不存在 identity 登录失败。
     */
    @Test
    void verifyLoginShouldFailWhenIdentityNotFound() {
        when(userAuthIdentityManager.findByIdentityTypeAndHash(anyString(), anyString())).thenReturn(null);

        UserBizException exception = assertThrows(UserBizException.class, () -> userAuthService.verifyLogin(buildValidLoginCommand()));
        assertEquals(UserErrorCode.IDENTITY_NOT_FOUND, exception.getErrorCode());
    }

    /**
     * BANNED 用户不能登录。
     */
    @Test
    void verifyLoginShouldRejectBannedUser() {
        mockSuccessfulLoginLookup(AccountStatus.BANNED.name(), UserType.BH.name());

        UserBizException exception = assertThrows(UserBizException.class, () -> userAuthService.verifyLogin(buildValidLoginCommand()));
        assertEquals(UserErrorCode.USER_BANNED, exception.getErrorCode());
    }

    /**
     * DELETED 用户不能登录。
     */
    @Test
    void verifyLoginShouldRejectDeletedUser() {
        mockSuccessfulLoginLookup(AccountStatus.DELETED.name(), UserType.BH.name());

        UserBizException exception = assertThrows(UserBizException.class, () -> userAuthService.verifyLogin(buildValidLoginCommand()));
        assertEquals(UserErrorCode.USER_DELETED, exception.getErrorCode());
    }

    /**
     * DISABLED 用户不能登录。
     */
    @Test
    void verifyLoginShouldRejectDisabledUser() {
        mockSuccessfulLoginLookup(AccountStatus.DISABLED.name(), UserType.BH.name());

        UserBizException exception = assertThrows(UserBizException.class, () -> userAuthService.verifyLogin(buildValidLoginCommand()));
        assertEquals(UserErrorCode.USER_DISABLED, exception.getErrorCode());
    }

    /**
     * DH 用户不能登录。
     */
    @Test
    void verifyLoginShouldRejectDhUser() {
        mockSuccessfulLoginLookup(AccountStatus.ACTIVE.name(), UserType.DH.name());

        UserBizException exception = assertThrows(UserBizException.class, () -> userAuthService.verifyLogin(buildValidLoginCommand()));
        assertEquals(UserErrorCode.USER_REQUEST_INVALID, exception.getErrorCode());
    }

    /**
     * 登录成功后更新 users.last_login_at。
     */
    @Test
    void verifyLoginShouldUpdateUsersLastLoginAt() {
        mockSuccessfulLoginLookup(AccountStatus.ACTIVE.name(), UserType.BH.name());
        when(userDeviceManager.findByUserIdAndDeviceFingerprint(USER_ID, DEVICE_FINGERPRINT)).thenReturn(null);
        when(businessIdGenerator.nextId()).thenReturn(3001L);

        userAuthService.verifyLogin(buildValidLoginCommand());

        ArgumentCaptor<OffsetDateTime> captor = ArgumentCaptor.forClass(OffsetDateTime.class);
        verify(userManager).updateLastLoginAt(eq(USER_ID), captor.capture());
        assertNotNull(captor.getValue());
    }

    /**
     * 登录成功后更新 user_auth_identities.last_login_at。
     */
    @Test
    void verifyLoginShouldUpdateAuthIdentityLastLoginAt() {
        mockSuccessfulLoginLookup(AccountStatus.ACTIVE.name(), UserType.BH.name());
        when(userDeviceManager.findByUserIdAndDeviceFingerprint(USER_ID, DEVICE_FINGERPRINT)).thenReturn(null);
        when(businessIdGenerator.nextId()).thenReturn(3001L);

        userAuthService.verifyLogin(buildValidLoginCommand());

        ArgumentCaptor<OffsetDateTime> captor = ArgumentCaptor.forClass(OffsetDateTime.class);
        verify(userAuthIdentityManager).updateLastLoginAt(eq(AUTH_ID), captor.capture());
        assertNotNull(captor.getValue());
    }

    /**
     * 登录成功后插入 user_devices。
     */
    @Test
    void verifyLoginShouldInsertUserDeviceOnFirstLogin() {
        mockSuccessfulLoginLookup(AccountStatus.ACTIVE.name(), UserType.BH.name());
        when(userDeviceManager.findByUserIdAndDeviceFingerprint(USER_ID, DEVICE_FINGERPRINT)).thenReturn(null);
        when(businessIdGenerator.nextId()).thenReturn(3001L);

        userAuthService.verifyLogin(buildValidLoginCommand());

        verify(userDeviceManager).createDevice(eq(3001L), eq(USER_ID), eq("IOS"), eq(DEVICE_FINGERPRINT), any(), eq("1.0.0"), any());
        verify(userDeviceManager, never()).updateDeviceSeen(any(), anyString(), any(), any(), any());
    }

    /**
     * 同设备再次登录只更新 user_devices，不重复插入。
     */
    @Test
    void verifyLoginShouldUpdateExistingDeviceOnRepeatLogin() {
        mockSuccessfulLoginLookup(AccountStatus.ACTIVE.name(), UserType.BH.name());
        UserDeviceEntity existing = new UserDeviceEntity();
        existing.setId(99L);
        existing.setDeviceId(3001L);
        existing.setUserId(USER_ID);
        existing.setDeviceFingerprint(DEVICE_FINGERPRINT);
        when(userDeviceManager.findByUserIdAndDeviceFingerprint(USER_ID, DEVICE_FINGERPRINT)).thenReturn(existing);

        userAuthService.verifyLogin(buildValidLoginCommand());

        verify(userDeviceManager).updateDeviceSeen(eq(existing), eq("IOS"), any(), eq("1.0.0"), any());
        verify(userDeviceManager, never()).createDevice(anyLong(), anyLong(), anyString(), anyString(), any(), any(), any());
    }

    /**
     * LoginResult 不包含敏感字段。
     */
    @Test
    void loginResultShouldNotContainSensitiveFields() {
        Set<String> fieldNames = Arrays.stream(LoginResult.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());

        assertEquals(Set.of("userId", "accountStatus", "profileStatus", "tokenVersion", "lastLoginAt"), fieldNames);
    }

    private void mockSuccessfulLoginLookup(String accountStatus, String userType) {
        when(userAuthIdentityManager.findByIdentityTypeAndHash(anyString(), anyString()))
                .thenReturn(buildAuthIdentity(passwordHash));
        when(userManager.findByUserId(USER_ID)).thenReturn(buildUserEntity(accountStatus, userType));
    }

    private UserAuthIdentityEntity buildAuthIdentity(String storedPasswordHash) {
        UserAuthIdentityEntity entity = new UserAuthIdentityEntity();
        entity.setAuthId(AUTH_ID);
        entity.setUserId(USER_ID);
        entity.setIdentityType("PHONE");
        entity.setPasswordHash(storedPasswordHash);
        return entity;
    }

    private UserEntity buildUserEntity(String accountStatus, String userType) {
        UserEntity entity = new UserEntity();
        entity.setUserId(USER_ID);
        entity.setUserType(userType);
        entity.setAccountStatus(accountStatus);
        entity.setProfileStatus(ProfileStatus.INIT.name());
        entity.setTokenVersion(1);
        return entity;
    }

    private LoginCommand buildValidLoginCommand() {
        DeviceInfoCommand deviceInfo = new DeviceInfoCommand();
        deviceInfo.setPlatform("IOS");
        deviceInfo.setDeviceFingerprint(DEVICE_FINGERPRINT);
        deviceInfo.setPushToken("push-token-sample");
        deviceInfo.setAppVersion("1.0.0");

        LoginCommand command = new LoginCommand();
        command.setIdentityType("PHONE");
        command.setIdentityValue("+8613812345678");
        command.setPassword(TEST_PASSWORD);
        command.setDeviceInfo(deviceInfo);
        return command;
    }
}
