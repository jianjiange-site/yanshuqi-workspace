package com.dating.post.manager;

import com.dating.post.constant.CommentStatus;
import com.dating.post.entity.PostCommentEntity;
import com.dating.post.mapper.PostCommentMapper;
import com.dating.post.mapper.PostStatMapper;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

/**
 * 评论 Manager，仅访问 post_comments / post_stats 单表。
 */
@Component
public class PostCommentManager {

    private final PostCommentMapper postCommentMapper;

    public PostCommentManager(PostCommentMapper postCommentMapper) {
        this.postCommentMapper = postCommentMapper;
    }

    public void insertComment(PostCommentEntity entity) {
        postCommentMapper.insert(entity);
    }

    public Optional<PostCommentEntity> findActiveComment(long commentId) {
        return Optional.ofNullable(postCommentMapper.findByCommentId(commentId));
    }

    public List<PostCommentEntity> listCommentsFromDb(long postId, long cursorCommentId, int limit) {
        return postCommentMapper.listByPostIdBeforeCommentId(postId, cursorCommentId, limit);
    }

    public void softDeleteComment(PostCommentEntity comment) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        comment.setDeleted(1);
        comment.setStatus(CommentStatus.INACTIVE);
        comment.setUpdatedAt(now);
        postCommentMapper.updateById(comment);
    }
}
