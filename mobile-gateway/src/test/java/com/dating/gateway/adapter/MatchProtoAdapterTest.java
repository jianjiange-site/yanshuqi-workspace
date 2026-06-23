package com.dating.gateway.adapter;

import com.dating.gateway.dto.vo.MatchCardVO;
import com.dating.gateway.dto.vo.MatchFeedVO;
import com.dating.gateway.dto.vo.MatchInfoVO;
import com.dating.gateway.dto.vo.MatchListVO;
import com.dating.gateway.dto.vo.MatchQuotaVO;
import com.dating.gateway.dto.vo.VisitInfoVO;
import com.dating.gateway.dto.vo.VisitListVO;
import com.dating.match.grpc.proto.GetQuotaResp;
import com.dating.match.grpc.proto.GetTodayFeedResp;
import com.dating.match.grpc.proto.ListMatchesResp;
import com.dating.match.grpc.proto.ListVisitsResp;
import com.dating.match.grpc.proto.MatchCard;
import com.dating.match.grpc.proto.MatchInfo;
import com.dating.match.grpc.proto.VisitInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatchProtoAdapterTest {

    @Test
    void toMatchCardVO_shouldMapAllSwaggerFields() {
        MatchCard card = MatchCard.newBuilder()
                .setTargetUserId(10002L)
                .setTargetUserType(1)
                .setNickname("Alice")
                .setAge(25)
                .addPhotoKeys("photo1.jpg")
                .setBio("hello")
                .setDistanceKm(3.5)
                .setStateCode("CA")
                .setCity("Tokyo")
                .build();

        MatchCardVO vo = MatchProtoAdapter.toMatchCardVO(card);

        assertEquals(10002L, vo.getTargetUserId());
        assertEquals(1, vo.getTargetUserType());
        assertEquals("Alice", vo.getNickname());
        assertEquals(25, vo.getAge());
        assertEquals(1, vo.getPhotoKeys().size());
        assertEquals("hello", vo.getBio());
        assertEquals(3.5, vo.getDistanceKm());
        assertEquals("CA", vo.getStateCode());
        assertEquals("Tokyo", vo.getCity());
    }

    @Test
    void toMatchQuotaVO_shouldMapAllFields() {
        GetQuotaResp resp = GetQuotaResp.newBuilder()
                .setTier("FREE")
                .setDailyRightSwipeLimit(10)
                .setDailyRightSwipeUsed(2)
                .setDailyCardLimit(20)
                .setDailyCardUsed(5)
                .setDailySuperHiLimit(1)
                .setDailySuperHiUsed(0)
                .setSuperHiCoinPrice(100)
                .build();

        MatchQuotaVO vo = MatchProtoAdapter.toMatchQuotaVO(resp);

        assertEquals("FREE", vo.getTier());
        assertEquals(10, vo.getDailyRightSwipeLimit());
        assertEquals(2, vo.getDailyRightSwipeUsed());
        assertEquals(20, vo.getDailyCardLimit());
        assertEquals(5, vo.getDailyCardUsed());
        assertEquals(1, vo.getDailySuperHiLimit());
        assertEquals(0, vo.getDailySuperHiUsed());
        assertEquals(100, vo.getSuperHiCoinPrice());
    }

    @Test
    void toMatchInfoVO_shouldMapPartnerFields() {
        MatchInfo info = MatchInfo.newBuilder()
                .setMatchId(9001L)
                .setPartnerUserId(10003L)
                .setPartnerNickname("Bob")
                .addPartnerPhotoKeys("p1.jpg")
                .setMatchedAtMs(1700000000000L)
                .setSource("SWIPE")
                .build();

        MatchInfoVO vo = MatchProtoAdapter.toMatchInfoVO(info);

        assertEquals(9001L, vo.getMatchId());
        assertEquals(10003L, vo.getPartnerUserId());
        assertEquals("Bob", vo.getPartnerNickname());
        assertEquals(1, vo.getPartnerPhotoKeys().size());
        assertEquals(1700000000000L, vo.getMatchedAtMs());
        assertEquals("SWIPE", vo.getSource());
    }

    @Test
    void toVisitInfoVO_shouldMapVisitFields() {
        VisitInfo info = VisitInfo.newBuilder()
                .setVisitId(1L)
                .setFromUserId(10004L)
                .setVisitCount(3)
                .setFirstVisitedAtMs(1000L)
                .setLastVisitedAtMs(2000L)
                .build();

        VisitInfoVO vo = MatchProtoAdapter.toVisitInfoVO(info);

        assertEquals(1L, vo.getVisitId());
        assertEquals(10004L, vo.getFromUserId());
        assertEquals(3, vo.getVisitCount());
        assertEquals(1000L, vo.getFirstVisitedAtMs());
        assertEquals(2000L, vo.getLastVisitedAtMs());
    }

    @Test
    void emptyListMapping_shouldNotNpe() {
        MatchFeedVO feed = MatchProtoAdapter.toMatchFeedVO(
                GetTodayFeedResp.newBuilder().setExhausted(true).build());
        assertTrue(feed.getCards().isEmpty());
        assertTrue(feed.getExhausted());

        MatchListVO matches = MatchProtoAdapter.toMatchListVO(ListMatchesResp.getDefaultInstance());
        assertNotNull(matches.getMatches());
        assertTrue(matches.getMatches().isEmpty());

        VisitListVO visits = MatchProtoAdapter.toVisitListVO(null);
        assertNotNull(visits.getVisits());
        assertTrue(visits.getVisits().isEmpty());
    }
}
