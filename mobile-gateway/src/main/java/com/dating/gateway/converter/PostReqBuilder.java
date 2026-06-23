package com.dating.gateway.converter;

import com.dating.gateway.dto.CreateCommentReq;
import com.dating.gateway.dto.CreatePostReq;
import com.dating.post.grpc.proto.ActionLikeRequest;
import com.dating.post.grpc.proto.CreateCommentRequest;
import com.dating.post.grpc.proto.CreatePostRequest;
import com.dating.post.grpc.proto.DeleteCommentRequest;
import com.dating.post.grpc.proto.DeletePostRequest;
import com.dating.post.grpc.proto.GetPostDetailRequest;
import com.dating.post.grpc.proto.GetRecommendFeedRequest;
import com.dating.post.grpc.proto.ListCommentsRequest;
import com.dating.post.grpc.proto.ListUserPostsRequest;

/**
 * REST DTO → post proto request 手写 builder；callerUserId 不在 proto 中，由 gRPC metadata 传递。
 */
public final class PostReqBuilder {

    private PostReqBuilder() {
    }

    public static CreatePostRequest buildCreatePost(CreatePostReq req) {
        CreatePostRequest.Builder builder = CreatePostRequest.newBuilder()
                .setContent(req.getContent());
        if (req.getImageKeys() != null) {
            builder.addAllImageKeys(req.getImageKeys());
        }
        return builder.build();
    }

    public static GetPostDetailRequest buildGetPostDetail(long postId) {
        return GetPostDetailRequest.newBuilder().setPostId(postId).build();
    }

    public static DeletePostRequest buildDeletePost(long postId) {
        return DeletePostRequest.newBuilder().setPostId(postId).build();
    }

    public static GetRecommendFeedRequest buildGetRecommendFeed(String cursor, int pageSize) {
        return GetRecommendFeedRequest.newBuilder()
                .setCursor(cursor == null ? "" : cursor)
                .setPageSize(pageSize)
                .build();
    }

    public static ListUserPostsRequest buildListUserPosts(long userId, String cursor, int pageSize) {
        return ListUserPostsRequest.newBuilder()
                .setUserId(userId)
                .setCursor(normalizeUserPostsCursor(cursor))
                .setPageSize(pageSize)
                .build();
    }

    public static ActionLikeRequest buildActionLike(long postId, boolean like) {
        return ActionLikeRequest.newBuilder()
                .setPostId(postId)
                .setLike(like)
                .build();
    }

    public static CreateCommentRequest buildCreateComment(long postId, String content) {
        return CreateCommentRequest.newBuilder()
                .setPostId(postId)
                .setContent(content)
                .build();
    }

    public static ListCommentsRequest buildListComments(long postId, String cursor, int pageSize) {
        return ListCommentsRequest.newBuilder()
                .setPostId(postId)
                .setCursor(normalizeUserPostsCursor(cursor))
                .setPageSize(pageSize)
                .build();
    }

    public static DeleteCommentRequest buildDeleteComment(long commentId) {
        return DeleteCommentRequest.newBuilder().setCommentId(commentId).build();
    }

    /** 用户帖子/评论列表游标：空或 null 视为 "0"（post-service 约定）。 */
    static String normalizeUserPostsCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return "0";
        }
        return cursor.trim();
    }
}
