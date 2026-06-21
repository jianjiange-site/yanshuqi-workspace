package com.dating.user.service;

import com.dating.user.constant.AccountStatus;
import com.dating.user.constant.ProfileStatus;
import com.dating.user.constant.UserType;
import com.dating.user.dto.GetHomeCardProfileQuery;
import com.dating.user.entity.UserEntity;
import com.dating.user.entity.UserProfileEntity;
import com.dating.user.exception.UserBizException;
import com.dating.user.exception.UserErrorCode;
import com.dating.user.manager.UserManager;
import com.dating.user.manager.UserProfileManager;
import com.dating.user.service.impl.UserHomeCardServiceImpl;
import com.dating.user.service.support.HomeCardSelfValidator;
import com.dating.user.service.support.HomeCardTargetVisibilityEvaluator;
import com.dating.user.service.support.LoginPendingCalculator;
import com.dating.user.service.support.ProfileAgeResolver;
import com.dating.user.service.support.ProfileBirthdayParser;
import com.dating.user.service.support.ProfileJsonSupport;
import com.dating.user.service.support.ProfileViewConverter;
import com.dating.user.service.support.SlowCallLogger;
import com.dating.user.service.support.UserAvailabilityEvaluator;
import com.dating.user.service.support.AvatarViewConverter;
import com.dating.user.vo.HomeCardProfileVO;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserHomeCardProfileTest {

    private static final long SELF_ID = 9101L;
    private static final long TARGET_ID = 9102L;

    @Mock
    private UserManager userManager;
    @Mock
    private UserProfileManager userProfileManager;

    private UserHomeCardService userHomeCardService;

    @BeforeEach
    void setUp() {
        ProfileJsonSupport jsonSupport = new ProfileJsonSupport();
        ProfileViewConverter profileViewConverter = new ProfileViewConverter(
                jsonSupport, new ProfileBirthdayParser(), new ProfileAgeResolver(new ProfileBirthdayParser()),
                new LoginPendingCalculator(), new AvatarViewConverter());
        userHomeCardService = new UserHomeCardServiceImpl(
                userManager, userProfileManager,
                new HomeCardSelfValidator(),
                new HomeCardTargetVisibilityEvaluator(new UserAvailabilityEvaluator()),
                profileViewConverter, SlowCallLogger.forTest());
    }

    @Test
    void shouldReturnHomeCardWithTargetProfile() {
        when(userManager.findByUserId(SELF_ID)).thenReturn(activeUser(SELF_ID));
        when(userManager.findByUserId(TARGET_ID)).thenReturn(activeUser(TARGET_ID));
        when(userProfileManager.findByUserId(TARGET_ID)).thenReturn(richProfile(TARGET_ID));

        GetHomeCardProfileQuery query = new GetHomeCardProfileQuery();
        query.setSelfUserId(SELF_ID);
        query.setTargetUserId(TARGET_ID);

        HomeCardProfileVO card = userHomeCardService.getHomeCardProfile(query);

        assertEquals(SELF_ID, card.getSelfUserId());
        assertNotNull(card.getTargetProfile());
        assertEquals(TARGET_ID, card.getTargetProfile().getUserId());
        assertEquals("Alice", card.getTargetProfile().getNickname());
        assertEquals("Engineer", card.getTargetProfile().getOccupation());
        assertNotNull(card.getTargetProfile().getAvatar());
        assertEquals("avatar/t.jpg", card.getTargetProfile().getAvatar().getOriginalKey());
        assertFalse(card.getTargetProfile().isPending());
        assertEquals(0, card.getTargetProfile().getRegulationStatus());
        assertNotNull(card.getTargetProfile().getLastOpenAtMs());
    }

    @Test
    void shouldRejectInvalidSelfUserId() {
        GetHomeCardProfileQuery query = new GetHomeCardProfileQuery();
        query.setSelfUserId(0L);
        query.setTargetUserId(TARGET_ID);
        UserBizException ex = assertThrows(UserBizException.class, () -> userHomeCardService.getHomeCardProfile(query));
        assertEquals(UserErrorCode.INVALID_USER_ID, ex.getErrorCode());
    }

    @Test
    void shouldRejectInvalidTargetUserId() {
        GetHomeCardProfileQuery query = new GetHomeCardProfileQuery();
        query.setSelfUserId(SELF_ID);
        query.setTargetUserId(-1L);
        UserBizException ex = assertThrows(UserBizException.class, () -> userHomeCardService.getHomeCardProfile(query));
        assertEquals(UserErrorCode.INVALID_TARGET_USER_ID, ex.getErrorCode());
    }

    @Test
    void shouldFailWhenSelfNotFound() {
        when(userManager.findByUserId(SELF_ID)).thenReturn(null);
        GetHomeCardProfileQuery query = new GetHomeCardProfileQuery();
        query.setSelfUserId(SELF_ID);
        query.setTargetUserId(TARGET_ID);
        UserBizException ex = assertThrows(UserBizException.class, () -> userHomeCardService.getHomeCardProfile(query));
        assertEquals(UserErrorCode.USER_NOT_FOUND, ex.getErrorCode());
    }

    private UserEntity activeUser(long userId) {
        UserEntity entity = new UserEntity();
        entity.setUserId(userId);
        entity.setUserType(UserType.BH.name());
        entity.setAccountStatus(AccountStatus.ACTIVE.name());
        entity.setProfileStatus(ProfileStatus.COMPLETED.name());
        entity.setLastLoginAt(OffsetDateTime.now(ZoneOffset.UTC));
        entity.setDeleted(0);
        return entity;
    }

    private UserProfileEntity richProfile(long userId) {
        UserProfileEntity entity = new UserProfileEntity();
        entity.setUserId(userId);
        entity.setNickname("Alice");
        entity.setGender("FEMALE");
        entity.setBirthDate(LocalDate.of(1996, 3, 10));
        entity.setHeight(165);
        entity.setBio("hello");
        entity.setOccupation("Engineer");
        entity.setEducation("Bachelor");
        entity.setLocation("Shanghai");
        entity.setAvatarKey("avatar/t.jpg");
        entity.setProfileCompleted(1);
        entity.setRegulationStatus(0);
        return entity;
    }
}
