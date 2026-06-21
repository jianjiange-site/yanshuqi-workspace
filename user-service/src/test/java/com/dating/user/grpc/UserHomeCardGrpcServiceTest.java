package com.dating.user.grpc;

import com.dating.user.constant.AccountStatus;
import com.dating.user.constant.UserType;
import com.dating.user.dto.GetHomeCardProfileQuery;
import com.dating.user.entity.UserEntity;
import com.dating.user.entity.UserProfileEntity;
import com.dating.user.manager.UserManager;
import com.dating.user.manager.UserProfileManager;
import com.dating.user.service.UserHomeCardService;
import com.dating.user.service.impl.UserHomeCardServiceImpl;
import com.dating.user.service.support.*;
import com.dating.user.vo.HomeCardProfileVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserHomeCardGrpcServiceTest {

    private static final long SELF_ID = 9301L;
    private static final long TARGET_ID = 9302L;

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
    void serviceShouldProvideFieldsForGrpcMapping() {
        when(userManager.findByUserId(SELF_ID)).thenReturn(activeUser(SELF_ID));
        when(userManager.findByUserId(TARGET_ID)).thenReturn(activeUser(TARGET_ID));
        UserProfileEntity profile = new UserProfileEntity();
        profile.setUserId(TARGET_ID);
        profile.setNickname("Bob");
        profile.setProfileCompleted(1);
        profile.setRegulationStatus(0);
        profile.setBirthDate(LocalDate.of(1990, 1, 1));
        when(userProfileManager.findByUserId(TARGET_ID)).thenReturn(profile);

        GetHomeCardProfileQuery query = new GetHomeCardProfileQuery();
        query.setSelfUserId(SELF_ID);
        query.setTargetUserId(TARGET_ID);
        HomeCardProfileVO card = userHomeCardService.getHomeCardProfile(query);

        assertEquals(SELF_ID, card.getSelfUserId());
        assertNotNull(card.getTargetProfile());
        assertEquals(TARGET_ID, card.getTargetProfile().getUserId());
        assertEquals("Bob", card.getTargetProfile().getNickname());
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
