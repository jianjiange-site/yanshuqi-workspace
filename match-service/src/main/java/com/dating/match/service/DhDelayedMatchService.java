package com.dating.match.service;

import com.dating.match.config.MatchProperties;
import com.dating.match.constant.MatchSourceConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

/**
 * RIGHT DH 延迟匹配：内存调度 15s~2min 后创建 match。
 * <p>
 * 进程重启会丢失未执行任务，本阶段接受该 trade-off，不做 PG pending 表。
 */
@Service
@Profile("!test")
public class DhDelayedMatchService {

    private static final Logger log = LoggerFactory.getLogger(DhDelayedMatchService.class);

    private final TaskScheduler taskScheduler;
    private final MatchCreationService matchCreationService;
    private final MatchProperties matchProperties;

    public DhDelayedMatchService(TaskScheduler taskScheduler,
                                 MatchCreationService matchCreationService,
                                 MatchProperties matchProperties) {
        this.taskScheduler = taskScheduler;
        this.matchCreationService = matchCreationService;
        this.matchProperties = matchProperties;
    }

    public void scheduleDelayedMatch(long userId, long dhUserId) {
        long delayMs = randomDelayMs();
        Instant runAt = Instant.now().plusMillis(delayMs);
        taskScheduler.schedule(() -> executeDelayedMatch(userId, dhUserId), runAt);
    }

    private void executeDelayedMatch(long userId, long dhUserId) {
        try {
            matchCreationService.createMatch(userId, dhUserId, MatchSourceConstant.SWIPE_MATCH);
        } catch (Exception ex) {
            log.error("DH 延迟匹配创建失败, userId={}, dhUserId={}", userId, dhUserId, ex);
        }
    }

    private long randomDelayMs() {
        long min = matchProperties.getDhDelayedMatchMinMs();
        long max = matchProperties.getDhDelayedMatchMaxMs();
        if (max <= min) {
            return min;
        }
        return ThreadLocalRandom.current().nextLong(min, max + 1);
    }
}
