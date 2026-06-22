package com.dating.post.service;

import com.dating.post.constant.LikeStatus;
import com.dating.post.exception.PostBusinessException;
import com.dating.post.exception.PostErrorCode;
import com.dating.post.manager.PostLikeManager;
import com.dating.post.manager.PostManager;
import com.dating.post.repository.PostStatDeltaRepository;
import org.springframework.stereotype.Service;

/**
 * 点赞 / 取消点赞业务。
 * <p>
 * 幂等依赖 post_likes 联合主键 (user_id, post_id)；只有 status 真实变化时才写 Redis delta，
 * 避免重复点赞/取消导致计数漂移。
 */
@Service
public class PostLikeService {

    private final PostManager postManager;
    private final PostLikeManager postLikeManager;
    private final PostStatDeltaRepository postStatDeltaRepository;
    private final PostCacheService postCacheService;

    public PostLikeService(PostManager postManager,
                           PostLikeManager postLikeManager,
                           PostStatDeltaRepository postStatDeltaRepository,
                           PostCacheService postCacheService) {
        this.postManager = postManager;
        this.postLikeManager = postLikeManager;
        this.postStatDeltaRepository = postStatDeltaRepository;
        this.postCacheService = postCacheService;
    }

    public boolean actionLike(long callerUserId, long postId, boolean like) {
        if (postId <= 0L) {
            throw new PostBusinessException(PostErrorCode.INVALID_ARGUMENT, "postId 非法");
        }
        postManager.findActivePost(postId)
                .orElseThrow(() -> new PostBusinessException(PostErrorCode.POST_NOT_FOUND));

        int targetStatus = like ? LikeStatus.LIKED : LikeStatus.UNLIKED;
        boolean changed = postLikeManager.upsertIfChanged(callerUserId, postId, targetStatus);
        if (changed) {
            int delta = like ? 1 : -1;
            postStatDeltaRepository.incrementLikeDelta(postId, delta);
            postCacheService.evictDetail(postId);
        }
        return true;
    }
}
