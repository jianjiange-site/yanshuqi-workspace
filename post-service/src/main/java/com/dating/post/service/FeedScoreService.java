package com.dating.post.service;

import org.springframework.stereotype.Service;

/**
 * Feed 热度分计算。
 */
@Service
public class FeedScoreService {

    /**
     * 热度分公式：评论权重高于点赞，时间衰减抑制老帖霸榜。
     * <p>
     * score = (10 + like*1 + comment*3) / pow(hoursSinceCreated + 2, 1.5)
     */
    public double calculateHotScore(int likeCount, int commentCount, long createdAtSeconds) {
        long nowSeconds = System.currentTimeMillis() / 1000;
        double hoursSinceCreated = Math.max(0, nowSeconds - createdAtSeconds) / 3600.0;
        double numerator = 10.0 + likeCount * 1.0 + commentCount * 3.0;
        return numerator / Math.pow(hoursSinceCreated + 2.0, 1.5);
    }
}
