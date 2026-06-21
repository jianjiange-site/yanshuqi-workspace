package com.dating.user.service;

import com.dating.user.constant.AccountStatus;
import com.dating.user.constant.ProfileStatus;
import com.dating.user.constant.UserType;
import com.dating.user.dto.UpsertOnboardingCommand;
import com.dating.user.entity.UserEntity;
import com.dating.user.entity.UserProfileEntity;
import com.dating.user.exception.UserBizException;
import com.dating.user.exception.UserErrorCode;
import com.dating.user.manager.UserManager;
import com.dating.user.manager.UserProfileManager;
import com.dating.user.service.impl.UserProfileServiceImpl;
import com.dating.user.service.support.AvatarViewConverter;
import com.dating.user.service.support.LoginPendingCalculator;
import com.dating.user.service.support.ProfileAgeResolver;
import com.dating.user.service.support.ProfileBirthdayParser;
import com.dating.user.service.support.ProfileCompletionCalculator;
import com.dating.user.service.support.ProfileFieldValidator;
import com.dating.user.service.support.ProfileJsonSupport;
import com.dating.user.service.support.ProfileStatusResolver;
import com.dating.user.service.support.ProfileViewConverter;
import com.dating.user.service.support.SlowCallLogger;
import com.dating.user.vo.UserProfileViewVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileUpsertOnboardingTest {

    private static final long USER_ID = 7001L;

    @Mock
    private UserManager userManager;
    @Mock
    private UserProfileManager userProfileManager;
    @Mock
    private UserCacheInvalidationService userCacheInvalidationService;

    private UserProfileService userProfileService;

    @BeforeEach
    void setUp() {
        ProfileJsonSupport jsonSupport = new ProfileJsonSupport();
        userProfileService = new UserProfileServiceImpl(
                userManager, userProfileManager,
                new ProfileFieldValidator(new ProfileBirthdayParser()),
                new ProfileCompletionCalculator(jsonSupport),
                jsonSupport, new ProfileStatusResolver(),
                new ProfileViewConverter(jsonSupport, new ProfileBirthdayParser(),
                        new ProfileAgeResolver(new ProfileBirthdayParser()), new LoginPendingCalculator(),
                        new AvatarViewConverter()),
                userCacheInvalidationService, SlowCallLogger.forTest());
    }

    @Test
    void shouldUpsertOnboardingSuccessfully() {
        when(userManager.findByUserId(USER_ID)).thenReturn(activeUser());
        when(userProfileManager.findByUserId(USER_ID)).thenReturn(emptyProfile());

        UserProfileViewVO result = userProfileService.upsertOnboarding(fullOnboardingCommand());

        assertEquals("Alice", result.getNickname());
        assertEquals("FEMALE", result.getGender());
        assertEquals("1995-06-15", result.getBirthday());
        assertFalse(result.isPending());
        verify(userCacheInvalidationService).evictProfileCache(USER_ID);
    }

    @Test
    void shouldSupportSlashBirthdayFormat() {
        when(userManager.findByUserId(USER_ID)).thenReturn(activeUser());
        when(userProfileManager.findByUserId(USER_ID)).thenReturn(emptyProfile());
        UpsertOnboardingCommand command = fullOnboardingCommand();
        command.setBirthday("1995/06/15");

        UserProfileViewVO result = userProfileService.upsertOnboarding(command);

        assertEquals("1995-06-15", result.getBirthday());
    }

    @Test
    void shouldRejectInvalidBirthday() {
        UpsertOnboardingCommand command = fullOnboardingCommand();
        command.setBirthday("bad-date");
        assertThrows(UserBizException.class, () -> userProfileService.upsertOnboarding(command));
    }

    @Test
    void shouldRejectNegativeHeight() {
        UpsertOnboardingCommand command = fullOnboardingCommand();
        command.setHeight(-1);
        UserBizException ex = assertThrows(UserBizException.class, () -> userProfileService.upsertOnboarding(command));
        assertEquals(UserErrorCode.INVALID_HEIGHT, ex.getErrorCode());
    }

    @Test
    void shouldRejectNegativeAge() {
        UpsertOnboardingCommand command = fullOnboardingCommand();
        command.setBirthday(null);
        command.setAge(-1);
        UserBizException ex = assertThrows(UserBizException.class, () -> userProfileService.upsertOnboarding(command));
        assertEquals(UserErrorCode.INVALID_AGE, ex.getErrorCode());
    }

    @Test
    void shouldRejectBannedUser() {
        UserEntity banned = activeUser();
        banned.setAccountStatus(AccountStatus.BANNED.name());
        when(userManager.findByUserId(USER_ID)).thenReturn(banned);
        UserBizException ex = assertThrows(UserBizException.class,
                () -> userProfileService.upsertOnboarding(fullOnboardingCommand()));
        assertEquals(UserErrorCode.USER_BANNED, ex.getErrorCode());
    }

    @Test
    void shouldPersistSwaggerFields() {
        when(userManager.findByUserId(USER_ID)).thenReturn(activeUser());
        when(userProfileManager.findByUserId(USER_ID)).thenReturn(emptyProfile());

        userProfileService.upsertOnboarding(fullOnboardingCommand());

        ArgumentCaptor<UserProfileEntity> captor = ArgumentCaptor.forClass(UserProfileEntity.class);
        verify(userProfileManager).updateProfile(captor.capture());
        UserProfileEntity saved = captor.getValue();
        assertEquals("Engineer", saved.getOccupation());
        assertEquals("Bachelor", saved.getEducation());
        assertEquals("Shanghai", saved.getLocation());
        assertTrue(saved.getProfileCompleted() == 1);
    }

    private UpsertOnboardingCommand fullOnboardingCommand() {
        UpsertOnboardingCommand command = new UpsertOnboardingCommand();
        command.setUserId(USER_ID);
        command.setNickname("Alice");
        command.setGender("FEMALE");
        command.setBirthday("1995-06-15");
        command.setHeight(170);
        command.setBio("hello");
        command.setOccupation("Engineer");
        command.setEducation("Bachelor");
        command.setLocation("Shanghai");
        command.setDefaultAvatarObjectKey("avatar/default.jpg");
        return command;
    }

    private UserEntity activeUser() {
        UserEntity entity = new UserEntity();
        entity.setUserId(USER_ID);
        entity.setUserType(UserType.BH.name());
        entity.setAccountStatus(AccountStatus.ACTIVE.name());
        entity.setProfileStatus(ProfileStatus.INIT.name());
        return entity;
    }

    private UserProfileEntity emptyProfile() {
        UserProfileEntity entity = new UserProfileEntity();
        entity.setId(1L);
        entity.setProfileId(2L);
        entity.setUserId(USER_ID);
        entity.setProfileScore(0);
        entity.setProfileCompleted(0);
        entity.setRegulationStatus(0);
        entity.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        entity.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        return entity;
    }
}
