package com.dating.match.recommend;

import com.dating.match.config.MatchProperties;
import com.dating.match.manager.SwipeHistoryManager;
import com.dating.match.repository.FeedQueueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

/**
 * D1 日更队列生成：偏好 → 召回 → 排序 → 混排 → Redis DEL+RPUSH 覆盖写入。
 */
@Component
@Profile("!test")
public class D1Generator {

    private static final Logger log = LoggerFactory.getLogger(D1Generator.class);

    private final SwipeHistoryManager swipeHistoryManager;
    private final PreferenceBuilder preferenceBuilder;
    private final CandidateRecaller candidateRecaller;
    private final D1Ranker d1Ranker;
    private final D1FeedMergeService d1FeedMergeService;
    private final FeedQueueRepository feedQueueRepository;
    private final MatchProperties matchProperties;

    public D1Generator(SwipeHistoryManager swipeHistoryManager,
                       PreferenceBuilder preferenceBuilder,
                       CandidateRecaller candidateRecaller,
                       D1Ranker d1Ranker,
                       D1FeedMergeService d1FeedMergeService,
                       FeedQueueRepository feedQueueRepository,
                       MatchProperties matchProperties) {
        this.swipeHistoryManager = swipeHistoryManager;
        this.preferenceBuilder = preferenceBuilder;
        this.candidateRecaller = candidateRecaller;
        this.d1Ranker = d1Ranker;
        this.d1FeedMergeService = d1FeedMergeService;
        this.feedQueueRepository = feedQueueRepository;
        this.matchProperties = matchProperties;
    }

    /**
     * 为指定用户生成 D1 队列；{@code referenceDate} 为调度运行日（UTC），
     * 仅处理 referenceDate 前一天有 swipe 行为的用户。
     *
     * @return 写入队列长度，0 表示跳过
     */
    public int generateForUser(long userId, LocalDate referenceDate) {
        LocalDate yesterday = referenceDate.minusDays(1);
        if (!swipeHistoryManager.hasSwipeYesterday(userId, yesterday)) {
            return 0;
        }

        PreferenceProfile preference = preferenceBuilder.build(userId);
        D1CandidatePools pools = candidateRecaller.recall(userId, preference);

        List<CandidateProfile> rankedDh = d1Ranker.rank(userId, preference, pools.getDhCandidates());
        List<CandidateProfile> rankedBh = d1Ranker.rank(userId, preference, pools.getBhCandidates());

        int queueSize = matchProperties.getFeed().getQueueSize();
        List<FeedQueueItem> merged = d1FeedMergeService.merge(preference, rankedBh, rankedDh, queueSize);

        if (merged.size() < queueSize) {
            log.warn("D1 queue underfilled userId={} expected={} actual={}", userId, queueSize, merged.size());
        }

        Duration ttl = Duration.ofDays(matchProperties.getFeed().getQueueTtlDays());
        feedQueueRepository.replaceAll(userId, merged, ttl);
        return merged.size();
    }
}
