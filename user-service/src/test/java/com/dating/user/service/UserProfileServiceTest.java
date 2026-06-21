package com.dating.user.service;

import com.dating.user.constant.AccountStatus;
import com.dating.user.constant.ProfileStatus;
import com.dating.user.constant.UserType;
import com.dating.user.dto.UpdateProfileCommand;
import com.dating.user.entity.UserEntity;
import com.dating.user.entity.UserProfileEntity;
import com.dating.user.exception.UserBizException;
import com.dating.user.exception.UserErrorCode;
import com.dating.user.manager.UserManager;
import com.dating.user.manager.UserProfileManager;
import com.dating.user.service.impl.UserProfileServiceImpl;
import com.dating.user.service.support.AvatarViewConverter;
import com.dating.user.service.support.ProfileAgeResolver;
import com.dating.user.service.support.ProfileBirthdayParser;
import com.dating.user.service.support.ProfileCompletionCalculator;
import com.dating.user.service.support.ProfileFieldValidator;
import com.dating.user.service.support.ProfileJsonSupport;
import com.dating.user.service.support.ProfileStatusResolver;
import com.dating.user.service.support.ProfileViewConverter;
import com.dating.user.service.support.LoginPendingCalculator;
import com.dating.user.service.support.SlowCallLogger;
import com.dating.user.vo.UserProfileDetailVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.dating.user.service.support.CacheSafeExecutor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 用户资料维护业务单元测试。
 */
@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    private static final long USER_ID = 1001L;

    @Mock
    private UserManager userManager;

    @Mock
    private UserProfileManager userProfileManager;

    @Mock
    private UserCacheInvalidationService userCacheInvalidationService;

    private ProfileFieldValidator profileFieldValidator;

    private ProfileCompletionCalculator profileCompletionCalculator;

    private ProfileJsonSupport profileJsonSupport;

    private ProfileStatusResolver profileStatusResolver;

    private ProfileViewConverter profileViewConverter;

    private UserProfileService userProfileService;

    /**
     * 初始化测试依赖。
     */
    @BeforeEach
    void setUp() {
        profileJsonSupport = new ProfileJsonSupport();
        profileFieldValidator = new ProfileFieldValidator(new ProfileBirthdayParser());
        profileCompletionCalculator = new ProfileCompletionCalculator(profileJsonSupport);
        profileStatusResolver = new ProfileStatusResolver();
        profileViewConverter = new ProfileViewConverter(
                profileJsonSupport, new ProfileBirthdayParser(), new ProfileAgeResolver(new ProfileBirthdayParser()),
                new LoginPendingCalculator(), new AvatarViewConverter());
        userProfileService = new UserProfileServiceImpl(
                userManager,
                userProfileManager,
                profileFieldValidator,
                profileCompletionCalculator,
                profileJsonSupport,
                profileStatusResolver,
                profileViewConverter,
                userCacheInvalidationService,
                SlowCallLogger.forTest()
        );
    }

    /**
     * GetSelfProfile 查询成功。
     */
    @Test
    void getSelfProfileShouldSucceed() {
        when(userManager.findByUserId(USER_ID)).thenReturn(buildUser(AccountStatus.ACTIVE.name()));
        when(userProfileManager.findByUserId(USER_ID)).thenReturn(buildProfile("nick", null));

        UserProfileDetailVO result = userProfileService.getSelfProfile(USER_ID);

        assertEquals(USER_ID, result.getUserId());
        assertEquals("nick", result.getNickname());
        assertEquals("avatar-key-original", result.getAvatarKey());
    }

    /**
     * 用户不存在查询失败。
     */
    @Test
    void getSelfProfileShouldFailWhenUserNotFound() {
        when(userManager.findByUserId(USER_ID)).thenReturn(null);

        UserBizException exception = assertThrows(UserBizException.class, () -> userProfileService.getSelfProfile(USER_ID));
        assertEquals(UserErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    /**
     * profile 不存在查询失败。
     */
    @Test
    void getSelfProfileShouldFailWhenProfileNotFound() {
        when(userManager.findByUserId(USER_ID)).thenReturn(buildUser(AccountStatus.ACTIVE.name()));
        when(userProfileManager.findByUserId(USER_ID)).thenReturn(null);

        UserBizException exception = assertThrows(UserBizException.class, () -> userProfileService.getSelfProfile(USER_ID));
        assertEquals(UserErrorCode.PROFILE_NOT_FOUND, exception.getErrorCode());
    }

    /**
     * UpdateProfile 更新成功。
     */
    @Test
    void updateProfileShouldSucceed() {
        mockUpdateContext(AccountStatus.ACTIVE.name(), buildProfile(null, null));

        UserProfileDetailVO result = userProfileService.updateProfile(buildPartialUpdateCommand());

        assertEquals("Alice", result.getNickname());
        assertEquals(15, result.getProfileScore());
        assertEquals(0, result.getProfileCompleted());
    }

    /**
     * 更新完整资料后 profile_score >= 80。
     */
    @Test
    void updateProfileShouldReachScoreAtLeast80() {
        mockUpdateContext(AccountStatus.ACTIVE.name(), buildProfile(null, null));

        UserProfileDetailVO result = userProfileService.updateProfile(buildFullUpdateCommand());

        assertTrue(result.getProfileScore() >= 80);
    }

    /**
     * 更新完整资料后 profile_completed = 1。
     */
    @Test
    void updateProfileShouldSetProfileCompleted() {
        mockUpdateContext(AccountStatus.ACTIVE.name(), buildProfile(null, null));

        UserProfileDetailVO result = userProfileService.updateProfile(buildFullUpdateCommand());

        assertEquals(1, result.getProfileCompleted());
    }

    /**
     * 更新完整资料后 users.profile_status = BASIC_DONE。
     */
    @Test
    void updateProfileShouldUpdateUsersProfileStatus() {
        mockUpdateContext(AccountStatus.ACTIVE.name(), buildProfile(null, ""));

        userProfileService.updateProfile(buildFullUpdateCommand());

        verify(userManager).updateProfileStatus(USER_ID, ProfileStatus.BASIC_DONE.name());
    }

    /**
     * 已有 avatar_key 时更新完整资料不应降级为 BASIC_DONE。
     */
    @Test
    void updateProfileShouldKeepPhotoDoneWhenAvatarExists() {
        mockUpdateContext(AccountStatus.ACTIVE.name(), buildProfile(null, "avatar/1001/a.jpg"));

        userProfileService.updateProfile(buildFullUpdateCommand());

        verify(userManager).updateProfileStatus(USER_ID, ProfileStatus.PHOTO_DONE.name());
    }

    /**
     * 非 ACTIVE 用户不能更新资料。
     */
    @Test
    void updateProfileShouldRejectDisabledUser() {
        when(userManager.findByUserId(USER_ID)).thenReturn(buildUser(AccountStatus.DISABLED.name()));

        UserBizException exception = assertThrows(UserBizException.class,
                () -> userProfileService.updateProfile(buildPartialUpdateCommand()));
        assertEquals(UserErrorCode.USER_DISABLED, exception.getErrorCode());
    }

    /**
     * BANNED 用户不能更新资料。
     */
    @Test
    void updateProfileShouldRejectBannedUser() {
        when(userManager.findByUserId(USER_ID)).thenReturn(buildUser(AccountStatus.BANNED.name()));

        UserBizException exception = assertThrows(UserBizException.class,
                () -> userProfileService.updateProfile(buildPartialUpdateCommand()));
        assertEquals(UserErrorCode.USER_BANNED, exception.getErrorCode());
    }

    /**
     * nickname 超长失败。
     */
    @Test
    void updateProfileShouldRejectOverlongNickname() {
        UpdateProfileCommand command = buildPartialUpdateCommand();
        command.setNickname("a".repeat(65));

        UserBizException exception = assertThrows(UserBizException.class, () -> userProfileService.updateProfile(command));
        assertEquals(UserErrorCode.PROFILE_UPDATE_INVALID, exception.getErrorCode());
    }

    /**
     * gender 非法失败。
     */
    @Test
    void updateProfileShouldRejectInvalidGender() {
        UpdateProfileCommand command = buildPartialUpdateCommand();
        command.setGender("INVALID");

        UserBizException exception = assertThrows(UserBizException.class, () -> userProfileService.updateProfile(command));
        assertEquals(UserErrorCode.PROFILE_UPDATE_INVALID, exception.getErrorCode());
    }

    /**
     * birthDate 为未来日期失败。
     */
    @Test
    void updateProfileShouldRejectFutureBirthDate() {
        UpdateProfileCommand command = buildPartialUpdateCommand();
        command.setBirthDate(LocalDate.now().plusDays(1));

        UserBizException exception = assertThrows(UserBizException.class, () -> userProfileService.updateProfile(command));
        assertEquals(UserErrorCode.INVALID_BIRTHDAY, exception.getErrorCode());
    }

    /**
     * bio 超长失败。
     */
    @Test
    void updateProfileShouldRejectOverlongBio() {
        UpdateProfileCommand command = buildPartialUpdateCommand();
        command.setBio("b".repeat(501));

        UserBizException exception = assertThrows(UserBizException.class, () -> userProfileService.updateProfile(command));
        assertEquals(UserErrorCode.PROFILE_UPDATE_INVALID, exception.getErrorCode());
    }

    /**
     * interests 数量过多失败。
     */
    @Test
    void updateProfileShouldRejectTooManyInterests() {
        UpdateProfileCommand command = buildPartialUpdateCommand();
        command.setInterests(java.util.stream.IntStream.range(0, 21).mapToObj(i -> "tag" + i).toList());

        UserBizException exception = assertThrows(UserBizException.class, () -> userProfileService.updateProfile(command));
        assertEquals(UserErrorCode.PROFILE_UPDATE_INVALID, exception.getErrorCode());
    }

    /**
     * 更新资料不修改 avatar_key。
     */
    @Test
    void updateProfileShouldNotModifyAvatarKey() {
        UserProfileEntity profile = buildProfile(null, null);
        mockUpdateContext(AccountStatus.ACTIVE.name(), profile);

        userProfileService.updateProfile(buildPartialUpdateCommand());

        ArgumentCaptor<UserProfileEntity> captor = ArgumentCaptor.forClass(UserProfileEntity.class);
        verify(userProfileManager).updateProfile(captor.capture());
        assertEquals("avatar-key-original", captor.getValue().getAvatarKey());
    }

    /**
     * 更新资料不写 user_photos。
     */
    @Test
    void updateProfileShouldNotDependOnUserPhotoManager() {
        Set<String> fields = Arrays.stream(UserProfileServiceImpl.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());
        assertTrue(fields.stream().noneMatch(name -> name.toLowerCase().contains("photo")));
    }

    /**
     * 返回结果不包含敏感字段。
     */
    @Test
    void profileDetailVoShouldNotContainSensitiveFields() {
        Set<String> fieldNames = Arrays.stream(UserProfileDetailVO.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());
        assertTrue(fieldNames.stream().noneMatch(name ->
                name.equalsIgnoreCase("password")
                        || name.equalsIgnoreCase("passwordHash")
                        || name.equalsIgnoreCase("identityValue")
                        || name.equalsIgnoreCase("token")
                        || name.equalsIgnoreCase("phone")
                        || name.equalsIgnoreCase("email")));
    }

    /**
     * Redis 删除失败不影响主流程。
     */
    @Test
    void updateProfileShouldSucceedWhenRedisEvictFails() {
        StringRedisTemplate stringRedisTemplate = org.mockito.Mockito.mock(StringRedisTemplate.class);
        org.mockito.Mockito.when(stringRedisTemplate.delete(org.mockito.ArgumentMatchers.anyCollection()))
                .thenThrow(new RuntimeException("redis down"));
        CacheSafeExecutor cacheSafeExecutor = new CacheSafeExecutor(stringRedisTemplate);
        UserProfileService serviceWithRealCacheEvictor = new UserProfileServiceImpl(
                userManager,
                userProfileManager,
                profileFieldValidator,
                profileCompletionCalculator,
                profileJsonSupport,
                profileStatusResolver,
                profileViewConverter,
                new UserCacheInvalidationService(cacheSafeExecutor),
                SlowCallLogger.forTest()
        );
        mockUpdateContext(AccountStatus.ACTIVE.name(), buildProfile(null, null));

        UserProfileDetailVO result = serviceWithRealCacheEvictor.updateProfile(buildPartialUpdateCommand());

        assertNotNull(result);
        verify(userProfileManager).updateProfile(any());
    }

    private void mockUpdateContext(String accountStatus, UserProfileEntity profileEntity) {
        when(userManager.findByUserId(USER_ID)).thenReturn(buildUser(accountStatus));
        when(userProfileManager.findByUserId(USER_ID)).thenReturn(profileEntity);
    }

    private UserEntity buildUser(String accountStatus) {
        UserEntity entity = new UserEntity();
        entity.setUserId(USER_ID);
        entity.setUserType(UserType.BH.name());
        entity.setAccountStatus(accountStatus);
        entity.setProfileStatus(ProfileStatus.INIT.name());
        return entity;
    }

    private UserProfileEntity buildProfile(String nickname, String avatarKey) {
        UserProfileEntity entity = new UserProfileEntity();
        entity.setId(10L);
        entity.setProfileId(2001L);
        entity.setUserId(USER_ID);
        entity.setNickname(nickname);
        entity.setAvatarKey(avatarKey == null ? "avatar-key-original" : avatarKey);
        entity.setProfileScore(0);
        entity.setProfileCompleted(0);
        entity.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        entity.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        return entity;
    }

    private UpdateProfileCommand buildPartialUpdateCommand() {
        UpdateProfileCommand command = new UpdateProfileCommand();
        command.setUserId(USER_ID);
        command.setNickname("Alice");
        return command;
    }

    private UpdateProfileCommand buildFullUpdateCommand() {
        UpdateProfileCommand command = new UpdateProfileCommand();
        command.setUserId(USER_ID);
        command.setNickname("Alice");
        command.setGender("FEMALE");
        command.setBirthDate(LocalDate.of(1995, 6, 15));
        command.setCountryCode("CN");
        command.setCityCode("SH");
        command.setLanguageCodes(List.of("zh", "en"));
        command.setBio("hello");
        command.setInterests(List.of("music", "travel"));
        return command;
    }
}
