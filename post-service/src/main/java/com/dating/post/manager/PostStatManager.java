package com.dating.post.manager;

import com.dating.post.entity.PostStatEntity;
import com.dating.post.mapper.PostStatMapper;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

/**
 * 帖子计数 Manager，仅访问 post_stats 单表。
 */
@Component
public class PostStatManager {

    private final PostStatMapper postStatMapper;

    public PostStatManager(PostStatMapper postStatMapper) {
        this.postStatMapper = postStatMapper;
    }

    public void insertInitial(long postId) {
        PostStatEntity entity = new PostStatEntity();
        entity.setPostId(postId);
        entity.setLikeCount(0);
        entity.setCommentCount(0);
        entity.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        postStatMapper.insert(entity);
    }

    public Optional<PostStatEntity> findByPostId(long postId) {
        PostStatEntity entity = postStatMapper.selectById(postId);
        return Optional.ofNullable(entity);
    }

    public void addLikeCount(long postId, int delta) {
        postStatMapper.addLikeCount(postId, delta);
    }

    public void addCommentCount(long postId, int delta) {
        postStatMapper.addCommentCount(postId, delta);
    }
}
