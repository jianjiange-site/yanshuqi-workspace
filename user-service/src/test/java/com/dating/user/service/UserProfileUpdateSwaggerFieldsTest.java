package com.dating.user.service;

import com.dating.user.constant.AccountStatus;
import com.dating.user.constant.ProfileStatus;
import com.dating.user.constant.UserType;
import com.dating.user.dto.UpdateProfileCommand;
import com.dating.user.entity.UserEntity;
import com.dating.user.entity.UserProfileEntity;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileUpdateSwaggerFieldsTest {

    private static final long USER_ID = 7201L;

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
    void shouldUpdateSwaggerDailyFields() {
        when(userManager.findByUserId(USER_ID)).thenReturn(activeUser());
        UserProfileEntity profile = baseProfile();
        profile.setGender("MALE");
        profile.setBirthDate(LocalDate.of(1990, 1, 1));
        when(userProfileManager.findByUserId(USER_ID)).thenReturn(profile);

        UpdateProfileCommand command = new UpdateProfileCommand();
        command.setUserId(USER_ID);
        command.setNickname("NewNick");
        command.setBio("new bio");
        command.setAgePresent(true);
        command.setAge(28);
        command.setHeightPresent(true);
        command.setHeight(180);
        command.setOccupation("Designer");
        command.setEducation("PhD");
        command.setLocation("Shenzhen");

        userProfileService.updateProfile(command);

        ArgumentCaptor<UserProfileEntity> captor = ArgumentCaptor.forClass(UserProfileEntity.class);
        verify(userProfileManager).updateProfile(captor.capture());
        UserProfileEntity saved = captor.getValue();
        assertEquals("NewNick", saved.getNickname());
        assertEquals(28, saved.getAge());
        assertEquals(180, saved.getHeight());
        assertEquals("Designer", saved.getOccupation());
        assertEquals("MALE", saved.getGender());
        assertEquals(LocalDate.of(1990, 1, 1), saved.getBirthDate());
        verify(userCacheInvalidationService).evictProfileCache(USER_ID);
    }

    @Test
    void shouldNotClearGenderWhenNotProvided() {
        when(userManager.findByUserId(USER_ID)).thenReturn(activeUser());
        UserProfileEntity profile = baseProfile();
        profile.setGender("FEMALE");
        when(userProfileManager.findByUserId(USER_ID)).thenReturn(profile);

        UpdateProfileCommand command = new UpdateProfileCommand();
        command.setUserId(USER_ID);
        command.setNickname("OnlyNick");

        userProfileService.updateProfile(command);

        ArgumentCaptor<UserProfileEntity> captor = ArgumentCaptor.forClass(UserProfileEntity.class);
        verify(userProfileManager).updateProfile(captor.capture());
        assertEquals("FEMALE", captor.getValue().getGender());
    }

    private UserEntity activeUser() {
        UserEntity entity = new UserEntity();
        entity.setUserId(USER_ID);
        entity.setUserType(UserType.BH.name());
        entity.setAccountStatus(AccountStatus.ACTIVE.name());
        entity.setProfileStatus(ProfileStatus.INIT.name());
        return entity;
    }

    private UserProfileEntity baseProfile() {
        UserProfileEntity entity = new UserProfileEntity();
        entity.setId(1L);
        entity.setProfileId(2L);
        entity.setUserId(USER_ID);
        entity.setProfileScore(0);
        entity.setProfileCompleted(0);
        entity.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        entity.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        return entity;
    }
}
