package com.dating.match.support;

import com.dating.match.service.support.SwipeLockExecutor;

import java.util.function.Supplier;

/**
 * 单测用锁：直接执行，不阻塞。
 */
public class FakeSwipeLockExecutor implements SwipeLockExecutor {

    private boolean lockAvailable = true;

    public void setLockAvailable(boolean lockAvailable) {
        this.lockAvailable = lockAvailable;
    }

    @Override
    public <T> T executeWithLock(long userId, long targetUserId, Supplier<T> action) {
        if (!lockAvailable) {
            throw SwipeLockExecutor.concurrentSwipe();
        }
        return action.get();
    }
}
