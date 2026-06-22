package com.dating.match.service;

import com.dating.match.client.MockCandidateClient;
import com.dating.match.entity.MatchEntity;
import com.dating.match.exception.MatchBizException;
import com.dating.match.exception.MatchErrorCode;
import com.dating.match.manager.MatchManager;
import com.dating.match.recommend.CandidateProfile;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchQueryServiceTest {

    private static final long CALLER = 10001L;

    @Mock
    private MatchManager matchManager;

    private MockCandidateClient candidateClient;
    private MatchQueryService matchQueryService;

    @BeforeEach
    void setUp() {
        candidateClient = new MockCandidateClient();
        matchQueryService = new MatchQueryService(matchManager, candidateClient);
    }

    @Test
    void listMatches_callerAsLow_shouldResolvePartnerHigh() {
        MatchEntity entity = match(90001L, 1L, CALLER, 20002L, "SWIPE_MATCH", hoursAgo(1));
        when(matchManager.listByUserId(CALLER, 20, "")).thenReturn(List.of(entity));

        var result = matchQueryService.listMatches(CALLER, 0, "");
        assertEquals(1, result.getMatches().size());
        assertEquals(90001L, result.getMatches().get(0).getMatchId());
        assertEquals(20002L, result.getMatches().get(0).getPartnerUserId());
        assertEquals("BH-20002", result.getMatches().get(0).getPartnerNickname());
    }

    @Test
    void listMatches_callerAsHigh_shouldResolvePartnerLow() {
        MatchEntity entity = match(90002L, 2L, 20001L, CALLER, "SUPER_HI", hoursAgo(2));
        when(matchManager.listByUserId(CALLER, 20, "")).thenReturn(List.of(entity));

        var result = matchQueryService.listMatches(CALLER, 0, "");
        assertEquals(20001L, result.getMatches().get(0).getPartnerUserId());
        assertEquals(90002L, result.getMatches().get(0).getMatchId());
        assertTrue(result.getMatches().get(0).getMatchId() != 2L);
    }

    @Test
    void listMatches_shouldReturnBizIdNotPhysicalId() {
        MatchEntity entity = match(88888L, 999L, CALLER, 20003L, "SWIPE_MATCH", hoursAgo(1));
        when(matchManager.listByUserId(CALLER, 20, "")).thenReturn(List.of(entity));

        var result = matchQueryService.listMatches(CALLER, 0, "");
        assertEquals(88888L, result.getMatches().get(0).getMatchId());
    }

    @Test
    void listMatches_shouldPreserveManagerOrder() {
        MatchEntity newer = match(90010L, 10L, CALLER, 20004L, "SWIPE_MATCH", hoursAgo(1));
        MatchEntity older = match(90011L, 11L, CALLER, 20005L, "SWIPE_MATCH", hoursAgo(5));
        when(matchManager.listByUserId(CALLER, 20, "")).thenReturn(List.of(newer, older));

        var result = matchQueryService.listMatches(CALLER, 0, "");
        assertEquals(90010L, result.getMatches().get(0).getMatchId());
        assertEquals(90011L, result.getMatches().get(1).getMatchId());
        assertTrue(result.getMatches().get(0).getMatchedAtMs() > result.getMatches().get(1).getMatchedAtMs());
    }

    @Test
    void listMatches_shouldCapPageSizeAt50() {
        when(matchManager.listByUserId(eq(CALLER), eq(50), any())).thenReturn(List.of());
        matchQueryService.listMatches(CALLER, 100, "");
    }

    @Test
    void listMatches_shouldEnrichPartnerProfileFromMockClient() {
        long partnerId = 30010L;
        CandidateProfile custom = new CandidateProfile();
        custom.setUserId(partnerId);
        custom.setNickname("CustomNick");
        custom.setPhotoKeys(List.of("photo/custom.jpg"));
        candidateClient.putProfile(custom);

        MatchEntity entity = match(90020L, 20L, CALLER, partnerId, "SWIPE_MATCH", hoursAgo(1));
        when(matchManager.listByUserId(CALLER, 20, "")).thenReturn(List.of(entity));

        var result = matchQueryService.listMatches(CALLER, 0, "");
        assertEquals("CustomNick", result.getMatches().get(0).getPartnerNickname());
        assertEquals(List.of("photo/custom.jpg"), result.getMatches().get(0).getPartnerPhotoKeys());
    }

    @Test
    void listMatches_invalidCaller_shouldReject() {
        MatchBizException ex = assertThrows(MatchBizException.class,
                () -> matchQueryService.listMatches(0, 20, ""));
        assertEquals(MatchErrorCode.INVALID_ARGUMENT, ex.getErrorCode());
    }

    private static MatchEntity match(long bizId, long physicalId, long low, long high, String source,
                                     OffsetDateTime matchedAt) {
        MatchEntity entity = new MatchEntity();
        entity.setBizId(bizId);
        entity.setId(physicalId);
        entity.setUserIdLow(low);
        entity.setUserIdHigh(high);
        entity.setSource(source);
        entity.setMatchedAt(matchedAt);
        return entity;
    }

    private static OffsetDateTime hoursAgo(int hours) {
        return OffsetDateTime.now(ZoneOffset.UTC).minusHours(hours);
    }
}
