package com.dating.match.service;

import com.dating.match.config.MatchProperties;
import com.dating.match.client.MockPaymentClient;
import com.dating.match.client.MockSubscriptionClient;
import com.dating.match.client.MockTargetUserTypeResolver;
import com.dating.match.exception.MatchBizException;
import com.dating.match.exception.MatchErrorCode;
import com.dating.match.manager.SwipeHistoryManager;
import com.dating.match.subscription.SubscriptionTier;
import com.dating.match.support.FakeSwipeLockExecutor;
import com.dating.match.support.InMemoryQuotaHashRepository;
import com.dating.match.support.InMemorySuperHiIdempotencyStore;
import com.dating.match.support.InMemorySwipedSetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuperHiIdempotencyTest {

    private static final long CALLER = 10001L;
    private static final long TARGET = 20002L;

    @Mock
    private SwipeHistoryManager swipeHistoryManager;

    @Mock
    private MatchCreationService matchCreationService;

    @Mock
    private DhDelayedMatchService dhDelayedMatchService;

    private QuotaService quotaService;
    private InMemorySuperHiIdempotencyStore idempotencyStore;
    private SwipeService swipeService;

    @BeforeEach
    void setUp() {
        MockSubscriptionClient subscriptionClient = new MockSubscriptionClient();
        subscriptionClient.setTier(CALLER, SubscriptionTier.FREE);
        quotaService = new QuotaService(new InMemoryQuotaHashRepository(), subscriptionClient, new MockPaymentClient(), new MatchProperties());
        idempotencyStore = new InMemorySuperHiIdempotencyStore();
        swipeService = new SwipeService(
                new FakeSwipeLockExecutor(),
                swipeHistoryManager,
                quotaService,
                new InMemorySwipedSetRepository(),
                idempotencyStore,
                new MockTargetUserTypeResolver(),
                matchCreationService,
                dhDelayedMatchService);
    }

    private void stubNewSwipe() {
        when(swipeHistoryManager.findByUserIdAndTargetUserId(CALLER, TARGET)).thenReturn(Optional.empty());
        when(swipeHistoryManager.insertIfAbsent(any(), any(), anyInt(), anyInt(), isNull()))
                .thenReturn(new com.dating.match.entity.UserSwipeHistoryEntity());
        when(matchCreationService.createMatch(CALLER, TARGET, com.dating.match.constant.MatchSourceConstant.SWIPE_SUPER_HI))
                .thenReturn(0L);
    }

    @Test
    void sameClientRequestId_shouldReturnSameResultWithoutDoubleCharge() {
        stubNewSwipe();
        var first = swipeService.superHi(CALLER, TARGET, "req-abc");
        var second = swipeService.superHi(CALLER, TARGET, "req-abc");
        assertEquals(first.getCoinsUsed(), second.getCoinsUsed());
        assertEquals(1, quotaService.getUsage(CALLER).getCardsUsed());
    }

    @Test
    void emptyClientRequestId_shouldReject() {
        MatchBizException ex = assertThrows(MatchBizException.class,
                () -> swipeService.superHi(CALLER, TARGET, "  "));
        assertEquals(MatchErrorCode.INVALID_ARGUMENT, ex.getErrorCode());
    }

    @Test
    void clientRequestId_shouldBeSaved() {
        stubNewSwipe();
        swipeService.superHi(CALLER, TARGET, "req-save");
        assertEquals(100, idempotencyStore.find(CALLER, "req-save").orElseThrow().getCoinsUsed());
    }
}
