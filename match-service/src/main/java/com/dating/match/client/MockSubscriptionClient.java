package com.dating.match.client;

import com.dating.match.subscription.SubscriptionTier;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock 订阅客户端，默认 FREE；测试可通过 {@link #setTier(long, SubscriptionTier)} 切换档位。
 */
@Component
public class MockSubscriptionClient implements SubscriptionClient {

    private final ConcurrentHashMap<Long, SubscriptionTier> tierOverrides = new ConcurrentHashMap<>();

    @Override
    public SubscriptionTier getTier(long userId) {
        return tierOverrides.getOrDefault(userId, SubscriptionTier.FREE);
    }

    public void setTier(long userId, SubscriptionTier tier) {
        if (tier == null) {
            tierOverrides.remove(userId);
        } else {
            tierOverrides.put(userId, tier);
        }
    }

    public void clear() {
        tierOverrides.clear();
    }
}
