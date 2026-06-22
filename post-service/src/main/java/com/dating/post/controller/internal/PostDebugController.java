package com.dating.post.controller.internal;

import com.dating.post.dto.CommentInfoDTO;
import com.dating.post.dto.FeedResult;
import com.dating.post.dto.ListCommentsResult;
import com.dating.post.dto.ListUserPostsResult;
import com.dating.post.dto.PostCreateCommand;
import com.dating.post.dto.PostInfoDTO;
import com.dating.post.grpc.PostGrpcService;
import com.dating.post.service.FeedPoolRebuildService;
import com.dating.post.service.FeedService;
import com.dating.post.service.PostCommentService;
import com.dating.post.service.PostLikeService;
import com.dating.post.service.PostReadService;
import com.dating.post.service.PostWriteService;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Post 本地调试 REST 入口。
 * <p>
 * mobile-gateway 未开发时用于本地验收；不等同于正式 Swagger 接口，仅在 dev/test profile 暴露，不是公网 API。
 */
@RestController
@RequestMapping("/internal/debug/post")
@Profile({"dev", "test"})
public class PostDebugController {

    private final PostWriteService postWriteService;
    private final PostReadService postReadService;
    private final PostLikeService postLikeService;
    private final PostCommentService postCommentService;
    private final FeedService feedService;
    private final FeedPoolRebuildService feedPoolRebuildService;

    public PostDebugController(PostWriteService postWriteService,
                               PostReadService postReadService,
                               PostLikeService postLikeService,
                               PostCommentService postCommentService,
                               FeedService feedService,
                               FeedPoolRebuildService feedPoolRebuildService) {
        this.postWriteService = postWriteService;
        this.postReadService = postReadService;
        this.postLikeService = postLikeService;
        this.postCommentService = postCommentService;
        this.feedService = feedService;
        this.feedPoolRebuildService = feedPoolRebuildService;
    }

    @PostMapping
    public Map<String, Object> createPost(@RequestHeader(value = "x-user-id", required = false) Long callerUserId,
                                          @RequestParam(value = "callerUserId", required = false) Long callerUserIdQuery,
                                          @RequestBody CreatePostBody body) {
        long userId = resolveCallerUserId(callerUserId, callerUserIdQuery);
        PostCreateCommand command = PostReadService.toCreateCommand(
                userId, body.getContent(), body.getImageKeys());
        long postId = postWriteService.createPost(command);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("postId", postId);
        result.put("callerUserId", userId);
        return result;
    }

    @GetMapping("/{postId}")
    public Map<String, Object> getPostDetail(@PathVariable long postId,
                                             @RequestHeader(value = "x-user-id", required = false) Long callerUserId,
                                             @RequestParam(value = "callerUserId", required = false) Long callerUserIdQuery) {
        Long resolvedCaller = resolveOptionalCaller(callerUserId, callerUserIdQuery);
        PostInfoDTO detail = postReadService.getPostDetail(postId, resolvedCaller);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("post", toPostMap(detail));
        return result;
    }

    @GetMapping("/user/{userId}")
    public Map<String, Object> listUserPosts(@PathVariable long userId,
                                             @RequestParam(defaultValue = "0") String cursor,
                                             @RequestParam(defaultValue = "20") int pageSize,
                                             @RequestHeader(value = "x-user-id", required = false) Long callerUserId,
                                             @RequestParam(value = "callerUserId", required = false) Long callerUserIdQuery) {
        Long resolvedCaller = resolveOptionalCaller(callerUserId, callerUserIdQuery);
        ListUserPostsResult listResult = postReadService.listUserPosts(userId, cursor, pageSize, resolvedCaller);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("items", listResult.getItems().stream().map(this::toPostMap).collect(Collectors.toList()));
        result.put("nextCursor", listResult.getNextCursor());
        result.put("hasMore", listResult.isHasMore());
        return result;
    }

    @DeleteMapping("/{postId}")
    public Map<String, Object> deletePost(@PathVariable long postId,
                                          @RequestHeader(value = "x-user-id", required = false) Long callerUserId,
                                          @RequestParam(value = "callerUserId", required = false) Long callerUserIdQuery) {
        long userId = resolveCallerUserId(callerUserId, callerUserIdQuery);
        postWriteService.deletePost(userId, postId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("postId", postId);
        return result;
    }

    @PostMapping("/{postId}/like")
    public Map<String, Object> likePost(@PathVariable long postId,
                                        @RequestHeader(value = "x-user-id", required = false) Long callerUserId,
                                        @RequestParam(value = "callerUserId", required = false) Long callerUserIdQuery) {
        long userId = resolveCallerUserId(callerUserId, callerUserIdQuery);
        boolean success = postLikeService.actionLike(userId, postId, true);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", success);
        result.put("postId", postId);
        return result;
    }

    @DeleteMapping("/{postId}/like")
    public Map<String, Object> unlikePost(@PathVariable long postId,
                                          @RequestHeader(value = "x-user-id", required = false) Long callerUserId,
                                          @RequestParam(value = "callerUserId", required = false) Long callerUserIdQuery) {
        long userId = resolveCallerUserId(callerUserId, callerUserIdQuery);
        boolean success = postLikeService.actionLike(userId, postId, false);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", success);
        result.put("postId", postId);
        return result;
    }

    @PostMapping("/{postId}/comment")
    public Map<String, Object> createComment(@PathVariable long postId,
                                            @RequestHeader(value = "x-user-id", required = false) Long callerUserId,
                                            @RequestParam(value = "callerUserId", required = false) Long callerUserIdQuery,
                                            @RequestBody CommentBody body) {
        long userId = resolveCallerUserId(callerUserId, callerUserIdQuery);
        long commentId = postCommentService.createComment(userId, postId, body.getContent());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("commentId", commentId);
        result.put("postId", postId);
        return result;
    }

    @GetMapping("/{postId}/comment")
    public Map<String, Object> listComments(@PathVariable long postId,
                                            @RequestParam(defaultValue = "0") String cursor,
                                            @RequestParam(defaultValue = "20") int pageSize) {
        ListCommentsResult listResult = postCommentService.listComments(postId, cursor, pageSize);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("items", listResult.getItems().stream().map(this::toCommentMap).collect(Collectors.toList()));
        result.put("nextCursor", listResult.getNextCursor());
        result.put("hasMore", listResult.isHasMore());
        return result;
    }

    @DeleteMapping("/comment/{commentId}")
    public Map<String, Object> deleteComment(@PathVariable long commentId,
                                             @RequestHeader(value = "x-user-id", required = false) Long callerUserId,
                                             @RequestParam(value = "callerUserId", required = false) Long callerUserIdQuery) {
        long userId = resolveCallerUserId(callerUserId, callerUserIdQuery);
        postCommentService.deleteComment(userId, commentId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("commentId", commentId);
        return result;
    }

    /**
     * Feed 推荐本地验收：无 gateway 阶段调试入口，不是公网正式 API。
     */
    @GetMapping("/feed")
    public Map<String, Object> getRecommendFeed(@RequestHeader(value = "x-user-id", required = false) Long callerUserId,
                                                @RequestParam(value = "callerUserId", required = false) Long callerUserIdQuery,
                                                @RequestParam(defaultValue = "0") String cursor,
                                                @RequestParam(defaultValue = "10") int pageSize) {
        long userId = resolveCallerUserId(callerUserId, callerUserIdQuery);
        FeedResult feedResult = feedService.getRecommendFeed(userId, pageSize, cursor);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("items", feedResult.getItems().stream().map(this::toPostMap).collect(Collectors.toList()));
        result.put("nextCursor", feedResult.getNextCursor());
        result.put("hasMore", feedResult.isHasMore());
        return result;
    }

    /**
     * 手动触发热门池重建，仅 dev/test 本地调试用。
     */
    @PostMapping("/feed/rebuild")
    public Map<String, Object> rebuildFeedPool() {
        feedPoolRebuildService.rebuildAllRecommendPools();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("message", "热门推荐池重建已触发");
        return result;
    }

    @GetMapping("/mock-feed")
    public Map<String, Object> mockFeed() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("stage", "phase-4-real-feed");
        body.put("note", "请改用 GET /internal/debug/post/feed 验收真实 Feed");
        body.put("items", PostGrpcService.buildMockFeedItems().stream()
                .map(proto -> toPostMap(fromProto(proto)))
                .collect(Collectors.toList()));
        body.put("hasMore", false);
        body.put("nextCursor", "");
        return body;
    }

    private long resolveCallerUserId(Long headerUserId, Long queryUserId) {
        if (headerUserId != null && headerUserId > 0L) {
            return headerUserId;
        }
        if (queryUserId != null && queryUserId > 0L) {
            return queryUserId;
        }
        throw new IllegalArgumentException("缺少 callerUserId，请通过 Header x-user-id 或 query callerUserId 传入");
    }

    private Long resolveOptionalCaller(Long headerUserId, Long queryUserId) {
        if (headerUserId != null && headerUserId > 0L) {
            return headerUserId;
        }
        if (queryUserId != null && queryUserId > 0L) {
            return queryUserId;
        }
        return null;
    }

    private Map<String, Object> toPostMap(PostInfoDTO post) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("postId", post.getPostId());
        map.put("userId", post.getUserId());
        map.put("content", post.getContent());
        map.put("imageKeys", post.getImageKeys());
        map.put("likeCount", post.getLikeCount());
        map.put("commentCount", post.getCommentCount());
        map.put("isLiked", post.isLiked());
        map.put("createdAtSeconds", post.getCreatedAtSeconds());
        return map;
    }

    private Map<String, Object> toCommentMap(CommentInfoDTO comment) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("commentId", comment.getCommentId());
        map.put("postId", comment.getPostId());
        map.put("userId", comment.getUserId());
        map.put("content", comment.getContent());
        map.put("createdAtSeconds", comment.getCreatedAtSeconds());
        return map;
    }

    private PostInfoDTO fromProto(com.dating.post.grpc.proto.PostInfo proto) {
        PostInfoDTO dto = new PostInfoDTO();
        dto.setPostId(proto.getPostId());
        dto.setUserId(proto.getUserId());
        dto.setContent(proto.getContent());
        dto.setImageKeys(proto.getImageKeysList());
        dto.setLikeCount(proto.getLikeCount());
        dto.setCommentCount(proto.getCommentCount());
        dto.setLiked(proto.getIsLiked());
        dto.setCreatedAtSeconds(proto.getCreatedAtSeconds());
        return dto;
    }

    public static class CreatePostBody {
        private String content;
        private List<String> imageKeys;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public List<String> getImageKeys() {
            return imageKeys;
        }

        public void setImageKeys(List<String> imageKeys) {
            this.imageKeys = imageKeys;
        }
    }

    public static class CommentBody {
        private String content;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
