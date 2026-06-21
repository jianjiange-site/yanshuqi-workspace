package com.dating.user.service;

import com.dating.user.constant.AccountStatus;
import com.dating.user.constant.ProfileStatus;
import com.dating.user.constant.UserType;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileViewTest {

    private static final long USER_ID = 7101L;

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
    void shouldReturnSwaggerFields() {
        when(userManager.findByUserId(USER_ID)).thenReturn(userWithLogin());
        when(userProfileManager.findByUserId(USER_ID)).thenReturn(richProfile());

        UserProfileViewVO view = userProfileService.getUserProfileView(USER_ID);

        assertEquals(USER_ID, view.getUserId());
        assertEquals("Bob", view.getNickname());
        assertTrue(view.getAge() >= 30);
        assertEquals("MALE", view.getGender());
        assertEquals(175, view.getHeight());
        assertEquals("bio", view.getBio());
        assertEquals("Dev", view.getOccupation());
        assertEquals("Master", view.getEducation());
        assertEquals("Beijing", view.getLocation());
        assertEquals("1995-01-01", view.getBirthday());
        assertFalse(view.isPending());
        assertEquals(0, view.getRegulationStatus());
        assertNotNull(view.getLastOpenAtMs());
    }

    @Test
    void shouldMapAvatarTemporarily() {
        when(userManager.findByUserId(USER_ID)).thenReturn(userWithLogin());
        when(userProfileManager.findByUserId(USER_ID)).thenReturn(richProfile());

        UserProfileViewVO view = userProfileService.getUserProfileView(USER_ID);

        assertNotNull(view.getAvatar());
        assertEquals("avatar/key.jpg", view.getAvatar().getOriginalKey());
        assertEquals("avatar/key.jpg", view.getAvatar().getMinKey());
        assertEquals("avatar/key.jpg", view.getAvatar().getMidKey());
    }

    @Test
    void shouldFailWhenUserNotFound() {
        when(userManager.findByUserId(USER_ID)).thenReturn(null);
        UserBizException ex = assertThrows(UserBizException.class, () -> userProfileService.getUserProfileView(USER_ID));
        assertEquals(UserErrorCode.USER_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void pendingShouldBeTrueForInitProfile() {
        UserEntity user = userWithLogin();
        user.setProfileStatus(ProfileStatus.INIT.name());
        UserProfileEntity profile = richProfile();
        profile.setProfileCompleted(0);
        when(userManager.findByUserId(USER_ID)).thenReturn(user);
        when(userProfileManager.findByUserId(USER_ID)).thenReturn(profile);

        assertTrue(userProfileService.getUserProfileView(USER_ID).isPending());
    }

    private UserEntity userWithLogin() {
        UserEntity entity = new UserEntity();
        entity.setUserId(USER_ID);
        entity.setUserType(UserType.BH.name());
        entity.setAccountStatus(AccountStatus.ACTIVE.name());
        entity.setProfileStatus(ProfileStatus.BASIC_DONE.name());
        entity.setLastLoginAt(OffsetDateTime.now(ZoneOffset.UTC));
        return entity;
    }

    private UserProfileEntity richProfile() {
        UserProfileEntity entity = new UserProfileEntity();
        entity.setUserId(USER_ID);
        entity.setNickname("Bob");
        entity.setGender("MALE");
        entity.setBirthDate(LocalDate.of(1995, 1, 1));
        entity.setHeight(175);
        entity.setBio("bio");
        entity.setOccupation("Dev");
        entity.setEducation("Master");
        entity.setLocation("Beijing");
        entity.setAvatarKey("avatar/key.jpg");
        entity.setProfileCompleted(1);
        entity.setRegulationStatus(0);
        return entity;
    }
}
