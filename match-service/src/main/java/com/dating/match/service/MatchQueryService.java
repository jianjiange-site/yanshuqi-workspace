package com.dating.match.service;

import com.dating.match.client.CandidateClient;
import com.dating.match.dto.ListMatchesResult;
import com.dating.match.dto.MatchInfoDto;
import com.dating.match.entity.MatchEntity;
import com.dating.match.exception.MatchBizException;
import com.dating.match.exception.MatchErrorCode;
import com.dating.match.manager.MatchManager;
import com.dating.match.recommend.CandidateProfile;
import com.dating.match.service.support.PageTokenCodec;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 匹配列表查询：仅查 match 单表，partner 资料通过 mock CandidateClient 补齐。
 */
@Service
@Profile("!test")
public class MatchQueryService {

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 50;

    private final MatchManager matchManager;
    private final CandidateClient candidateClient;

    public MatchQueryService(MatchManager matchManager, CandidateClient candidateClient) {
        this.matchManager = matchManager;
        this.candidateClient = candidateClient;
    }

    public ListMatchesResult listMatches(long callerUserId, int pageSize, String pageToken) {
        if (callerUserId <= 0) {
            throw new MatchBizException(MatchErrorCode.INVALID_ARGUMENT);
        }
        int normalizedSize = normalizePageSize(pageSize);
        List<MatchEntity> entities = matchManager.listByUserId(callerUserId, normalizedSize, pageToken);
        if (entities.isEmpty()) {
            return new ListMatchesResult(List.of(), "");
        }

        Set<Long> partnerIds = new LinkedHashSet<>();
        for (MatchEntity entity : entities) {
            partnerIds.add(MatchManager.resolvePartnerUserId(entity, callerUserId));
        }
        Map<Long, CandidateProfile> profiles = candidateClient.batchGetProfiles(partnerIds);

        List<MatchInfoDto> matches = new ArrayList<>(entities.size());
        for (MatchEntity entity : entities) {
            long partnerUserId = MatchManager.resolvePartnerUserId(entity, callerUserId);
            MatchInfoDto dto = new MatchInfoDto();
            dto.setMatchId(entity.getBizId());
            dto.setPartnerUserId(partnerUserId);
            dto.setMatchedAtMs(entity.getMatchedAt().toInstant().toEpochMilli());
            dto.setSource(entity.getSource());
            CandidateProfile profile = profiles.get(partnerUserId);
            if (profile != null) {
                dto.setPartnerNickname(profile.getNickname());
                dto.setPartnerPhotoKeys(profile.getPhotoKeys());
            } else {
                dto.setPartnerNickname("");
            }
            matches.add(dto);
        }

        String nextPageToken = "";
        if (entities.size() == normalizedSize) {
            MatchEntity last = entities.get(entities.size() - 1);
            nextPageToken = PageTokenCodec.encode(last.getMatchedAt(), last.getBizId());
        }
        return new ListMatchesResult(matches, nextPageToken);
    }

    static int normalizePageSize(int pageSize) {
        if (pageSize <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }
}
