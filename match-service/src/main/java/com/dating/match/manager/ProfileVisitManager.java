package com.dating.match.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dating.match.entity.ProfileVisitEntity;
import com.dating.match.mapper.ProfileVisitMapper;
import com.dating.match.service.support.BusinessIdGenerator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import com.dating.match.service.support.PageTokenCodec;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

/**
 * 主页访问 Manager，仅封装 profile_visit 单表操作。
 */
@Component
@Profile("!test")
public class ProfileVisitManager {

    private final ProfileVisitMapper profileVisitMapper;
    private final BusinessIdGenerator businessIdGenerator;

    public ProfileVisitManager(ProfileVisitMapper profileVisitMapper,
                               BusinessIdGenerator businessIdGenerator) {
        this.profileVisitMapper = profileVisitMapper;
        this.businessIdGenerator = businessIdGenerator;
    }

    /**
     * UPSERT 访问记录：重复访问累加 visit_count，更新 last_visited_at。
     */
    public void upsertVisit(Long fromUserId, Long targetUserId) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        profileVisitMapper.upsertVisit(businessIdGenerator.nextId(), fromUserId, targetUserId, now);
    }

    public List<ProfileVisitEntity> listVisitors(Long targetUserId, int pageSize, String pageToken) {
        if (targetUserId == null || pageSize <= 0) {
            return Collections.emptyList();
        }
        OffsetDateTime cursorTime = null;
        Long cursorBizId = null;
        PageTokenCodec.Cursor cursor = PageTokenCodec.decode(pageToken);
        if (cursor != null) {
            cursorTime = cursor.time();
            cursorBizId = cursor.bizId();
        }
        return profileVisitMapper.listVisitors(targetUserId, pageSize, cursorTime, cursorBizId);
    }

    public ProfileVisitEntity findByPair(Long fromUserId, Long targetUserId) {
        LambdaQueryWrapper<ProfileVisitEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProfileVisitEntity::getFromUserId, fromUserId)
                .eq(ProfileVisitEntity::getTargetUserId, targetUserId);
        return profileVisitMapper.selectOne(wrapper);
    }
}
