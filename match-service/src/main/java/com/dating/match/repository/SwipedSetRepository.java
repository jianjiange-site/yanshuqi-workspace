package com.dating.match.repository;

/**
 * 已划用户 SET 抽象。
 */
public interface SwipedSetRepository {

    void addSwiped(long userId, long targetUserId);
}
