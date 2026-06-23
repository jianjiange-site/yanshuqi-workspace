package com.dating.gateway.support;

import com.dating.gateway.dto.CreateCommentReq;
import com.dating.gateway.dto.CreatePostReq;
import com.dating.gateway.exception.GatewayBizException;
import com.dating.gateway.exception.GatewayErrorCode;
import org.springframework.util.StringUtils;

/**
 * Post REST 入参边界与轻量校验；发帖/点赞/评论业务逻辑在 post-service。
 */
public final class PostParamSupport {

    public static final int DEFAULT_FEED_PAGE_SIZE = 10;
    public static final int DEFAULT_LIST_PAGE_SIZE = 20;
    public static final int MIN_PAGE_SIZE = 1;
    public static final int MAX_PAGE_SIZE = 50;

    private PostParamSupport() {
    }

    public static int clampFeedPageSize(int pageSize) {
        if (pageSize <= 0) {
            return DEFAULT_FEED_PAGE_SIZE;
        }
        return Math.min(Math.max(pageSize, MIN_PAGE_SIZE), MAX_PAGE_SIZE);
    }

    public static int clampListPageSize(int pageSize) {
        if (pageSize <= 0) {
            return DEFAULT_LIST_PAGE_SIZE;
        }
        return Math.min(Math.max(pageSize, MIN_PAGE_SIZE), MAX_PAGE_SIZE);
    }

    public static long validatePostId(Long postId) {
        if (postId == null || postId <= 0) {
            throw new GatewayBizException(GatewayErrorCode.INVALID_ARGUMENT, "postId 非法");
        }
        return postId;
    }

    public static long validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new GatewayBizException(GatewayErrorCode.INVALID_ARGUMENT, "userId 非法");
        }
        return userId;
    }

    public static long validateCommentId(Long commentId) {
        if (commentId == null || commentId <= 0) {
            throw new GatewayBizException(GatewayErrorCode.INVALID_ARGUMENT, "commentId 非法");
        }
        return commentId;
    }

    public static void validateCreatePost(CreatePostReq req) {
        if (req == null || !StringUtils.hasText(req.getContent()) || !StringUtils.hasText(req.getContent().trim())) {
            throw new GatewayBizException(GatewayErrorCode.INVALID_ARGUMENT, "content 不能为空");
        }
    }

    public static void validateCreateComment(long pathPostId, CreateCommentReq req) {
        if (req == null || !StringUtils.hasText(req.getContent()) || !StringUtils.hasText(req.getContent().trim())) {
            throw new GatewayBizException(GatewayErrorCode.INVALID_ARGUMENT, "content 不能为空");
        }
        if (req.getPostId() != null && !req.getPostId().equals(pathPostId)) {
            throw new GatewayBizException(GatewayErrorCode.INVALID_ARGUMENT, "path postId 与 body postId 不一致");
        }
    }
}
