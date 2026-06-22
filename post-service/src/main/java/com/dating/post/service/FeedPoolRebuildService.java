package com.dating.post.service;

import com.dating.post.client.UserProfileClient;
import com.dating.post.constant.GenderBucket;
import com.dating.post.entity.PostEntity;
import com.dating.post.entity.PostStatEntity;
import com.dating.post.manager.PostManager;
import com.dating.post.manager.PostStatManager;
import com.dating.post.repository.FeedPoolRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 热门推荐池定时重建。
 * <p>
 * 采用定时批量重算而非每次点赞实时重排，避免高频互动触发全量 ZSet 更新；
 * 使用 tmp key + rename 原子切换，读侧始终看到完整旧池或完整新池。
 * 评论权重 &gt; 点赞，因为评论代表更深互动、更能反映内容质量。
 */
@Service
public class FeedPoolRebuildService {

    private static final Logger log = LoggerFactory.getLogger(FeedPoolRebuildService.class);
    private static final int RECENT_DAYS = 3;

    private final PostManager postManager;
    private final PostStatManager postStatManager;
    private final PostStatReadService postStatReadService;
    private final FeedScoreService feedScoreService;
    private final UserProfileClient userProfileClient;
    private final FeedPoolRepository feedPoolRepository;

    public FeedPoolRebuildService(PostManager postManager,
                                  PostStatManager postStatManager,
                                  PostStatReadService postStatReadService,
                                  FeedScoreService feedScoreService,
                                  UserProfileClient userProfileClient,
                                  FeedPoolRepository feedPoolRepository) {
        this.postManager = postManager;
        this.postStatManager = postStatManager;
        this.postStatReadService = postStatReadService;
        this.feedScoreService = feedScoreService;
        this.userProfileClient = userProfileClient;
        this.feedPoolRepository = feedPoolRepository;
    }

    public void rebuildAllRecommendPools() {
        OffsetDateTime since = OffsetDateTime.now(ZoneOffset.UTC).minusDays(RECENT_DAYS);
        List<PostEntity> posts = postManager.listRecentActivePosts(since);
        if (posts.isEmpty()) {
            feedPoolRepository.rebuildRecommendPool(GenderBucket.MALE, Map.of());
            feedPoolRepository.rebuildRecommendPool(GenderBucket.FEMALE, Map.of());
            log.info("热门池重建完成，近 {} 天无可用帖子", RECENT_DAYS);
            return;
        }

        Set<Long> userIds = new HashSet<>();
        for (PostEntity post : posts) {
            userIds.add(post.getUserId());
        }
        Map<Long, GenderBucket> genderMap = userProfileClient.batchGetGenderBuckets(userIds);

        Map<GenderBucket, Map<Long, Double>> bucketScores = new EnumMap<>(GenderBucket.class);
        bucketScores.put(GenderBucket.MALE, new HashMap<>());
        bucketScores.put(GenderBucket.FEMALE, new HashMap<>());

        for (PostEntity post : posts) {
            PostStatEntity stat = postStatManager.findByPostId(post.getPostId()).orElse(null);
            int likeCount = postStatReadService.getRealLikeCount(post.getPostId(), stat);
            int commentCount = postStatReadService.getRealCommentCount(post.getPostId(), stat);
            long createdAtSeconds = post.getCreatedAt() == null ? 0L : post.getCreatedAt().toEpochSecond();
            double score = feedScoreService.calculateHotScore(likeCount, commentCount, createdAtSeconds);

            GenderBucket authorGender = genderMap.getOrDefault(post.getUserId(), GenderBucket.MALE);
            bucketScores.get(authorGender).put(post.getPostId(), score);
        }

        feedPoolRepository.rebuildRecommendPool(GenderBucket.MALE, bucketScores.get(GenderBucket.MALE));
        feedPoolRepository.rebuildRecommendPool(GenderBucket.FEMALE, bucketScores.get(GenderBucket.FEMALE));
        log.info("热门池重建完成, 扫描帖子数={}", posts.size());
    }
}
