package com.dating.match.service.support;

import com.dating.match.constant.RedisKeyConstants;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redisson 实现的 swipe 锁：waitTime=5s，leaseTime=3s；仅负责并发串行化，幂等仍依赖 DB 唯一约束。
 */
@Component
@Profile("!test")
public class RedissonSwipeLockExecutor implements SwipeLockExecutor {

    private static final long WAIT_SECONDS = 5L;
    private static final long LEASE_SECONDS = 3L;

    private final RedissonClient redissonClient;

    public RedissonSwipeLockExecutor(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public <T> T executeWithLock(long userId, long targetUserId, Supplier<T> action) {
        String lockKey = RedisKeyConstants.swipeLockKey(userId, targetUserId);
        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = false;
        try {
            acquired = lock.tryLock(WAIT_SECONDS, LEASE_SECONDS, TimeUnit.SECONDS);
            if (!acquired) {
                throw SwipeLockExecutor.concurrentSwipe();
            }
            return action.get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw SwipeLockExecutor.concurrentSwipe();
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
