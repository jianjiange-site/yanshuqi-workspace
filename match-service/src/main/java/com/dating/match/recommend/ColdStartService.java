package com.dating.match.recommend;

import com.dating.match.client.CandidateClient;
import com.dating.match.config.MatchProperties;
import com.dating.match.repository.FeedQueueRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

/**
 * D0 冷启动：mock 召回 DH/BH 候选，混排后 RPUSH 到 Feed LIST。
 */
@Service
@Profile("!test")
public class ColdStartService {

    private final CandidateClient candidateClient;
    private final FeedMergeService feedMergeService;
    private final FeedQueueRepository feedQueueRepository;
    private final MatchProperties matchProperties;

    public ColdStartService(CandidateClient candidateClient,
                            FeedMergeService feedMergeService,
                            FeedQueueRepository feedQueueRepository,
                            MatchProperties matchProperties) {
        this.candidateClient = candidateClient;
        this.feedMergeService = feedMergeService;
        this.feedQueueRepository = feedQueueRepository;
        this.matchProperties = matchProperties;
    }

    public int buildAndPush(long callerUserId) {
        MatchProperties.FeedProperties feed = matchProperties.getFeed();
        int queueSize = feed.getQueueSize();
        double bhRatio = feed.getColdStartBhRatio();
        List<CandidateProfile> dhCandidates = candidateClient.listDhCandidates(callerUserId, queueSize);
        List<CandidateProfile> bhCandidates = candidateClient.listBhCandidates(callerUserId, queueSize);
        List<FeedQueueItem> merged = feedMergeService.merge(bhCandidates, dhCandidates, queueSize, bhRatio);
        Duration ttl = Duration.ofDays(feed.getQueueTtlDays());
        feedQueueRepository.pushAll(callerUserId, merged, ttl);
        return merged.size();
    }
}
