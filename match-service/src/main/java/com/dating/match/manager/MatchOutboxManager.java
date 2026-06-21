package com.dating.match.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.dating.match.entity.MatchOutboxEntity;
import com.dating.match.mapper.MatchOutboxMapper;
import com.dating.match.service.support.BusinessIdGenerator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * match outbox Manager，仅封装 match_outbox 单表操作；本阶段不实现 retry 调度。
 */
@Component
@Profile("!test")
public class MatchOutboxManager {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_DONE = "DONE";
    public static final String STATUS_DEAD = "DEAD";

    private final MatchOutboxMapper matchOutboxMapper;
    private final BusinessIdGenerator businessIdGenerator;

    public MatchOutboxManager(MatchOutboxMapper matchOutboxMapper,
                              BusinessIdGenerator businessIdGenerator) {
        this.matchOutboxMapper = matchOutboxMapper;
        this.businessIdGenerator = businessIdGenerator;
    }

    public long countByMatchBizId(Long matchBizId) {
        LambdaQueryWrapper<MatchOutboxEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MatchOutboxEntity::getMatchBizId, matchBizId);
        return matchOutboxMapper.selectCount(wrapper);
    }

    public MatchOutboxEntity createPending(Long matchBizId, String action, String payloadJson, Instant nextRetryAt) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime retryAt = nextRetryAt == null ? now : OffsetDateTime.ofInstant(nextRetryAt, ZoneOffset.UTC);
        MatchOutboxEntity entity = new MatchOutboxEntity();
        entity.setBizId(businessIdGenerator.nextId());
        entity.setMatchBizId(matchBizId);
        entity.setAction(action);
        entity.setPayloadJson(payloadJson);
        entity.setAttempts(0);
        entity.setNextRetryAt(retryAt);
        entity.setStatus(STATUS_PENDING);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setDeleted(0);
        matchOutboxMapper.insert(entity);
        return entity;
    }

    public List<MatchOutboxEntity> listPendingForRetry(int limit) {
        LambdaQueryWrapper<MatchOutboxEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MatchOutboxEntity::getStatus, STATUS_PENDING)
                .le(MatchOutboxEntity::getNextRetryAt, OffsetDateTime.now(ZoneOffset.UTC))
                .orderByAsc(MatchOutboxEntity::getNextRetryAt)
                .last("LIMIT " + Math.max(limit, 1));
        return matchOutboxMapper.selectList(wrapper);
    }

    public void markDone(Long id) {
        updateStatus(id, STATUS_DONE);
    }

    public void markDead(Long id) {
        updateStatus(id, STATUS_DEAD);
    }

    public void increaseAttemptsAndDelay(Long id, Instant nextRetryAt) {
        OffsetDateTime retryAt = OffsetDateTime.ofInstant(nextRetryAt, ZoneOffset.UTC);
        LambdaUpdateWrapper<MatchOutboxEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(MatchOutboxEntity::getId, id)
                .setSql("attempts = attempts + 1")
                .set(MatchOutboxEntity::getNextRetryAt, retryAt)
                .set(MatchOutboxEntity::getUpdatedAt, OffsetDateTime.now(ZoneOffset.UTC));
        matchOutboxMapper.update(null, wrapper);
    }

    private void updateStatus(Long id, String status) {
        LambdaUpdateWrapper<MatchOutboxEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(MatchOutboxEntity::getId, id)
                .set(MatchOutboxEntity::getStatus, status)
                .set(MatchOutboxEntity::getUpdatedAt, OffsetDateTime.now(ZoneOffset.UTC));
        matchOutboxMapper.update(null, wrapper);
    }
}
