package com.dating.user.service;

import com.dating.user.constant.AccountStatus;
import com.dating.user.constant.UserType;
import com.dating.user.dto.GetHomeCardProfileQuery;
import com.dating.user.entity.UserEntity;
import com.dating.user.entity.UserProfileEntity;
import com.dating.user.manager.UserManager;
import com.dating.user.manager.UserProfileManager;
import com.dating.user.service.impl.UserHomeCardServiceImpl;
import com.dating.user.service.support.*;
import com.dating.user.vo.HomeCardProfileVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileViewReuseTest {

    @Mock
    private UserManager userManager;
    @Mock
    private UserProfileManager userProfileManager;
    @Mock
    private ProfileViewConverter profileViewConverter;

    @Test
    void shouldReuseProfileViewConverterForTarget() {
        UserHomeCardService service = new UserHomeCardServiceImpl(
                userManager, userProfileManager,
                new HomeCardSelfValidator(),
                new HomeCardTargetVisibilityEvaluator(new UserAvailabilityEvaluator()),
                profileViewConverter, SlowCallLogger.forTest());

        UserEntity self = new UserEntity();
        self.setUserId(1L);
        self.setAccountStatus(AccountStatus.ACTIVE.name());
        self.setDeleted(0);
        UserEntity target = new UserEntity();
        target.setUserId(2L);
        target.setAccountStatus(AccountStatus.ACTIVE.name());
        target.setDeleted(0);
        UserProfileEntity profile = new UserProfileEntity();
        profile.setUserId(2L);
        profile.setRegulationStatus(0);

        when(userManager.findByUserId(1L)).thenReturn(self);
        when(userManager.findByUserId(2L)).thenReturn(target);
        when(userProfileManager.findByUserId(2L)).thenReturn(profile);
        when(profileViewConverter.toView(target, profile)).thenReturn(new com.dating.user.vo.UserProfileViewVO());

        GetHomeCardProfileQuery query = new GetHomeCardProfileQuery();
        query.setSelfUserId(1L);
        query.setTargetUserId(2L);
        HomeCardProfileVO card = service.getHomeCardProfile(query);

        verify(profileViewConverter).toView(target, profile);
        assertEquals(1L, card.getSelfUserId());
    }
}
