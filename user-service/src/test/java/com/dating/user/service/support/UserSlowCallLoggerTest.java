package com.dating.user.service.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class UserSlowCallLoggerTest {

    @Test
    void fastCallShouldNotThrow() {
        SlowCallLogger logger = new SlowCallLogger();
        long startNano = System.nanoTime();
        assertDoesNotThrow(() -> logger.logIfSlow("getUserProfileView", startNano, 1001L, true, null));
    }

    @Test
    void slowCallShouldNotThrowAndOnlyUseWhitelistFields() {
        SlowCallLogger logger = new SlowCallLogger();
        long fakeStart = System.nanoTime() - 600_000_000L;
        assertDoesNotThrow(() -> logger.logIfSlow("resolveOrCreateDeviceUser", fakeStart, 2002L, false, "INTERNAL_ERROR"));
        assertDoesNotThrow(() -> logger.logBatchIfSlow("batchGetBasicProfiles", fakeStart, 10, 5, 5, true, null));
    }
}
