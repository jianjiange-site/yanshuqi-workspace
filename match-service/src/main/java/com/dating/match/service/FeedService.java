package com.dating.match.service;

import com.dating.match.client.CandidateClient;
import com.dating.match.dto.GetTodayFeedResult;
import com.dating.match.exception.MatchBizException;
import com.dating.match.exception.MatchErrorCode;
import com.dating.match.recommend.CandidateProfile;
import com.dating.match.recommend.ColdStartService;
import com.dating.match.recommend.FeedQueueItem;
import com.dating.match.repository.FeedQueueRepository;
import com.dating.match.repository.SwipedSetRepository;
import com.dating.match.subscription.QuotaLimit;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 首页 Feed：Redis LIST LPOP + swiped SET 二次过滤 + D0 冷启动。
 * <p>
 * LPOP 不扣 cards 配额；配额仅在 Swipe/SuperHi 阶段扣减。
 */
@Service
@Profile("!test")
public class FeedService {

    public static final int DEFAULT_COUNT = 5;
    public static final int MAX_COUNT = 20;
    private static final int MAX_LPOP_ROUNDS = 50;

    private final QuotaService quotaService;
    private final FeedQueueRepository feedQueueRepository;
    private final SwipedSetRepository swipedSetRepository;
    private final ColdStartService coldStartService;
    private final CandidateClient candidateClient;

    public FeedService(QuotaService quotaService,
                       FeedQueueRepository feedQueueRepository,
                       SwipedSetRepository swipedSetRepository,
                       ColdStartService coldStartService,
                       CandidateClient candidateClient) {
        this.quotaService = quotaService;
        this.feedQueueRepository = feedQueueRepository;
        this.swipedSetRepository = swipedSetRepository;
        this.coldStartService = coldStartService;
        this.candidateClient = candidateClient;
    }

    public GetTodayFeedResult getTodayFeed(long callerUserId, int count) {
        if (callerUserId <= 0) {
            throw new MatchBizException(MatchErrorCode.INVALID_ARGUMENT);
        }
        int normalizedCount = normalizeCount(count);
        if (quotaService.isCardsExhausted(callerUserId)) {
            return new GetTodayFeedResult(List.of(), true);
        }
        int need = Math.min(normalizedCount, quotaService.getRemainingCards(callerUserId));
        if (need <= 0) {
            return new GetTodayFeedResult(List.of(), true);
        }

        List<CandidateProfile> result = new ArrayList<>(need);
        int rounds = 0;
        boolean coldStarted = false;

        while (result.size() < need && rounds++ < MAX_LPOP_ROUNDS) {
            int batchSize = need - result.size();
            List<FeedQueueItem> popped = feedQueueRepository.leftPop(callerUserId, batchSize);
            if (popped.isEmpty()) {
                if (!coldStarted) {
                    coldStartService.buildAndPush(callerUserId);
                    coldStarted = true;
                    continue;
                }
                break;
            }

            List<Long> targetIds = popped.stream()
                    .map(FeedQueueItem::getTargetUserId)
                    .collect(Collectors.toList());
            Set<Long> swiped = swipedSetRepository.findSwipedTargets(callerUserId, targetIds);

            // 召回阶段是快照；用户可能在其他设备已 swipe，消费阶段用 swiped SET 防重复展示；命中过滤的卡片不放回 LIST。
            List<Long> acceptedIds = new ArrayList<>();
            for (FeedQueueItem item : popped) {
                if (!swiped.contains(item.getTargetUserId())) {
                    acceptedIds.add(item.getTargetUserId());
                }
            }
            if (acceptedIds.isEmpty()) {
                if (feedQueueRepository.size(callerUserId) == 0 && coldStarted) {
                    break;
                }
                continue;
            }

            Map<Long, CandidateProfile> profiles = candidateClient.batchGetProfiles(acceptedIds);
            for (Long userId : acceptedIds) {
                if (result.size() >= need) {
                    break;
                }
                CandidateProfile profile = profiles.get(userId);
                if (profile != null) {
                    result.add(profile);
                }
            }
        }

        return new GetTodayFeedResult(result, false);
    }

    static int normalizeCount(int count) {
        if (count <= 0) {
            return DEFAULT_COUNT;
        }
        return Math.min(count, MAX_COUNT);
    }
}
