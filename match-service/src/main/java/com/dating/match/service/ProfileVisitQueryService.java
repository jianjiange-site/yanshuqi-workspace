package com.dating.match.service;

import com.dating.match.dto.ListVisitsResult;
import com.dating.match.dto.VisitInfoDto;
import com.dating.match.entity.ProfileVisitEntity;
import com.dating.match.exception.MatchBizException;
import com.dating.match.exception.MatchErrorCode;
import com.dating.match.manager.ProfileVisitManager;
import com.dating.match.service.support.PageTokenCodec;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 访问列表查询：仅查 profile_visit 单表。
 */
@Service
@Profile("!test")
public class ProfileVisitQueryService {

    private final ProfileVisitManager profileVisitManager;

    public ProfileVisitQueryService(ProfileVisitManager profileVisitManager) {
        this.profileVisitManager = profileVisitManager;
    }

    public ListVisitsResult listVisits(long callerUserId, int pageSize, String pageToken) {
        if (callerUserId <= 0) {
            throw new MatchBizException(MatchErrorCode.INVALID_ARGUMENT);
        }
        int normalizedSize = MatchQueryService.normalizePageSize(pageSize);
        List<ProfileVisitEntity> entities = profileVisitManager.listVisitors(callerUserId, normalizedSize, pageToken);
        if (entities.isEmpty()) {
            return new ListVisitsResult(List.of(), "");
        }

        List<VisitInfoDto> visits = new ArrayList<>(entities.size());
        for (ProfileVisitEntity entity : entities) {
            VisitInfoDto dto = new VisitInfoDto();
            dto.setVisitId(entity.getBizId());
            dto.setFromUserId(entity.getFromUserId());
            dto.setVisitCount(entity.getVisitCount());
            dto.setFirstVisitedAtMs(entity.getFirstVisitedAt().toInstant().toEpochMilli());
            dto.setLastVisitedAtMs(entity.getLastVisitedAt().toInstant().toEpochMilli());
            visits.add(dto);
        }

        String nextPageToken = "";
        if (entities.size() == normalizedSize) {
            ProfileVisitEntity last = entities.get(entities.size() - 1);
            nextPageToken = PageTokenCodec.encode(last.getLastVisitedAt(), last.getBizId());
        }
        return new ListVisitsResult(visits, nextPageToken);
    }
}
