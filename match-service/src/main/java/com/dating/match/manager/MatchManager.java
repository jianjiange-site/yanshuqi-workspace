package com.dating.match.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dating.match.dto.MatchInsertResult;
import com.dating.match.entity.MatchEntity;
import com.dating.match.mapper.MatchMapper;
import com.dating.match.service.support.BusinessIdGenerator;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

/**
 * 匹配关系 Manager，仅封装 match 单表操作。
 * <p>
 * 通过 user_id_low / user_id_high 规范化用户对，保证 A-B 与 B-A 落到同一条记录，避免重复匹配。
 */
@Component
@Profile("!test")
public class MatchManager {

    private final MatchMapper matchMapper;
    private final BusinessIdGenerator businessIdGenerator;

    public MatchManager(MatchMapper matchMapper, BusinessIdGenerator businessIdGenerator) {
        this.matchMapper = matchMapper;
        this.businessIdGenerator = businessIdGenerator;
    }

    public Optional<MatchEntity> findByPair(Long userIdA, Long userIdB) {
        long[] pair = normalizePair(userIdA, userIdB);
        LambdaQueryWrapper<MatchEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MatchEntity::getUserIdLow, pair[0])
                .eq(MatchEntity::getUserIdHigh, pair[1]);
        return Optional.ofNullable(matchMapper.selectOne(wrapper));
    }

    /**
     * 幂等创建匹配关系，返回是否新创建。
     */
    public MatchInsertResult insertIfAbsentWithResult(Long userIdA, Long userIdB, String source) {
        Optional<MatchEntity> existing = findByPair(userIdA, userIdB);
        if (existing.isPresent()) {
            return new MatchInsertResult(existing.get(), false);
        }
        long[] pair = normalizePair(userIdA, userIdB);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        MatchEntity entity = new MatchEntity();
        entity.setBizId(businessIdGenerator.nextId());
        entity.setUserIdLow(pair[0]);
        entity.setUserIdHigh(pair[1]);
        entity.setMatchedAt(now);
        entity.setSource(source);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setDeleted(0);
        try {
            matchMapper.insert(entity);
            return new MatchInsertResult(entity, true);
        } catch (DuplicateKeyException ex) {
            MatchEntity dup = findByPair(userIdA, userIdB).orElseThrow(() -> ex);
            return new MatchInsertResult(dup, false);
        }
    }

    /**
     * 幂等创建匹配关系，重复 pair 返回已有记录。
     */
    public MatchEntity insertIfAbsent(Long userIdA, Long userIdB, String source) {
        return insertIfAbsentWithResult(userIdA, userIdB, source).getEntity();
    }

    static long[] normalizePair(Long userIdA, Long userIdB) {
        if (userIdA == null || userIdB == null) {
            throw new IllegalArgumentException("userId 不能为空");
        }
        long low = Math.min(userIdA, userIdB);
        long high = Math.max(userIdA, userIdB);
        return new long[]{low, high};
    }
}
