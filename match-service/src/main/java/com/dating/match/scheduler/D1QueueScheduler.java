package com.dating.match.scheduler;

import com.dating.match.constant.RedisKeyConstants;
import com.dating.match.manager.SwipeHistoryManager;
import com.dating.match.recommend.D1Generator;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * D1 日更队列定时调度：UTC 07:00 触发，Redisson 分布式锁防多实例重复生成。
 */
@Component
@Profile("!test")
public class D1QueueScheduler {

    private static final Logger log = LoggerFactory.getLogger(D1QueueScheduler.class);
    private static final int USER_BATCH_LIMIT = 10000;
    private static final long LOCK_WAIT_SECONDS = 0L;
    private static final long LOCK_LEASE_SECONDS = 3600L;

    private final RedissonClient redissonClient;
    private final SwipeHistoryManager swipeHistoryManager;
    private final D1Generator d1Generator;

    public D1QueueScheduler(RedissonClient redissonClient,
                            SwipeHistoryManager swipeHistoryManager,
                            D1Generator d1Generator) {
        this.redissonClient = redissonClient;
        this.swipeHistoryManager = swipeHistoryManager;
        this.d1Generator = d1Generator;
    }

    @Scheduled(cron = "0 0 7 * * *", zone = "UTC")
    public void scheduleDailyD1() {
        runOnce(LocalDate.now(ZoneOffset.UTC));
    }

    public D1RunStats runOnce(LocalDate referenceDate) {
        String lockKey = RedisKeyConstants.d1LockKey(referenceDate);
        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = false;
        try {
            acquired = lock.tryLock(LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
            if (!acquired) {
                log.info("D1 scheduler skipped, lock not acquired key={}", lockKey);
                return new D1RunStats(0, 0, 0, 0);
            }
            return executeGeneration(referenceDate);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("D1 scheduler interrupted key={}", lockKey);
            return new D1RunStats(0, 0, 0, 0);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private D1RunStats executeGeneration(LocalDate referenceDate) {
        LocalDate yesterday = referenceDate.minusDays(1);
        List<Long> userIds = swipeHistoryManager.listUsersWithSwipeYesterday(yesterday, USER_BATCH_LIMIT);
        int success = 0;
        int skipped = 0;
        int failed = 0;
        for (Long userId : userIds) {
            try {
                int written = d1Generator.generateForUser(userId, referenceDate);
                if (written > 0) {
                    success++;
                } else {
                    skipped++;
                }
            } catch (Exception ex) {
                failed++;
                log.error("D1 generation failed userId={}", userId, ex);
            }
        }
        log.info("D1 scheduler finished date={} total={} success={} skipped={} failed={}",
                referenceDate, userIds.size(), success, skipped, failed);
        return new D1RunStats(userIds.size(), success, skipped, failed);
    }

    public record D1RunStats(int total, int success, int skipped, int failed) {
    }
}
