package com.dating.gateway.adapter;

import com.dating.gateway.dto.vo.MatchCardVO;
import com.dating.gateway.dto.vo.MatchFeedVO;
import com.dating.gateway.dto.vo.MatchInfoVO;
import com.dating.gateway.dto.vo.MatchListVO;
import com.dating.gateway.dto.vo.MatchQuotaVO;
import com.dating.gateway.dto.vo.SuperHiResultVO;
import com.dating.gateway.dto.vo.SwipeResultVO;
import com.dating.gateway.dto.vo.VisitInfoVO;
import com.dating.gateway.dto.vo.VisitListVO;
import com.dating.match.grpc.proto.GetQuotaResp;
import com.dating.match.grpc.proto.GetTodayFeedResp;
import com.dating.match.grpc.proto.ListMatchesResp;
import com.dating.match.grpc.proto.ListVisitsResp;
import com.dating.match.grpc.proto.MatchCard;
import com.dating.match.grpc.proto.MatchInfo;
import com.dating.match.grpc.proto.SuperHiResp;
import com.dating.match.grpc.proto.SwipeDirection;
import com.dating.match.grpc.proto.SwipeResp;
import com.dating.match.grpc.proto.VisitInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * match gRPC proto 与 gateway Swagger VO 转换器，不含业务逻辑。
 */
public final class MatchProtoAdapter {

    private MatchProtoAdapter() {
    }

    public static MatchFeedVO toMatchFeedVO(GetTodayFeedResp resp) {
        MatchFeedVO vo = new MatchFeedVO();
        vo.setExhausted(resp.getExhausted());
        List<MatchCardVO> cards = new ArrayList<>();
        for (MatchCard card : resp.getCardsList()) {
            cards.add(toMatchCardVO(card));
        }
        vo.setCards(cards);
        return vo;
    }

    public static MatchCardVO toMatchCardVO(MatchCard card) {
        MatchCardVO vo = new MatchCardVO();
        vo.setTargetUserId(card.getTargetUserId());
        vo.setTargetUserType(card.getTargetUserType());
        vo.setNickname(card.getNickname());
        vo.setAge(card.getAge());
        vo.setPhotoKeys(new ArrayList<>(card.getPhotoKeysList()));
        vo.setBio(card.getBio());
        vo.setDistanceKm(card.getDistanceKm());
        vo.setStateCode(card.getStateCode());
        vo.setCity(card.getCity());
        return vo;
    }

    public static SwipeResultVO toSwipeResultVO(SwipeResp resp) {
        SwipeResultVO vo = new SwipeResultVO();
        long matchId = resp.getMatchId();
        vo.setMatchId(matchId > 0 ? matchId : null);
        return vo;
    }

    public static SuperHiResultVO toSuperHiResultVO(SuperHiResp resp) {
        SuperHiResultVO vo = new SuperHiResultVO();
        long matchId = resp.getMatchId();
        vo.setMatchId(matchId > 0 ? matchId : null);
        vo.setCoinsUsed(resp.getCoinsUsed());
        return vo;
    }

    public static MatchQuotaVO toMatchQuotaVO(GetQuotaResp resp) {
        MatchQuotaVO vo = new MatchQuotaVO();
        vo.setTier(resp.getTier());
        vo.setDailyRightSwipeLimit(resp.getDailyRightSwipeLimit());
        vo.setDailyRightSwipeUsed(resp.getDailyRightSwipeUsed());
        vo.setDailyCardLimit(resp.getDailyCardLimit());
        vo.setDailyCardUsed(resp.getDailyCardUsed());
        vo.setDailySuperHiLimit(resp.getDailySuperHiLimit());
        vo.setDailySuperHiUsed(resp.getDailySuperHiUsed());
        vo.setSuperHiCoinPrice(resp.getSuperHiCoinPrice());
        return vo;
    }

    public static MatchListVO toMatchListVO(ListMatchesResp resp) {
        MatchListVO vo = new MatchListVO();
        vo.setNextPageToken(resp.getNextPageToken());
        List<MatchInfoVO> matches = new ArrayList<>();
        for (MatchInfo info : resp.getMatchesList()) {
            matches.add(toMatchInfoVO(info));
        }
        vo.setMatches(matches);
        return vo;
    }

    public static MatchInfoVO toMatchInfoVO(MatchInfo info) {
        MatchInfoVO vo = new MatchInfoVO();
        vo.setMatchId(info.getMatchId());
        vo.setPartnerUserId(info.getPartnerUserId());
        vo.setPartnerNickname(info.getPartnerNickname());
        vo.setPartnerPhotoKeys(new ArrayList<>(info.getPartnerPhotoKeysList()));
        vo.setMatchedAtMs(info.getMatchedAtMs());
        vo.setSource(info.getSource());
        return vo;
    }

    public static VisitListVO toVisitListVO(ListVisitsResp resp) {
        VisitListVO vo = new VisitListVO();
        vo.setNextPageToken(resp.getNextPageToken());
        List<VisitInfoVO> visits = new ArrayList<>();
        for (VisitInfo info : resp.getVisitsList()) {
            visits.add(toVisitInfoVO(info));
        }
        vo.setVisits(visits);
        return vo;
    }

    public static VisitInfoVO toVisitInfoVO(VisitInfo info) {
        VisitInfoVO vo = new VisitInfoVO();
        vo.setVisitId(info.getVisitId());
        vo.setFromUserId(info.getFromUserId());
        vo.setVisitCount(info.getVisitCount());
        vo.setFirstVisitedAtMs(info.getFirstVisitedAtMs());
        vo.setLastVisitedAtMs(info.getLastVisitedAtMs());
        return vo;
    }

    public static SwipeDirection toSwipeDirection(String direction) {
        if (direction == null) {
            return SwipeDirection.SWIPE_DIRECTION_UNSPECIFIED;
        }
        return switch (direction.trim().toUpperCase()) {
            case "LEFT" -> SwipeDirection.LEFT;
            case "RIGHT" -> SwipeDirection.RIGHT;
            default -> SwipeDirection.SWIPE_DIRECTION_UNSPECIFIED;
        };
    }
}
