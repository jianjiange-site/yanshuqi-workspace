package com.dating.match.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dating.match.constant.SwipeDirectionConstant;
import com.dating.match.entity.UserSwipeHistoryEntity;
import com.dating.match.mapper.UserSwipeHistoryMapper;
import com.dating.match.service.support.BusinessIdGenerator;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

/**
 * 划卡历史 Manager，仅封装 user_swipe_history 单表操作。
 */
@Component
@Profile("!test")
public class SwipeHistoryManager {

    private final UserSwipeHistoryMapper userSwipeHistoryMapper;
    private final BusinessIdGenerator businessIdGenerator;

    public SwipeHistoryManager(UserSwipeHistoryMapper userSwipeHistoryMapper,
                               BusinessIdGenerator businessIdGenerator) {
        this.userSwipeHistoryMapper = userSwipeHistoryMapper;
        this.businessIdGenerator = businessIdGenerator;
    }

    public boolean existsByUserIdAndTargetUserId(Long userId, Long targetUserId) {
        return findByUserIdAndTargetUserId(userId, targetUserId).isPresent();
    }

    public Optional<UserSwipeHistoryEntity> findByUserIdAndTargetUserId(Long userId, Long targetUserId) {
        if (userId == null || targetUserId == null) {
            return Optional.empty();
        }
        LambdaQueryWrapper<UserSwipeHistoryEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSwipeHistoryEntity::getUserId, userId)
                .eq(UserSwipeHistoryEntity::getTargetUserId, targetUserId);
        return Optional.ofNullable(userSwipeHistoryMapper.selectOne(wrapper));
    }

    /**
     * 是否对 target 做过 RIGHT 或 SUPER_HI（LEFT 不算正向喜欢）。
     */
    public boolean hasPositiveSwipe(Long fromUserId, Long targetUserId) {
        return findByUserIdAndTargetUserId(fromUserId, targetUserId)
                .map(entity -> entity.getDirection() == SwipeDirectionConstant.RIGHT
                        || entity.getDirection() == SwipeDirectionConstant.SUPER_HI)
                .orElse(false);
    }

    /**
     * 幂等插入划卡记录，依赖 (user_id, target_user_id) 唯一约束防并发重复。
     */
    public UserSwipeHistoryEntity insertIfAbsent(Long userId,
                                                 Long targetUserId,
                                                 Integer targetUserType,
                                                 Integer direction,
                                                 OffsetDateTime swipedAt) {
        Optional<UserSwipeHistoryEntity> existing = findByUserIdAndTargetUserId(userId, targetUserId);
        if (existing.isPresent()) {
            return existing.get();
        }
        OffsetDateTime now = swipedAt == null ? OffsetDateTime.now(ZoneOffset.UTC) : swipedAt;
        UserSwipeHistoryEntity entity = new UserSwipeHistoryEntity();
        entity.setBizId(businessIdGenerator.nextId());
        entity.setUserId(userId);
        entity.setTargetUserId(targetUserId);
        entity.setTargetUserType(targetUserType);
        entity.setDirection(direction);
        entity.setSwipedAt(now);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setDeleted(0);
        try {
            userSwipeHistoryMapper.insert(entity);
            return entity;
        } catch (DuplicateKeyException ex) {
            return findByUserIdAndTargetUserId(userId, targetUserId)
                    .orElseThrow(() -> ex);
        }
    }
}
