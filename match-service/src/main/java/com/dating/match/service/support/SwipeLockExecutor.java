package com.dating.match.service.support;

import com.dating.match.exception.MatchBizException;
import com.dating.match.exception.MatchErrorCode;

import java.util.function.Supplier;

/**
 * Swipe 分布式锁执行器，Redisson 实现并发串行化；锁不能替代 DB 唯一约束。
 */
public interface SwipeLockExecutor {

    <T> T executeWithLock(long userId, long targetUserId, Supplier<T> action);

    default void runWithLock(long userId, long targetUserId, Runnable action) {
        executeWithLock(userId, targetUserId, () -> {
            action.run();
            return null;
        });
    }

    static MatchBizException concurrentSwipe() {
        return new MatchBizException(MatchErrorCode.CONCURRENT_SWIPE);
    }
}
