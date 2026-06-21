package com.dating.match.service;

import com.dating.match.client.MockPaymentClient;
import com.dating.match.client.MockSubscriptionClient;
import com.dating.match.exception.MatchBizException;
import com.dating.match.exception.MatchErrorCode;
import com.dating.match.subscription.SubscriptionTier;
import com.dating.match.support.InMemoryQuotaHashRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QuotaServiceTest {

    private static final long USER_ID = 10001L;

    private InMemoryQuotaHashRepository quotaRepository;
    private MockSubscriptionClient subscriptionClient;
    private MockPaymentClient paymentClient;
    private QuotaService quotaService;

    @BeforeEach
    void setUp() {
        quotaRepository = new InMemoryQuotaHashRepository();
        subscriptionClient = new MockSubscriptionClient();
        paymentClient = new MockPaymentClient();
        quotaService = new QuotaService(quotaRepository, subscriptionClient, paymentClient);
        subscriptionClient.setTier(USER_ID, SubscriptionTier.FREE);
    }

    @Test
    void freeLeft_shouldAllow50CardsThenFail() {
        for (int i = 0; i < 50; i++) {
            quotaService.consumeLeftSwipe(USER_ID);
        }
        MatchBizException ex = assertThrows(MatchBizException.class, () -> quotaService.consumeLeftSwipe(USER_ID));
        assertEquals(MatchErrorCode.QUOTA_CARD_EXCEEDED, ex.getErrorCode());
        assertEquals(50, quotaService.getUsage(USER_ID).getCardsUsed());
    }

    @Test
    void freeRight_shouldAllow5ThenFailAndRollbackCards() {
        for (int i = 0; i < 5; i++) {
            quotaService.consumeRightSwipe(USER_ID);
        }
        MatchBizException ex = assertThrows(MatchBizException.class, () -> quotaService.consumeRightSwipe(USER_ID));
        assertEquals(MatchErrorCode.QUOTA_RIGHT_SWIPE_EXCEEDED, ex.getErrorCode());
        assertEquals(5, quotaService.getUsage(USER_ID).getRightSwipeUsed());
        assertEquals(5, quotaService.getUsage(USER_ID).getCardsUsed());
    }

    @Test
    void monthlySuperHi_firstFree_secondCoins() {
        subscriptionClient.setTier(USER_ID, SubscriptionTier.MONTHLY);
        int first = quotaService.consumeSuperHi(USER_ID, "req-1");
        int second = quotaService.consumeSuperHi(USER_ID, "req-2");
        assertEquals(0, first);
        assertEquals(100, second);
    }

    @Test
    void freeSuperHi_shouldChargeCoins() {
        int coinsUsed = quotaService.consumeSuperHi(USER_ID, "req-free");
        assertEquals(100, coinsUsed);
    }

    @Test
    void insufficientCoins_shouldRollbackCardsAndRightSwipe() {
        paymentClient.setInsufficientCoins(true);
        MatchBizException ex = assertThrows(MatchBizException.class,
                () -> quotaService.consumeSuperHi(USER_ID, "req-no-coin"));
        assertEquals(MatchErrorCode.INSUFFICIENT_COINS, ex.getErrorCode());
        assertEquals(0, quotaService.getUsage(USER_ID).getCardsUsed());
        assertEquals(0, quotaService.getUsage(USER_ID).getRightSwipeUsed());
    }
}
