package com.dating.post.job;

import com.dating.post.service.PostStatFlushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 评论增量定时刷盘 Job。
 * <p>
 * 写合并策略：每 60 秒将 Redis comments delta 批量落库到 post_stats，非强一致实时 UPDATE。
 */
@Component
@Profile("!test")
public class CommentFlushJob {

    private static final Logger log = LoggerFactory.getLogger(CommentFlushJob.class);

    private final PostStatFlushService postStatFlushService;

    public CommentFlushJob(PostStatFlushService postStatFlushService) {
        this.postStatFlushService = postStatFlushService;
    }

    @Scheduled(fixedDelayString = "${app.post.comment-flush-interval-ms:60000}")
    public void flushComments() {
        try {
            postStatFlushService.flushCommentDeltas();
        } catch (Exception ex) {
            log.warn("CommentFlushJob 执行失败, error={}", ex.getMessage());
        }
    }
}
