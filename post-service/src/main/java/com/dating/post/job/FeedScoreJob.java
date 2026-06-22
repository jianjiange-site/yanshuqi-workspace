package com.dating.post.job;

import com.dating.post.service.FeedPoolRebuildService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 热门推荐池定时重建 Job，每 5 分钟扫描近 3 天帖子并重算热度分。
 */
@Component
@Profile("!test")
public class FeedScoreJob {

    private static final Logger log = LoggerFactory.getLogger(FeedScoreJob.class);

    private final FeedPoolRebuildService feedPoolRebuildService;

    public FeedScoreJob(FeedPoolRebuildService feedPoolRebuildService) {
        this.feedPoolRebuildService = feedPoolRebuildService;
    }

    @Scheduled(fixedDelay = 300_000, initialDelay = 60_000)
    public void rebuildRecommendPools() {
        try {
            feedPoolRebuildService.rebuildAllRecommendPools();
        } catch (Exception ex) {
            log.warn("FeedScoreJob 执行失败, error={}", ex.getMessage());
        }
    }
}
