package com.dating.post.manager;

import com.dating.post.constant.LikeStatus;
import com.dating.post.mapper.PostLikeMapper;
import org.springframework.stereotype.Component;

/**
 * 点赞 Manager，仅访问 post_likes 单表。
 */
@Component
public class PostLikeManager {

    private final PostLikeMapper postLikeMapper;

    public PostLikeManager(PostLikeMapper postLikeMapper) {
        this.postLikeMapper = postLikeMapper;
    }

    /**
     * 幂等 upsert，返回是否发生状态变化。
     */
    public boolean upsertIfChanged(long userId, long postId, int targetStatus) {
        int affected = postLikeMapper.upsertIfChanged(userId, postId, targetStatus);
        return affected > 0;
    }

    public boolean isLiked(long userId, long postId) {
        Integer status = postLikeMapper.findStatus(userId, postId);
        return status != null && status == LikeStatus.LIKED;
    }
}
