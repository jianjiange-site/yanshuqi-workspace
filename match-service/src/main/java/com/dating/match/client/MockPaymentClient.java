package com.dating.match.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock 金币客户端，默认扣减成功；测试可模拟余额不足。
 */
@Component
@ConditionalOnProperty(prefix = "app.match.external", name = "payment-client-mode", havingValue = "mock", matchIfMissing = true)
public class MockPaymentClient implements PaymentClient {

    public static final int SUPER_HI_COIN_PRICE = 100;

    private volatile boolean insufficientCoins;
    private final Set<String> consumedKeys = ConcurrentHashMap.newKeySet();

    @Override
    public boolean consumeCoins(long userId, int amount, String idempotencyKey) {
        if (insufficientCoins) {
            return false;
        }
        if (idempotencyKey != null) {
            consumedKeys.add(idempotencyKey);
        }
        return true;
    }

    public void setInsufficientCoins(boolean insufficientCoins) {
        this.insufficientCoins = insufficientCoins;
    }

    public boolean wasConsumed(String idempotencyKey) {
        return consumedKeys.contains(idempotencyKey);
    }

    public void reset() {
        insufficientCoins = false;
        consumedKeys.clear();
    }
}
