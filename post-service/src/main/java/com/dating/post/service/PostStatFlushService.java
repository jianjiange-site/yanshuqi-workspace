package com.dating.post.service;

import com.dating.post.manager.PostStatManager;
import com.dating.post.repository.PostStatDeltaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Redis delta 刷盘到 post_stats 的公共逻辑。
 * <p>
 * 定时刷盘是写合并策略，不是强一致实时写库；Lua 原子取数归零避免丢增量。
 */
@Service
public class PostStatFlushService {

    private static final Logger log = LoggerFactory.getLogger(PostStatFlushService.class);

    private final PostStatDeltaRepository postStatDeltaRepository;
    private final PostStatManager postStatManager;

    public PostStatFlushService(PostStatDeltaRepository postStatDeltaRepository,
                                PostStatManager postStatManager) {
        this.postStatDeltaRepository = postStatDeltaRepository;
        this.postStatManager = postStatManager;
    }

    public void flushLikeDeltas() {
        Set<Long> postIds = postStatDeltaRepository.listUpdatedPostIds();
        for (Long postId : postIds) {
            long delta = postStatDeltaRepository.atomicTakeLikeDelta(postId);
            if (delta != 0L) {
                postStatManager.addLikeCount(postId, (int) delta);
                log.info("刷盘点赞增量, postId={}, delta={}", postId, delta);
            }
            postStatDeltaRepository.removeUpdatedPostIfIdle(postId);
        }
    }

    public void flushCommentDeltas() {
        Set<Long> postIds = postStatDeltaRepository.listUpdatedPostIds();
        for (Long postId : postIds) {
            long delta = postStatDeltaRepository.atomicTakeCommentDelta(postId);
            if (delta != 0L) {
                postStatManager.addCommentCount(postId, (int) delta);
                log.info("刷盘评论增量, postId={}, delta={}", postId, delta);
            }
            postStatDeltaRepository.removeUpdatedPostIfIdle(postId);
        }
    }
}
