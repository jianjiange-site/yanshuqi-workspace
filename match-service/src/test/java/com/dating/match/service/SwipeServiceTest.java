package com.dating.match.service;

import com.dating.match.config.MatchProperties;
import com.dating.match.client.MockPaymentClient;
import com.dating.match.client.MockSubscriptionClient;
import com.dating.match.client.MockTargetUserTypeResolver;
import com.dating.match.constant.SwipeDirectionConstant;
import com.dating.match.entity.UserSwipeHistoryEntity;
import com.dating.match.exception.MatchBizException;
import com.dating.match.exception.MatchErrorCode;
import com.dating.match.grpc.proto.SwipeDirection;
import com.dating.match.manager.SwipeHistoryManager;
import com.dating.match.support.FakeSwipeLockExecutor;
import com.dating.match.support.InMemoryQuotaHashRepository;
import com.dating.match.support.InMemorySuperHiIdempotencyStore;
import com.dating.match.support.InMemorySwipedSetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SwipeServiceTest {

    private static final long CALLER = 10001L;
    private static final long TARGET = 20002L;

    @Mock
    private SwipeHistoryManager swipeHistoryManager;

    @Mock
    private MatchCreationService matchCreationService;

    @Mock
    private DhDelayedMatchService dhDelayedMatchService;

    private FakeSwipeLockExecutor lockExecutor;
    private InMemorySwipedSetRepository swipedSetRepository;
    private QuotaService quotaService;
    private SwipeService swipeService;

    @BeforeEach
    void setUp() {
        lockExecutor = new FakeSwipeLockExecutor();
        swipedSetRepository = new InMemorySwipedSetRepository();
        quotaService = new QuotaService(
                new InMemoryQuotaHashRepository(),
                new MockSubscriptionClient(),
                new MockPaymentClient(),
                new MatchProperties());
        swipeService = new SwipeService(
                lockExecutor,
                swipeHistoryManager,
                quotaService,
                swipedSetRepository,
                new InMemorySuperHiIdempotencyStore(),
                new MockTargetUserTypeResolver(),
                matchCreationService,
                dhDelayedMatchService);
    }

    private void stubNewSwipe() {
        when(swipeHistoryManager.findByUserIdAndTargetUserId(CALLER, TARGET)).thenReturn(Optional.empty());
        when(swipeHistoryManager.insertIfAbsent(eq(CALLER), eq(TARGET), anyInt(), anyInt(), isNull()))
                .thenAnswer(inv -> {
                    UserSwipeHistoryEntity entity = new UserSwipeHistoryEntity();
                    entity.setBizId(90001L);
                    return entity;
                });
    }

    @Test
    void leftSwipe_shouldPersistHistoryAndSwipedSet() {
        stubNewSwipe();
        swipeService.swipe(CALLER, TARGET, SwipeDirection.LEFT);
        verify(swipeHistoryManager).insertIfAbsent(eq(CALLER), eq(TARGET), eq(1), eq(SwipeDirectionConstant.LEFT), isNull());
        assertEquals(1, swipedSetRepository.size());
    }

    @Test
    void rightSwipe_shouldPersistHistory() {
        stubNewSwipe();
        swipeService.swipe(CALLER, TARGET, SwipeDirection.RIGHT);
        verify(swipeHistoryManager).insertIfAbsent(eq(CALLER), eq(TARGET), eq(1), eq(SwipeDirectionConstant.RIGHT), isNull());
    }

    @Test
    void duplicateSwipe_shouldNotInsertAgain() {
        when(swipeHistoryManager.findByUserIdAndTargetUserId(CALLER, TARGET))
                .thenReturn(Optional.of(new UserSwipeHistoryEntity()));
        swipeService.swipe(CALLER, TARGET, SwipeDirection.LEFT);
        verify(swipeHistoryManager, never()).insertIfAbsent(any(), any(), anyInt(), anyInt(), any(OffsetDateTime.class));
    }

    @Test
    void duplicateSwipe_shouldNotIncreaseQuota() {
        when(swipeHistoryManager.findByUserIdAndTargetUserId(CALLER, TARGET))
                .thenReturn(Optional.of(new UserSwipeHistoryEntity()));
        swipeService.swipe(CALLER, TARGET, SwipeDirection.LEFT);
        assertEquals(0, quotaService.getUsage(CALLER).getCardsUsed());
    }

    @Test
    void sameUserTarget_shouldReject() {
        assertThrows(MatchBizException.class, () -> swipeService.swipe(CALLER, CALLER, SwipeDirection.LEFT));
    }

    @Test
    void invalidDirection_shouldReject() {
        assertThrows(MatchBizException.class,
                () -> swipeService.swipe(CALLER, TARGET, SwipeDirection.SWIPE_DIRECTION_UNSPECIFIED));
    }

    @Test
    void lockUnavailable_shouldThrowConcurrentSwipe() {
        lockExecutor.setLockAvailable(false);
        MatchBizException ex = assertThrows(MatchBizException.class,
                () -> swipeService.swipe(CALLER, TARGET, SwipeDirection.LEFT));
        assertEquals(MatchErrorCode.CONCURRENT_SWIPE, ex.getErrorCode());
    }
}
