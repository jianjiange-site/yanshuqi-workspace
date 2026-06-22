package com.dating.match.service;

import com.dating.match.exception.MatchBizException;
import com.dating.match.exception.MatchErrorCode;
import com.dating.match.manager.ProfileVisitManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProfileVisitServiceTest {

    @Mock
    private ProfileVisitManager profileVisitManager;

    private ProfileVisitService profileVisitService;

    @BeforeEach
    void setUp() {
        profileVisitService = new ProfileVisitService(profileVisitManager);
    }

    @Test
    void recordVisit_firstVisit_shouldDelegateUpsert() {
        profileVisitService.recordVisit(10001L, 20002L);
        verify(profileVisitManager).upsertVisit(10001L, 20002L);
    }

    @Test
    void recordVisit_repeatVisit_shouldDelegateUpsertAgain() {
        profileVisitService.recordVisit(10001L, 20002L);
        profileVisitService.recordVisit(10001L, 20002L);
        verify(profileVisitManager, times(2)).upsertVisit(10001L, 20002L);
    }

    @Test
    void recordVisit_selfVisit_shouldReject() {
        MatchBizException ex = assertThrows(MatchBizException.class,
                () -> profileVisitService.recordVisit(10001L, 10001L));
        assertEquals(MatchErrorCode.INVALID_ARGUMENT, ex.getErrorCode());
    }

    @Test
    void recordVisit_invalidIds_shouldReject() {
        assertThrows(MatchBizException.class, () -> profileVisitService.recordVisit(0, 20002L));
        assertThrows(MatchBizException.class, () -> profileVisitService.recordVisit(10001L, 0));
    }
}
