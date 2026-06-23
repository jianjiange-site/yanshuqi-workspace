package com.dating.gateway.controller;

import com.dating.gateway.common.Result;
import com.dating.gateway.dto.CreateCommentReq;
import com.dating.gateway.dto.CreatePostReq;
import com.dating.gateway.dto.vo.CommentVO;
import com.dating.gateway.dto.vo.PostDetailVO;
import com.dating.gateway.dto.vo.PostVO;
import com.dating.gateway.resolver.CallerUserResolver;
import com.dating.gateway.service.PostBffService;
import com.dating.gateway.support.PostParamSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Post REST 入口：callerUserId 来自 JWT，经 PostGrpcClient 注入 post-service {@code x-user-id} metadata。
 */
@RestController
@RequestMapping("/api/v1/post")
@Tag(name = "Post", description = "发帖、Feed、点赞、评论")
public class PostController {

    private final CallerUserResolver callerUserResolver;
    private final PostBffService postBffService;

    public PostController(CallerUserResolver callerUserResolver, PostBffService postBffService) {
        this.callerUserResolver = callerUserResolver;
        this.postBffService = postBffService;
    }

    @PostMapping
    @Operation(summary = "发布帖子")
    public Result<Long> create(HttpServletRequest request, @RequestBody CreatePostReq req) {
        long callerUserId = callerUserResolver.resolveCallerUserId(request);
        PostParamSupport.validateCreatePost(req);
        long postId = postBffService.createPost(callerUserId, req);
        return Result.ok(postId);
    }

    @GetMapping("/feed")
    @Operation(summary = "推荐 Feed")
    public Result<List<PostVO>> feed(HttpServletRequest request,
                                     @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                     @RequestParam(value = "cursor", required = false) String cursor) {
        long callerUserId = callerUserResolver.resolveCallerUserId(request);
        List<PostVO> data = postBffService.getRecommendFeed(callerUserId, pageSize, cursor);
        return Result.ok(data);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "用户帖子列表")
    public Result<List<PostVO>> listUserPosts(HttpServletRequest request,
                                             @PathVariable("userId") Long userId,
                                             @RequestParam(value = "pageSize", defaultValue = "20") int pageSize,
                                             @RequestParam(value = "cursor", required = false) String cursor) {
        long callerUserId = callerUserResolver.resolveCallerUserId(request);
        long safeUserId = PostParamSupport.validateUserId(userId);
        List<PostVO> data = postBffService.listUserPosts(callerUserId, safeUserId, pageSize, cursor);
        return Result.ok(data);
    }

    @DeleteMapping("/comment/{commentId}")
    @Operation(summary = "删除评论")
    public Result<Boolean> deleteComment(HttpServletRequest request,
                                         @PathVariable("commentId") Long commentId) {
        long callerUserId = callerUserResolver.resolveCallerUserId(request);
        long safeCommentId = PostParamSupport.validateCommentId(commentId);
        return Result.ok(postBffService.deleteComment(callerUserId, safeCommentId));
    }

    @GetMapping("/{postId}")
    @Operation(summary = "帖子详情")
    public Result<PostDetailVO> detail(HttpServletRequest request, @PathVariable("postId") Long postId) {
        long callerUserId = callerUserResolver.resolveCallerUserId(request);
        long safePostId = PostParamSupport.validatePostId(postId);
        PostDetailVO data = postBffService.getPostDetail(callerUserId, safePostId);
        return Result.ok(data);
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "删除帖子")
    public Result<Boolean> delete(HttpServletRequest request, @PathVariable("postId") Long postId) {
        long callerUserId = callerUserResolver.resolveCallerUserId(request);
        long safePostId = PostParamSupport.validatePostId(postId);
        return Result.ok(postBffService.deletePost(callerUserId, safePostId));
    }

    @PostMapping("/{postId}/like")
    @Operation(summary = "点赞")
    public Result<Boolean> like(HttpServletRequest request, @PathVariable("postId") Long postId) {
        long callerUserId = callerUserResolver.resolveCallerUserId(request);
        long safePostId = PostParamSupport.validatePostId(postId);
        return Result.ok(postBffService.likePost(callerUserId, safePostId));
    }

    @DeleteMapping("/{postId}/like")
    @Operation(summary = "取消点赞")
    public Result<Boolean> unlike(HttpServletRequest request, @PathVariable("postId") Long postId) {
        long callerUserId = callerUserResolver.resolveCallerUserId(request);
        long safePostId = PostParamSupport.validatePostId(postId);
        return Result.ok(postBffService.unlikePost(callerUserId, safePostId));
    }

    @GetMapping("/{postId}/comment")
    @Operation(summary = "评论列表")
    public Result<List<CommentVO>> listComments(HttpServletRequest request,
                                                @PathVariable("postId") Long postId,
                                                @RequestParam(value = "pageSize", defaultValue = "20") int pageSize,
                                                @RequestParam(value = "cursor", required = false) String cursor) {
        long callerUserId = callerUserResolver.resolveCallerUserId(request);
        long safePostId = PostParamSupport.validatePostId(postId);
        List<CommentVO> data = postBffService.listComments(callerUserId, safePostId, pageSize, cursor);
        return Result.ok(data);
    }

    @PostMapping("/{postId}/comment")
    @Operation(summary = "发表评论")
    public Result<Long> createComment(HttpServletRequest request,
                                     @PathVariable("postId") Long postId,
                                     @RequestBody CreateCommentReq req) {
        long callerUserId = callerUserResolver.resolveCallerUserId(request);
        long safePostId = PostParamSupport.validatePostId(postId);
        PostParamSupport.validateCreateComment(safePostId, req);
        long commentId = postBffService.createComment(callerUserId, safePostId, req);
        return Result.ok(commentId);
    }
}
