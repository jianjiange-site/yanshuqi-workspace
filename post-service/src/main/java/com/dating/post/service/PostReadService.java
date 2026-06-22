package com.dating.post.service;

import com.dating.post.dto.ListUserPostsResult;
import com.dating.post.dto.PostCreateCommand;
import com.dating.post.dto.PostInfoDTO;
import com.dating.post.entity.PostEntity;
import com.dating.post.entity.PostImageEntity;
import com.dating.post.entity.PostStatEntity;
import com.dating.post.exception.PostBusinessException;
import com.dating.post.exception.PostErrorCode;
import com.dating.post.manager.PostManager;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 帖子读业务：详情与用户帖子列表。
 */
@Service
public class PostReadService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;

    private final PostManager postManager;
    private final PostCacheService postCacheService;
    private final PostStatReadService postStatReadService;

    public PostReadService(PostManager postManager,
                           PostCacheService postCacheService,
                           PostStatReadService postStatReadService) {
        this.postManager = postManager;
        this.postCacheService = postCacheService;
        this.postStatReadService = postStatReadService;
    }

    public PostInfoDTO getPostDetail(long postId) {
        return getPostDetail(postId, null);
    }

    public PostInfoDTO getPostDetail(long postId, Long callerUserId) {
        if (postId <= 0L) {
            throw new PostBusinessException(PostErrorCode.INVALID_ARGUMENT, "postId 非法");
        }

        Optional<PostInfoDTO> cached = postCacheService.getDetail(postId);
        PostInfoDTO dto = cached.orElseGet(() -> {
            PostInfoDTO fromDb = loadFromDb(postId)
                    .orElseThrow(() -> new PostBusinessException(PostErrorCode.POST_NOT_FOUND));
            postCacheService.putDetail(fromDb);
            return fromDb;
        });
        enrichRealtimeFields(dto, callerUserId);
        return dto;
    }

    public ListUserPostsResult listUserPosts(long targetUserId, String cursor, int pageSize) {
        return listUserPosts(targetUserId, cursor, pageSize, null);
    }

    public ListUserPostsResult listUserPosts(long targetUserId, String cursor, int pageSize, Long callerUserId) {
        if (targetUserId <= 0L) {
            throw new PostBusinessException(PostErrorCode.INVALID_ARGUMENT, "userId 非法");
        }
        int resolvedPageSize = normalizePageSize(pageSize);
        long cursorPostId = parseCursor(cursor);

        List<PostEntity> posts = postManager.listActivePostsByUser(
                targetUserId, cursorPostId, resolvedPageSize + 1);

        ListUserPostsResult result = new ListUserPostsResult();
        boolean hasMore = posts.size() > resolvedPageSize;
        List<PostEntity> pagePosts = hasMore ? posts.subList(0, resolvedPageSize) : posts;

        List<PostInfoDTO> items = new ArrayList<>();
        for (PostEntity post : pagePosts) {
            PostInfoDTO dto = assemblePostInfo(post, callerUserId);
            items.add(dto);
        }
        result.setItems(items);
        result.setHasMore(hasMore);
        if (hasMore && !pagePosts.isEmpty()) {
            result.setNextCursor(String.valueOf(pagePosts.get(pagePosts.size() - 1).getPostId()));
        } else {
            result.setNextCursor("");
        }
        return result;
    }

    PostInfoDTO assemblePostInfo(PostEntity post) {
        return assemblePostInfo(post, null);
    }

    PostInfoDTO assemblePostInfo(PostEntity post, Long callerUserId) {
        List<String> imageKeys = postManager.listImages(post.getPostId()).stream()
                .map(PostImageEntity::getImageKey)
                .collect(Collectors.toList());
        PostStatEntity stat = postManager.findStat(post.getPostId()).orElse(null);
        PostInfoDTO dto = toDto(post, imageKeys, stat);
        enrichRealtimeFields(dto, callerUserId);
        return dto;
    }

    private void enrichRealtimeFields(PostInfoDTO dto, Long callerUserId) {
        PostStatEntity stat = postManager.findStat(dto.getPostId()).orElse(null);
        dto.setLikeCount(postStatReadService.getRealLikeCount(dto.getPostId(), stat));
        dto.setCommentCount(postStatReadService.getRealCommentCount(dto.getPostId(), stat));
        if (callerUserId != null && callerUserId > 0L) {
            dto.setLiked(postStatReadService.isLiked(callerUserId, dto.getPostId()));
        } else {
            dto.setLiked(false);
        }
    }

    Optional<PostInfoDTO> loadFromDb(long postId) {
        return postManager.findActivePost(postId)
                .map(post -> assemblePostInfo(post, null));
    }

    static PostInfoDTO toDto(PostEntity post, List<String> imageKeys, PostStatEntity stat) {
        PostInfoDTO dto = new PostInfoDTO();
        dto.setPostId(post.getPostId());
        dto.setUserId(post.getUserId());
        dto.setContent(post.getContent());
        dto.setImageKeys(imageKeys);
        dto.setLikeCount(stat == null ? 0 : stat.getLikeCount());
        dto.setCommentCount(stat == null ? 0 : stat.getCommentCount());
        dto.setLiked(false);
        dto.setCreatedAtSeconds(post.getCreatedAt() == null ? 0L : post.getCreatedAt().toEpochSecond());
        return dto;
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

    static List<String> normalizeImageKeys(List<String> imageKeys) {
        if (imageKeys == null || imageKeys.isEmpty()) {
            return List.of();
        }
        if (imageKeys.size() > 9) {
            throw new PostBusinessException(PostErrorCode.INVALID_ARGUMENT, "图片最多 9 张");
        }
        List<String> normalized = new ArrayList<>();
        for (String key : imageKeys) {
            if (!StringUtils.hasText(key)) {
                throw new PostBusinessException(PostErrorCode.INVALID_ARGUMENT, "imageKey 不能为空");
            }
            normalized.add(key.trim());
        }
        return normalized;
    }

    static String normalizeContent(String content) {
        if (content == null) {
            throw new PostBusinessException(PostErrorCode.INVALID_ARGUMENT, "content 不能为空");
        }
        String trimmed = content.trim();
        if (!StringUtils.hasText(trimmed)) {
            throw new PostBusinessException(PostErrorCode.INVALID_ARGUMENT, "content 不能为空");
        }
        if (trimmed.length() > 1024) {
            throw new PostBusinessException(PostErrorCode.INVALID_ARGUMENT, "content 长度不能超过 1024");
        }
        return trimmed;
    }

    public static PostCreateCommand toCreateCommand(long callerUserId, String content, List<String> imageKeys) {
        PostCreateCommand command = new PostCreateCommand();
        command.setCallerUserId(callerUserId);
        command.setContent(normalizeContent(content));
        command.setImageKeys(normalizeImageKeys(imageKeys));
        return command;
    }
}
