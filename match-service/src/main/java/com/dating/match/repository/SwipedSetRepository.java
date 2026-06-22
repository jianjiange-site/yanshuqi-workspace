package com.dating.match.repository;

import java.util.Collection;
import java.util.Set;

/**
 * 已划用户 SET 抽象。
 */
public interface SwipedSetRepository {

    void addSwiped(long userId, long targetUserId);

    /**
     * 批量查询 target 是否已在 swiped SET 中。
     */
    Set<Long> findSwipedTargets(long userId, Collection<Long> targetUserIds);
}
