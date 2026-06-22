package com.dating.match.service;

import com.dating.match.client.PaymentClient;
import com.dating.match.client.SubscriptionClient;
import com.dating.match.config.MatchProperties;
import com.dating.match.constant.RedisKeyConstants;
import com.dating.match.dto.QuotaUsage;
import com.dating.match.exception.MatchBizException;
import com.dating.match.exception.MatchErrorCode;
import com.dating.match.grpc.proto.GetQuotaResp;
import com.dating.match.repository.QuotaHashRepository;
import com.dating.match.subscription.QuotaLimit;
import com.dating.match.subscription.SubscriptionTier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * 每日配额服务：Redis Hash 记录已用量，HINCRBY + 业务回滚（后续高并发可改 Lua）。
 */
@Service
@Profile("!test")
public class QuotaService {

    public static final String FIELD_CARDS = "cards";
    public static final String FIELD_RIGHT_SWIPE = "right_swipe";
    public static final String FIELD_SUPER_HI = "super_hi";
    public static final long QUOTA_TTL_SECONDS = 36 * 3600L;

    private final QuotaHashRepository quotaHashRepository;
    private final SubscriptionClient subscriptionClient;
    private final PaymentClient paymentClient;
    private final MatchProperties matchProperties;

    public QuotaService(QuotaHashRepository quotaHashRepository,
                        SubscriptionClient subscriptionClient,
                        PaymentClient paymentClient,
                        MatchProperties matchProperties) {
        this.quotaHashRepository = quotaHashRepository;
        this.subscriptionClient = subscriptionClient;
        this.paymentClient = paymentClient;
        this.matchProperties = matchProperties;
    }

    public QuotaUsage getUsage(long userId) {
        String key = quotaKey(userId);
        QuotaUsage usage = new QuotaUsage();
        usage.setCardsUsed((int) quotaHashRepository.get(key, FIELD_CARDS));
        usage.setRightSwipeUsed((int) quotaHashRepository.get(key, FIELD_RIGHT_SWIPE));
        usage.setSuperHiUsed((int) quotaHashRepository.get(key, FIELD_SUPER_HI));
        return usage;
    }

    public boolean isCardsExhausted(long userId) {
        QuotaLimit limit = subscriptionClient.getTier(userId).quotaLimit();
        return getUsage(userId).getCardsUsed() >= limit.getDailyCardLimit();
    }

    public int getRemainingCards(long userId) {
        QuotaLimit limit = subscriptionClient.getTier(userId).quotaLimit();
        return Math.max(0, limit.getDailyCardLimit() - getUsage(userId).getCardsUsed());
    }

    public GetQuotaResp buildQuotaResponse(long userId) {
        SubscriptionTier tier = subscriptionClient.getTier(userId);
        QuotaLimit limit = tier.quotaLimit();
        QuotaUsage usage = getUsage(userId);
        return GetQuotaResp.newBuilder()
                .setTier(tier.getCode())
                .setDailyRightSwipeLimit(limit.getDailyRightSwipeLimit())
                .setDailyRightSwipeUsed(usage.getRightSwipeUsed())
                .setDailyCardLimit(limit.getDailyCardLimit())
                .setDailyCardUsed(usage.getCardsUsed())
                .setDailySuperHiLimit(limit.getDailySuperHiLimit())
                .setDailySuperHiUsed(usage.getSuperHiUsed())
                .setSuperHiCoinPrice(matchProperties.getSuperHiCoinPrice())
                .build();
    }

    public void consumeLeftSwipe(long userId) {
        QuotaLimit limit = subscriptionClient.getTier(userId).quotaLimit();
        String key = quotaKey(userId);
        long cards = increment(key, FIELD_CARDS, 1);
        if (cards > limit.getDailyCardLimit()) {
            increment(key, FIELD_CARDS, -1);
            throw new MatchBizException(MatchErrorCode.QUOTA_CARD_EXCEEDED);
        }
    }

    public void consumeRightSwipe(long userId) {
        QuotaLimit limit = subscriptionClient.getTier(userId).quotaLimit();
        String key = quotaKey(userId);
        long cards = increment(key, FIELD_CARDS, 1);
        if (cards > limit.getDailyCardLimit()) {
            increment(key, FIELD_CARDS, -1);
            throw new MatchBizException(MatchErrorCode.QUOTA_CARD_EXCEEDED);
        }
        long rightSwipe = increment(key, FIELD_RIGHT_SWIPE, 1);
        if (rightSwipe > limit.getDailyRightSwipeLimit()) {
            increment(key, FIELD_RIGHT_SWIPE, -1);
            increment(key, FIELD_CARDS, -1);
            throw new MatchBizException(MatchErrorCode.QUOTA_RIGHT_SWIPE_EXCEEDED);
        }
    }

    /**
     * SuperHi 配额：先按 RIGHT 扣 cards+right_swipe，再扣 super_hi 或 mock 金币。
     *
     * @return 本次消耗金币数
     */
    public int consumeSuperHi(long userId, String idempotencyKey) {
        QuotaLimit limit = subscriptionClient.getTier(userId).quotaLimit();
        String key = quotaKey(userId);
        long cards = increment(key, FIELD_CARDS, 1);
        if (cards > limit.getDailyCardLimit()) {
            increment(key, FIELD_CARDS, -1);
            throw new MatchBizException(MatchErrorCode.QUOTA_CARD_EXCEEDED);
        }
        long rightSwipe = increment(key, FIELD_RIGHT_SWIPE, 1);
        if (rightSwipe > limit.getDailyRightSwipeLimit()) {
            increment(key, FIELD_RIGHT_SWIPE, -1);
            increment(key, FIELD_CARDS, -1);
            throw new MatchBizException(MatchErrorCode.QUOTA_RIGHT_SWIPE_EXCEEDED);
        }
        if (limit.getDailySuperHiLimit() > 0) {
            long superHi = increment(key, FIELD_SUPER_HI, 1);
            if (superHi <= limit.getDailySuperHiLimit()) {
                return 0;
            }
            increment(key, FIELD_SUPER_HI, -1);
        }
        int coinPrice = matchProperties.getSuperHiCoinPrice();
        boolean paid = paymentClient.consumeCoins(userId, coinPrice, idempotencyKey);
        if (!paid) {
            increment(key, FIELD_RIGHT_SWIPE, -1);
            increment(key, FIELD_CARDS, -1);
            throw new MatchBizException(MatchErrorCode.INSUFFICIENT_COINS);
        }
        return coinPrice;
    }

    private long increment(String key, String field, long delta) {
        try {
            long value = quotaHashRepository.increment(key, field, delta);
            quotaHashRepository.ensureTtl(key, QUOTA_TTL_SECONDS);
            return value;
        } catch (RuntimeException ex) {
            throw new MatchBizException(MatchErrorCode.INTERNAL_ERROR, ex.getMessage());
        }
    }

    private String quotaKey(long userId) {
        return RedisKeyConstants.quotaKey(userId, LocalDate.now(ZoneOffset.UTC));
    }
}
