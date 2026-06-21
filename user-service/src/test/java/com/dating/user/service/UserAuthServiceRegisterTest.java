package com.dating.user.service;

import com.dating.user.constant.AccountStatus;
import com.dating.user.constant.ProfileStatus;
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
import com.dating.user.service.impl.UserAuthServiceImpl;
import com.dating.user.service.support.BusinessIdGenerator;
import com.dating.user.service.support.IdentityHashService;
import com.dating.user.service.support.LoginPendingCalculator;
import com.dating.user.service.support.PasswordHashService;
import com.dating.user.service.support.SlowCallLogger;
import com.dating.user.service.support.SmsCodeValidator;
import com.dating.user.vo.RegisterResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 用户注册业务单元测试。
 */
@ExtendWith(MockitoExtension.class)
class UserAuthServiceRegisterTest {

    private static final String TEST_PASSWORD = "Passw0rd1";

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

    /**
     * 初始化测试依赖。
     */
    @BeforeEach
    void setUp() {
        identityHashService = new IdentityHashService();
        passwordHashService = new PasswordHashService();
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
     * 正常 BH 注册成功。
     */
    @Test
    void registerShouldSucceedForBhUser() {
        when(businessIdGenerator.nextId()).thenReturn(1001L, 2001L, 3001L, 4001L);
        when(userAuthIdentityManager.findByIdentityTypeAndHash(anyString(), anyString())).thenReturn(null);

        RegisterCommand command = buildValidPhoneCommand();
        RegisterResult result = userAuthService.register(command);

        assertEquals(1001L, result.getUserId());
        assertEquals(AccountStatus.ACTIVE.name(), result.getAccountStatus());
        assertEquals(ProfileStatus.INIT.name(), result.getProfileStatus());
        assertEquals(1, result.getTokenVersion());
    }

    /**
     * 重复 identity 注册失败。
     */
    @Test
    void registerShouldFailWhenIdentityAlreadyExists() {
        UserAuthIdentityEntity existing = new UserAuthIdentityEntity();
        existing.setUserId(999L);
        when(userAuthIdentityManager.findByIdentityTypeAndHash(anyString(), anyString())).thenReturn(existing);

        RegisterCommand command = buildValidPhoneCommand();
        UserBizException exception = assertThrows(UserBizException.class, () -> userAuthService.register(command));

        assertEquals(UserErrorCode.IDENTITY_ALREADY_EXISTS, exception.getErrorCode());
        verify(userManager, never()).createUser(any());
    }

    /**
     * 四张表 user_id 一致。
     */
    @Test
    void registerShouldUseSameUserIdAcrossFourTables() {
        when(businessIdGenerator.nextId()).thenReturn(1001L, 2001L, 3001L, 4001L);
        when(userAuthIdentityManager.findByIdentityTypeAndHash(anyString(), anyString())).thenReturn(null);

        userAuthService.register(buildValidPhoneCommand());

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userManager).createUser(userCaptor.capture());
        assertEquals(1001L, userCaptor.getValue().getUserId());

        verify(userAuthIdentityManager).createIdentity(eq(2001L), eq(1001L), anyString(), anyString(), anyString(), anyString());
        verify(userProfileManager).createDefaultProfile(3001L, 1001L);
        verify(userSettingsManager).createDefaultSettings(4001L, 1001L);
    }

    /**
     * password_hash 不等于明文 password。
     */
    @Test
    void registerShouldStorePasswordHashNotPlainPassword() {
        when(businessIdGenerator.nextId()).thenReturn(1001L, 2001L, 3001L, 4001L);
        when(userAuthIdentityManager.findByIdentityTypeAndHash(anyString(), anyString())).thenReturn(null);

        userAuthService.register(buildValidPhoneCommand());

        ArgumentCaptor<String> passwordHashCaptor = ArgumentCaptor.forClass(String.class);
        verify(userAuthIdentityManager).createIdentity(anyLong(), anyLong(), anyString(), anyString(), anyString(), passwordHashCaptor.capture());
        assertNotEquals(TEST_PASSWORD, passwordHashCaptor.getValue());
        assertNotNull(passwordHashCaptor.getValue());
    }

    /**
     * RegisterResult 不包含敏感字段。
     */
    @Test
    void registerResultShouldNotContainSensitiveFields() {
        Set<String> fieldNames = Arrays.stream(RegisterResult.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());

        assertEquals(Set.of("userId", "accountStatus", "profileStatus", "tokenVersion"), fieldNames);
    }

    /**
     * 注册中任一步失败时抛出并发冲突异常。
     */
    @Test
    void registerShouldFailWhenAnyStepHitsUniqueConstraint() {
        when(businessIdGenerator.nextId()).thenReturn(1001L, 2001L, 3001L, 4001L);
        when(userAuthIdentityManager.findByIdentityTypeAndHash(anyString(), anyString())).thenReturn(null);
        doThrow(new DuplicateKeyException("duplicate")).when(userSettingsManager).createDefaultSettings(anyLong(), anyLong());

        UserBizException exception = assertThrows(UserBizException.class, () -> userAuthService.register(buildValidPhoneCommand()));

        assertEquals(UserErrorCode.USER_CONCURRENT_CONFLICT, exception.getErrorCode());
    }

    /**
     * 不支持 DH 注册。
     */
    @Test
    void registerShouldRejectDhUserType() {
        RegisterCommand command = buildValidPhoneCommand();
        command.setUserType("DH");

        UserBizException exception = assertThrows(UserBizException.class, () -> userAuthService.register(command));

        assertEquals(UserErrorCode.USER_REQUEST_INVALID, exception.getErrorCode());
    }

    /**
     * 非法 identity_type 失败。
     */
    @Test
    void registerShouldRejectInvalidIdentityType() {
        RegisterCommand command = buildValidPhoneCommand();
        command.setIdentityType("GOOGLE");

        UserBizException exception = assertThrows(UserBizException.class, () -> userAuthService.register(command));

        assertEquals(UserErrorCode.USER_REQUEST_INVALID, exception.getErrorCode());
    }

    /**
     * 非法 password 失败。
     */
    @Test
    void registerShouldRejectInvalidPassword() {
        RegisterCommand command = buildValidPhoneCommand();
        command.setPassword("123");

        UserBizException exception = assertThrows(UserBizException.class, () -> userAuthService.register(command));

        assertEquals(UserErrorCode.PASSWORD_INVALID, exception.getErrorCode());
    }

    private RegisterCommand buildValidPhoneCommand() {
        RegisterCommand command = new RegisterCommand();
        command.setIdentityType("PHONE");
        command.setIdentityValue("+8613812345678");
        command.setPassword(TEST_PASSWORD);
        command.setUserType("BH");
        command.setRegisterSource("PHONE");
        return command;
    }
}
