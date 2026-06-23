package com.dating.gateway.client;

import com.dating.gateway.exception.GatewayBizException;
import com.dating.gateway.exception.GatewayErrorCode;
import com.dating.gateway.exception.GatewayGrpcExceptionMapper;
import com.dating.post.grpc.proto.ActionLikeRequest;
import com.dating.post.grpc.proto.ActionLikeResponse;
import com.dating.post.grpc.proto.CreateCommentRequest;
import com.dating.post.grpc.proto.CreateCommentResponse;
import com.dating.post.grpc.proto.CreatePostRequest;
import com.dating.post.grpc.proto.CreatePostResponse;
import com.dating.post.grpc.proto.DeleteCommentRequest;
import com.dating.post.grpc.proto.DeleteCommentResponse;
import com.dating.post.grpc.proto.DeletePostRequest;
import com.dating.post.grpc.proto.DeletePostResponse;
import com.dating.post.grpc.proto.GetPostDetailRequest;
import com.dating.post.grpc.proto.GetPostDetailResponse;
import com.dating.post.grpc.proto.GetRecommendFeedRequest;
import com.dating.post.grpc.proto.GetRecommendFeedResponse;
import com.dating.post.grpc.proto.ListCommentsRequest;
import com.dating.post.grpc.proto.ListCommentsResponse;
import com.dating.post.grpc.proto.ListUserPostsRequest;
import com.dating.post.grpc.proto.ListUserPostsResponse;
import com.dating.post.grpc.proto.PostServiceGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.MetadataUtils;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * post-service gRPC 客户端：每次调用注入 {@code x-user-id} metadata，callerUserId 来自 JWT。
 */
@Component
@Profile("!test")
public class PostGrpcClient {

    @GrpcClient("post-service")
    private PostServiceGrpc.PostServiceBlockingStub postServiceStub;

    @Value("${gateway.grpc.deadline-seconds:5}")
    private long deadlineSeconds;

    public CreatePostResponse createPost(long callerUserId, CreatePostRequest request) {
        return invoke(callerUserId, stub -> stub.createPost(request));
    }

    public GetPostDetailResponse getPostDetail(long callerUserId, GetPostDetailRequest request) {
        return invoke(callerUserId, stub -> stub.getPostDetail(request));
    }

    public DeletePostResponse deletePost(long callerUserId, DeletePostRequest request) {
        return invoke(callerUserId, stub -> stub.deletePost(request));
    }

    public GetRecommendFeedResponse getRecommendFeed(long callerUserId, GetRecommendFeedRequest request) {
        return invoke(callerUserId, stub -> stub.getRecommendFeed(request));
    }

    public ListUserPostsResponse listUserPosts(long callerUserId, ListUserPostsRequest request) {
        return invoke(callerUserId, stub -> stub.listUserPosts(request));
    }

    public ActionLikeResponse actionLike(long callerUserId, ActionLikeRequest request) {
        return invoke(callerUserId, stub -> stub.actionLike(request));
    }

    public CreateCommentResponse createComment(long callerUserId, CreateCommentRequest request) {
        return invoke(callerUserId, stub -> stub.createComment(request));
    }

    public ListCommentsResponse listComments(long callerUserId, ListCommentsRequest request) {
        return invoke(callerUserId, stub -> stub.listComments(request));
    }

    public DeleteCommentResponse deleteComment(long callerUserId, DeleteCommentRequest request) {
        return invoke(callerUserId, stub -> stub.deleteComment(request));
    }

    private <T> T invoke(long callerUserId, GrpcCall<T> call) {
        PostServiceGrpc.PostServiceBlockingStub stub = stubWithMetadata(callerUserId);
        try {
            return call.execute(stub);
        } catch (StatusRuntimeException ex) {
            throw mapPostException(ex);
        }
    }

    /**
     * 为 stub 附加 deadline 与 JWT 解析出的 metadata（含 x-user-id）。
     */
    PostServiceGrpc.PostServiceBlockingStub stubWithMetadata(long callerUserId) {
        PostServiceGrpc.PostServiceBlockingStub deadlineStub = postServiceStub
                .withDeadlineAfter(deadlineSeconds, TimeUnit.SECONDS);
        return deadlineStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(
                PostGrpcMetadataSupport.buildMetadata(callerUserId)));
    }

    /**
     * post 域 NOT_FOUND 细化为 POST_NOT_FOUND / COMMENT_NOT_FOUND，其余复用全局映射。
     */
    GatewayBizException mapPostException(StatusRuntimeException ex) {
        Status.Code code = ex.getStatus().getCode();
        String description = ex.getStatus().getDescription();
        if (code == Status.Code.NOT_FOUND && StringUtils.hasText(description)) {
            if ("COMMENT_NOT_FOUND".equals(description)) {
                return new GatewayBizException(GatewayErrorCode.COMMENT_NOT_FOUND, description);
            }
            if ("POST_NOT_FOUND".equals(description)) {
                return new GatewayBizException(GatewayErrorCode.POST_NOT_FOUND, description);
            }
        }
        return GatewayGrpcExceptionMapper.toGatewayException(ex);
    }

    @FunctionalInterface
    interface GrpcCall<T> {
        T execute(PostServiceGrpc.PostServiceBlockingStub stub);
    }
}
