package com.dating.post.service;

import com.dating.post.constant.CommentStatus;
import com.dating.post.dto.CommentInfoDTO;
import com.dating.post.dto.ListCommentsResult;
import com.dating.post.entity.PostCommentEntity;
import com.dating.post.exception.PostBusinessException;
import com.dating.post.exception.PostErrorCode;
import com.dating.post.manager.PostCommentManager;
import com.dating.post.manager.PostManager;
import com.dating.post.repository.PostStatDeltaRepository;
import com.dating.post.service.support.BusinessIdGenerator;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 评论创建 / 列表 / 删除业务。
 * <p>
 * 评论 ZSet 只维护最新 200 条窗口，超出部分需回源 DB；Redis 不可用时自动降级读库。
 */
@Service
public class PostCommentService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;

    private final PostManager postManager;
    private final PostCommentManager postCommentManager;
    private final PostStatDeltaRepository postStatDeltaRepository;
    private final PostCacheService postCacheService;
    private final BusinessIdGenerator businessIdGenerator;

    public PostCommentService(PostManager postManager,
                              PostCommentManager postCommentManager,
                              PostStatDeltaRepository postStatDeltaRepository,
                              PostCacheService postCacheService,
                              BusinessIdGenerator businessIdGenerator) {
        this.postManager = postManager;
        this.postCommentManager = postCommentManager;
        this.postStatDeltaRepository = postStatDeltaRepository;
        this.postCacheService = postCacheService;
        this.businessIdGenerator = businessIdGenerator;
    }

    public long createComment(long callerUserId, long postId, String content) {
        if (postId <= 0L) {
            throw new PostBusinessException(PostErrorCode.INVALID_ARGUMENT, "postId 非法");
        }
        postManager.findActivePost(postId)
                .orElseThrow(() -> new PostBusinessException(PostErrorCode.POST_NOT_FOUND));

        String normalized = normalizeCommentContent(content);
        long commentId = businessIdGenerator.nextId();
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        PostCommentEntity entity = new PostCommentEntity();
        entity.setCommentId(commentId);
        entity.setPostId(postId);
        entity.setUserId(callerUserId);
        entity.setRootId(0L);
        entity.setParentId(0L);
        entity.setReplyToUserId(0L);
        entity.setContent(normalized);
        entity.setStatus(CommentStatus.ACTIVE);
        entity.setDeleted(0);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        postCommentManager.insertComment(entity);

        postStatDeltaRepository.incrementCommentDelta(postId, 1);
        postStatDeltaRepository.addCommentToWindow(postId, commentId);
        postCacheService.evictDetail(postId);
        return commentId;
    }

    public ListCommentsResult listComments(long postId, String cursor, int pageSize) {
        if (postId <= 0L) {
            throw new PostBusinessException(PostErrorCode.INVALID_ARGUMENT, "postId 非法");
        }
        int resolvedPageSize = normalizePageSize(pageSize);
        long cursorCommentId = parseCursor(cursor);

        Set<Long> windowIds = postStatDeltaRepository.listCommentIdsFromWindow(
                postId, cursorCommentId, resolvedPageSize + 1);
        if (!windowIds.isEmpty()) {
            List<PostCommentEntity> windowEntities = new ArrayList<>();
            for (Long commentId : windowIds) {
                postCommentManager.findActiveComment(commentId).ifPresent(windowEntities::add);
            }
            if (!windowEntities.isEmpty()) {
                return buildResultFromEntities(windowEntities, resolvedPageSize);
            }
        }

        List<PostCommentEntity> entities = postCommentManager.listCommentsFromDb(
                postId, cursorCommentId, resolvedPageSize + 1);
        return buildResultFromEntities(entities, resolvedPageSize);
    }

    public void deleteComment(long callerUserId, long commentId) {
        if (commentId <= 0L) {
            throw new PostBusinessException(PostErrorCode.INVALID_ARGUMENT, "commentId 非法");
        }
        PostCommentEntity comment = postCommentManager.findActiveComment(commentId)
                .orElseThrow(() -> new PostBusinessException(PostErrorCode.COMMENT_NOT_FOUND));
        if (comment.getUserId() == null || !comment.getUserId().equals(callerUserId)) {
            throw new PostBusinessException(PostErrorCode.FORBIDDEN, "仅作者可删除评论");
        }

        postCommentManager.softDeleteComment(comment);
        postStatDeltaRepository.incrementCommentDelta(comment.getPostId(), -1);
        postStatDeltaRepository.removeCommentFromWindow(comment.getPostId(), commentId);
        postCacheService.evictDetail(comment.getPostId());
    }

    private ListCommentsResult buildResultFromEntities(List<PostCommentEntity> entities, int pageSize) {
        ListCommentsResult result = new ListCommentsResult();
        boolean hasMore = entities.size() > pageSize;
        List<PostCommentEntity> page = hasMore ? entities.subList(0, pageSize) : entities;
        List<CommentInfoDTO> items = new ArrayList<>();
        for (PostCommentEntity entity : page) {
            items.add(toDto(entity));
        }
        result.setItems(items);
        result.setHasMore(hasMore);
        if (hasMore && !page.isEmpty()) {
            result.setNextCursor(String.valueOf(page.get(page.size() - 1).getCommentId()));
        } else {
            result.setNextCursor("");
        }
        return result;
    }

    static CommentInfoDTO toDto(PostCommentEntity entity) {
        CommentInfoDTO dto = new CommentInfoDTO();
        dto.setCommentId(entity.getCommentId());
        dto.setPostId(entity.getPostId());
        dto.setUserId(entity.getUserId());
        dto.setContent(entity.getContent());
        dto.setCreatedAtSeconds(entity.getCreatedAt() == null ? 0L : entity.getCreatedAt().toEpochSecond());
        return dto;
    }

    static String normalizeCommentContent(String content) {
        if (content == null) {
            throw new PostBusinessException(PostErrorCode.INVALID_ARGUMENT, "content 不能为空");
        }
        String trimmed = content.trim();
        if (!StringUtils.hasText(trimmed)) {
            throw new PostBusinessException(PostErrorCode.INVALID_ARGUMENT, "content 不能为空");
        }
        if (trimmed.length() > 512) {
            throw new PostBusinessException(PostErrorCode.INVALID_ARGUMENT, "content 长度不能超过 512");
        }
        return trimmed;
    }

    static int normalizePageSize(int pageSize) {
        if (pageSize <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    static long parseCursor(String cursor) {
        if (!StringUtils.hasText(cursor) || "0".equals(cursor.trim())) {
            return 0L;
        }
        try {
            return Long.parseLong(cursor.trim());
        } catch (NumberFormatException ex) {
            throw new PostBusinessException(PostErrorCode.INVALID_ARGUMENT, "cursor 非法");
        }
    }
}
