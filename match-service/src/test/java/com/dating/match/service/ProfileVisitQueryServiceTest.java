package com.dating.match.service;

import com.dating.match.entity.ProfileVisitEntity;
import com.dating.match.exception.MatchBizException;
import com.dating.match.exception.MatchErrorCode;
import com.dating.match.manager.ProfileVisitManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileVisitQueryServiceTest {

    private static final long TARGET = 10001L;

    @Mock
    private ProfileVisitManager profileVisitManager;

    private ProfileVisitQueryService profileVisitQueryService;

    @BeforeEach
    void setUp() {
        profileVisitQueryService = new ProfileVisitQueryService(profileVisitManager);
    }

    @Test
    void listVisits_shouldReturnVisitorsForTargetUser() {
        ProfileVisitEntity visit = visit(70001L, 1L, 20002L, TARGET, 3, hoursAgo(10), hoursAgo(1));
        when(profileVisitManager.listVisitors(TARGET, 20, "")).thenReturn(List.of(visit));

        var result = profileVisitQueryService.listVisits(TARGET, 0, "");
        assertEquals(1, result.getVisits().size());
        assertEquals(20002L, result.getVisits().get(0).getFromUserId());
        assertEquals(TARGET, visit.getTargetUserId());
    }

    @Test
    void listVisits_shouldOrderByLastVisitedAtDesc() {
        ProfileVisitEntity newer = visit(70002L, 2L, 20003L, TARGET, 1, hoursAgo(5), hoursAgo(1));
        ProfileVisitEntity older = visit(70003L, 3L, 20004L, TARGET, 2, hoursAgo(20), hoursAgo(10));
        when(profileVisitManager.listVisitors(TARGET, 20, "")).thenReturn(List.of(newer, older));

        var result = profileVisitQueryService.listVisits(TARGET, 0, "");
        assertTrue(result.getVisits().get(0).getLastVisitedAtMs() > result.getVisits().get(1).getLastVisitedAtMs());
    }

    @Test
    void listVisits_shouldReturnBizIdNotPhysicalId() {
        ProfileVisitEntity visit = visit(77777L, 999L, 20005L, TARGET, 1, hoursAgo(3), hoursAgo(2));
        when(profileVisitManager.listVisitors(TARGET, 20, "")).thenReturn(List.of(visit));

        var result = profileVisitQueryService.listVisits(TARGET, 0, "");
        assertEquals(77777L, result.getVisits().get(0).getVisitId());
    }

    @Test
    void listVisits_shouldCapPageSizeAt50() {
        when(profileVisitManager.listVisitors(eq(TARGET), eq(50), eq(""))).thenReturn(List.of());
        profileVisitQueryService.listVisits(TARGET, 100, "");
    }

    @Test
    void listVisits_invalidCaller_shouldReject() {
        MatchBizException ex = assertThrows(MatchBizException.class,
                () -> profileVisitQueryService.listVisits(0, 20, ""));
        assertEquals(MatchErrorCode.INVALID_ARGUMENT, ex.getErrorCode());
    }

    private static ProfileVisitEntity visit(long bizId, long physicalId, long from, long target, int count,
                                            OffsetDateTime first, OffsetDateTime last) {
        ProfileVisitEntity entity = new ProfileVisitEntity();
        entity.setBizId(bizId);
        entity.setId(physicalId);
        entity.setFromUserId(from);
        entity.setTargetUserId(target);
        entity.setVisitCount(count);
        entity.setFirstVisitedAt(first);
        entity.setLastVisitedAt(last);
        return entity;
    }

    private static OffsetDateTime hoursAgo(int hours) {
        return OffsetDateTime.now(ZoneOffset.UTC).minusHours(hours);
    }
}
