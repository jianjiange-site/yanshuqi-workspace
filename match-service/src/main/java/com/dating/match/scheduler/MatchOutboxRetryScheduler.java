package com.dating.match.scheduler;

import com.dating.match.service.MatchOutboxRetryService;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Outbox 定时重试调度，每 30 秒扫描 PENDING 记录。
 */
@Component
@Profile("!test")
public class MatchOutboxRetryScheduler {

    private static final int BATCH_SIZE = 50;

    private final MatchOutboxRetryService matchOutboxRetryService;

    public MatchOutboxRetryScheduler(MatchOutboxRetryService matchOutboxRetryService) {
        this.matchOutboxRetryService = matchOutboxRetryService;
    }

    @Scheduled(fixedDelay = 30000)
    public void retryOutbox() {
        matchOutboxRetryService.retryPending(BATCH_SIZE);
    }
}
