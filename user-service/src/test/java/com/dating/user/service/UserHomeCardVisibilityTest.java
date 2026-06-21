package com.dating.user.service;

import com.dating.user.constant.AccountStatus;
import com.dating.user.constant.UserType;
import com.dating.user.dto.GetHomeCardProfileQuery;
import com.dating.user.entity.UserEntity;
import com.dating.user.entity.UserProfileEntity;
import com.dating.user.exception.UserBizException;
import com.dating.user.exception.UserErrorCode;
import com.dating.user.manager.UserManager;
import com.dating.user.manager.UserProfileManager;
import com.dating.user.service.impl.UserHomeCardServiceImpl;
import com.dating.user.service.support.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserHomeCardVisibilityTest {

    private static final long SELF_ID = 9201L;
    private static final long TARGET_ID = 9202L;

    @Mock
    private UserManager userManager;
    @Mock
    private UserProfileManager userProfileManager;

    private UserHomeCardService userHomeCardService;

    @BeforeEach
    void setUp() {
        ProfileJsonSupport jsonSupport = new ProfileJsonSupport();
        userHomeCardService = new UserHomeCardServiceImpl(
                userManager, userProfileManager,
                new HomeCardSelfValidator(),
                new HomeCardTargetVisibilityEvaluator(new UserAvailabilityEvaluator()),
                new ProfileViewConverter(jsonSupport, new ProfileBirthdayParser(),
                        new ProfileAgeResolver(new ProfileBirthdayParser()), new LoginPendingCalculator(),
                        new AvatarViewConverter()),
                SlowCallLogger.forTest());
    }

    @Test
    void selfBannedShouldReturnPermissionDenied() {
        UserEntity self = activeUser(SELF_ID);
        self.setAccountStatus(AccountStatus.BANNED.name());
        when(userManager.findByUserId(SELF_ID)).thenReturn(self);
        GetHomeCardProfileQuery query = query();
        UserBizException ex = assertThrows(UserBizException.class, () -> userHomeCardService.getHomeCardProfile(query));
        assertEquals(UserErrorCode.USER_BANNED, ex.getErrorCode());
    }

    @Test
    void selfDisabledShouldReturnPermissionDenied() {
        UserEntity self = activeUser(SELF_ID);
        self.setAccountStatus(AccountStatus.DISABLED.name());
        when(userManager.findByUserId(SELF_ID)).thenReturn(self);
        UserBizException ex = assertThrows(UserBizException.class, () -> userHomeCardService.getHomeCardProfile(query()));
        assertEquals(UserErrorCode.USER_DISABLED, ex.getErrorCode());
    }

    @Test
    void targetNotFoundShouldReturnTargetNotFound() {
        when(userManager.findByUserId(SELF_ID)).thenReturn(activeUser(SELF_ID));
        when(userManager.findByUserId(TARGET_ID)).thenReturn(null);
        UserBizException ex = assertThrows(UserBizException.class, () -> userHomeCardService.getHomeCardProfile(query()));
        assertEquals(UserErrorCode.TARGET_USER_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void targetBannedShouldReturnUnavailable() {
        when(userManager.findByUserId(SELF_ID)).thenReturn(activeUser(SELF_ID));
        UserEntity target = activeUser(TARGET_ID);
        target.setAccountStatus(AccountStatus.BANNED.name());
        when(userManager.findByUserId(TARGET_ID)).thenReturn(target);
        when(userProfileManager.findByUserId(TARGET_ID)).thenReturn(new UserProfileEntity());
        UserBizException ex = assertThrows(UserBizException.class, () -> userHomeCardService.getHomeCardProfile(query()));
        assertEquals(UserErrorCode.TARGET_USER_UNAVAILABLE, ex.getErrorCode());
    }

    @Test
    void targetProfileMissingShouldReturnProfileNotFound() {
        when(userManager.findByUserId(SELF_ID)).thenReturn(activeUser(SELF_ID));
        when(userManager.findByUserId(TARGET_ID)).thenReturn(activeUser(TARGET_ID));
        when(userProfileManager.findByUserId(TARGET_ID)).thenReturn(null);
        UserBizException ex = assertThrows(UserBizException.class, () -> userHomeCardService.getHomeCardProfile(query()));
        assertEquals(UserErrorCode.PROFILE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void targetNegativeRegulationStatusShouldReturnUnavailable() {
        when(userManager.findByUserId(SELF_ID)).thenReturn(activeUser(SELF_ID));
        when(userManager.findByUserId(TARGET_ID)).thenReturn(activeUser(TARGET_ID));
        UserProfileEntity profile = new UserProfileEntity();
        profile.setUserId(TARGET_ID);
        profile.setRegulationStatus(-1);
        when(userProfileManager.findByUserId(TARGET_ID)).thenReturn(profile);
        UserBizException ex = assertThrows(UserBizException.class, () -> userHomeCardService.getHomeCardProfile(query()));
        assertEquals(UserErrorCode.TARGET_USER_UNAVAILABLE, ex.getErrorCode());
    }

    private GetHomeCardProfileQuery query() {
        GetHomeCardProfileQuery query = new GetHomeCardProfileQuery();
        query.setSelfUserId(SELF_ID);
        query.setTargetUserId(TARGET_ID);
        return query;
    }

    private UserEntity activeUser(long userId) {
        UserEntity entity = new UserEntity();
        entity.setUserId(userId);
        entity.setUserType(UserType.BH.name());
        entity.setAccountStatus(AccountStatus.ACTIVE.name());
        entity.setDeleted(0);
        return entity;
    }
}
