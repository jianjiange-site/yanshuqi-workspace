package com.dating.post.service;

import com.dating.post.entity.PostStatEntity;
import com.dating.post.manager.PostLikeManager;
import com.dating.post.repository.PostStatDeltaRepository;
import org.springframework.stereotype.Service;

/**
 * 帖子计数读取：DB 基准值 + Redis 未刷盘增量。
 * <p>
 * post_stats 是权威基准，Redis delta 承载高频写合并；用户看到的是两者之和。
 */
@Service
public class PostStatReadService {

    private final PostStatDeltaRepository postStatDeltaRepository;
    private final PostLikeManager postLikeManager;

    public PostStatReadService(PostStatDeltaRepository postStatDeltaRepository,
                               PostLikeManager postLikeManager) {
        this.postStatDeltaRepository = postStatDeltaRepository;
        this.postLikeManager = postLikeManager;
    }

    public int getRealLikeCount(long postId, PostStatEntity stat) {
        int base = stat == null || stat.getLikeCount() == null ? 0 : stat.getLikeCount();
        return base + postStatDeltaRepository.readLikeDelta(postId);
    }

    public int getRealCommentCount(long postId, PostStatEntity stat) {
        int base = stat == null || stat.getCommentCount() == null ? 0 : stat.getCommentCount();
        return base + postStatDeltaRepository.readCommentDelta(postId);
    }

    public boolean isLiked(long callerUserId, long postId) {
        if (callerUserId <= 0L) {
            return false;
        }
        return postLikeManager.isLiked(callerUserId, postId);
    }
}
