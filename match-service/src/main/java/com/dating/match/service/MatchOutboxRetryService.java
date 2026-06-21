package com.dating.match.service;

import com.dating.match.client.ImClient;
import com.dating.match.constant.MatchOutboxStatus;
import com.dating.match.entity.MatchOutboxEntity;
import com.dating.match.manager.MatchOutboxManager;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Outbox 重试：解决「本地 match 已创建、外部 IM 调用失败」的最终一致性问题。
 */
@Service
@Profile("!test")
public class MatchOutboxRetryService {

    public static final int MAX_ATTEMPTS = 5;
    private static final long[] BACKOFF_SECONDS = {10L, 30L, 60L, 300L, 900L};

    private final MatchOutboxManager matchOutboxManager;
    private final ImClient imClient;

    public MatchOutboxRetryService(MatchOutboxManager matchOutboxManager, ImClient imClient) {
        this.matchOutboxManager = matchOutboxManager;
        this.imClient = imClient;
    }

    public void retryPending(int batchSize) {
        List<MatchOutboxEntity> pending = matchOutboxManager.listPendingForRetry(batchSize);
        for (MatchOutboxEntity entity : pending) {
            processOne(entity);
        }
    }

    private void processOne(MatchOutboxEntity entity) {
        if (!MatchOutboxStatus.PENDING.equals(entity.getStatus())) {
            return;
        }
        boolean success = imClient.execute(entity.getAction(), entity.getPayloadJson());
        if (success) {
            matchOutboxManager.markDone(entity.getId());
            return;
        }
        int nextAttempts = entity.getAttempts() + 1;
        if (nextAttempts >= MAX_ATTEMPTS) {
            matchOutboxManager.markDead(entity.getId());
            return;
        }
        long backoff = BACKOFF_SECONDS[Math.min(nextAttempts - 1, BACKOFF_SECONDS.length - 1)];
        matchOutboxManager.increaseAttemptsAndDelay(entity.getId(), Instant.now().plusSeconds(backoff));
    }
}
