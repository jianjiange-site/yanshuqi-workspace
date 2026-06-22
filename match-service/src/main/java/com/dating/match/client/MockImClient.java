package com.dating.match.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Mock IM 客户端：默认成功；测试可配置连续失败次数。
 */
@Component
@ConditionalOnProperty(prefix = "app.match.external", name = "im-client-mode", havingValue = "mock", matchIfMissing = true)
public class MockImClient implements ImClient {

    private volatile int failRemaining = 0;
    private final AtomicInteger callCount = new AtomicInteger();

    @Override
    public boolean execute(String action, String payloadJson) {
        callCount.incrementAndGet();
        if (failRemaining > 0) {
            failRemaining--;
            return false;
        }
        return true;
    }

    public void setFailCount(int count) {
        this.failRemaining = count;
    }

    public int getCallCount() {
        return callCount.get();
    }

    public void reset() {
        failRemaining = 0;
        callCount.set(0);
    }
}
