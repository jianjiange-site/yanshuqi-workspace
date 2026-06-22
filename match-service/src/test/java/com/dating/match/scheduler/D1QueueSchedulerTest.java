package com.dating.match.scheduler;

import com.dating.match.constant.RedisKeyConstants;
import com.dating.match.manager.SwipeHistoryManager;
import com.dating.match.recommend.D1Generator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class D1QueueSchedulerTest {

    @Mock
    private RedissonClient redissonClient;
    @Mock
    private SwipeHistoryManager swipeHistoryManager;
    @Mock
    private D1Generator d1Generator;
    @Mock
    private RLock lock;

    private D1QueueScheduler scheduler;
    private LocalDate referenceDate;

    @BeforeEach
    void setUp() {
        scheduler = new D1QueueScheduler(redissonClient, swipeHistoryManager, d1Generator);
        referenceDate = LocalDate.of(2026, 6, 22);
    }

    @Test
    void runOnce_shouldExecuteWhenLockAcquired() throws Exception {
        when(redissonClient.getLock(RedisKeyConstants.d1LockKey(referenceDate))).thenReturn(lock);
        when(lock.tryLock(0L, 3600L, TimeUnit.SECONDS)).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);
        when(swipeHistoryManager.listUsersWithSwipeYesterday(referenceDate.minusDays(1), 10000))
                .thenReturn(List.of(10001L, 10002L));
        when(d1Generator.generateForUser(10001L, referenceDate)).thenReturn(240);
        when(d1Generator.generateForUser(10002L, referenceDate)).thenReturn(200);

        D1QueueScheduler.D1RunStats stats = scheduler.runOnce(referenceDate);
        assertEquals(2, stats.total());
        assertEquals(2, stats.success());
        verify(lock).unlock();
    }

    @Test
    void runOnce_shouldSkipWhenLockNotAcquired() throws Exception {
        when(redissonClient.getLock(RedisKeyConstants.d1LockKey(referenceDate))).thenReturn(lock);
        when(lock.tryLock(0L, 3600L, TimeUnit.SECONDS)).thenReturn(false);

        D1QueueScheduler.D1RunStats stats = scheduler.runOnce(referenceDate);
        assertEquals(0, stats.total());
        verify(d1Generator, never()).generateForUser(anyLong(), any());
        verify(lock, never()).unlock();
    }

    @Test
    void runOnce_shouldContinueWhenSingleUserFails() throws Exception {
        when(redissonClient.getLock(RedisKeyConstants.d1LockKey(referenceDate))).thenReturn(lock);
        when(lock.tryLock(0L, 3600L, TimeUnit.SECONDS)).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);
        when(swipeHistoryManager.listUsersWithSwipeYesterday(referenceDate.minusDays(1), 10000))
                .thenReturn(List.of(10001L, 10002L));
        doThrow(new RuntimeException("db error")).when(d1Generator).generateForUser(10001L, referenceDate);
        when(d1Generator.generateForUser(10002L, referenceDate)).thenReturn(240);

        D1QueueScheduler.D1RunStats stats = scheduler.runOnce(referenceDate);
        assertEquals(2, stats.total());
        assertEquals(1, stats.success());
        assertEquals(1, stats.failed());
    }

    @Test
    void runOnce_shouldCountSkippedWhenGeneratorReturnsZero() throws Exception {
        when(redissonClient.getLock(RedisKeyConstants.d1LockKey(referenceDate))).thenReturn(lock);
        when(lock.tryLock(0L, 3600L, TimeUnit.SECONDS)).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);
        when(swipeHistoryManager.listUsersWithSwipeYesterday(referenceDate.minusDays(1), 10000))
                .thenReturn(List.of(10001L));
        when(d1Generator.generateForUser(10001L, referenceDate)).thenReturn(0);

        D1QueueScheduler.D1RunStats stats = scheduler.runOnce(referenceDate);
        assertEquals(1, stats.total());
        assertEquals(1, stats.skipped());
    }
}
