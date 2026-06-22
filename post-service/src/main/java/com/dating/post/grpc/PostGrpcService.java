package com.dating.post.grpc;

import com.dating.post.dto.CommentInfoDTO;
import com.dating.post.dto.FeedResult;
import com.dating.post.dto.ListCommentsResult;
import com.dating.post.dto.ListUserPostsResult;
import com.dating.post.dto.PostCreateCommand;
import com.dating.post.dto.PostInfoDTO;
import com.dating.post.exception.PostBusinessException;
import com.dating.post.exception.PostErrorCode;
import com.dating.post.grpc.proto.ActionLikeRequest;
import com.dating.post.grpc.proto.ActionLikeResponse;
import com.dating.post.grpc.proto.CommentInfo;
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
import com.dating.post.grpc.proto.PostInfo;
import com.dating.post.grpc.proto.PostServiceGrpc;
import com.dating.post.service.FeedService;
import com.dating.post.service.PostCommentService;
import com.dating.post.service.PostLikeService;
import com.dating.post.service.PostReadService;
import com.dating.post.service.PostWriteService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.context.annotation.Profile;

import java.time.Instant;
import java.util.List;

/**
 * Post gRPC 服务实现。
 * <p>
 * <b>阶段 4 说明：</b>9 个 RPC 均已接入真实 Service；GetRecommendFeed 为三路 Feed 混排。
 */
@GrpcService
@Profile("!test")
public class PostGrpcService extends PostServiceGrpc.PostServiceImplBase {

    public static final int SUCCESS_CODE = 0;
    public static final String SUCCESS_MESSAGE = "OK";
    public static final long MOCK_POST_ID = 10001L;
    public static final long MOCK_AUTHOR_USER_ID = 30001L;

    private final PostWriteService postWriteService;
    private final PostReadService postReadService;
    private final PostLikeService postLikeService;
    private final PostCommentService postCommentService;
    private final FeedService feedService;

    public PostGrpcService(PostWriteService postWriteService,
                           PostReadService postReadService,
                           PostLikeService postLikeService,
                           PostCommentService postCommentService,
                           FeedService feedService) {
        this.postWriteService = postWriteService;
        this.postReadService = postReadService;
        this.postLikeService = postLikeService;
        this.postCommentService = postCommentService;
        this.feedService = feedService;
    }

    @Override
    public void createPost(CreatePostRequest request, StreamObserver<CreatePostResponse> responseObserver) {
        long callerUserId = GrpcUserContext.requireUserId();
        PostCreateCommand command = PostReadService.toCreateCommand(
                callerUserId, request.getContent(), request.getImageKeysList());
        long postId = postWriteService.createPost(command);
        responseObserver.onNext(CreatePostResponse.newBuilder()
                .setCode(SUCCESS_CODE)
                .setMessage(SUCCESS_MESSAGE)
                .setPostId(postId)
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void getPostDetail(GetPostDetailRequest request, StreamObserver<GetPostDetailResponse> responseObserver) {
        Long callerUserId = GrpcUserContext.getUserId();
        PostInfoDTO detail = postReadService.getPostDetail(request.getPostId(), callerUserId);
        responseObserver.onNext(GetPostDetailResponse.newBuilder()
                .setCode(SUCCESS_CODE)
                .setMessage(SUCCESS_MESSAGE)
                .setPost(toProto(detail))
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void listUserPosts(ListUserPostsRequest request, StreamObserver<ListUserPostsResponse> responseObserver) {
        Long callerUserId = GrpcUserContext.getUserId();
        ListUserPostsResult result = postReadService.listUserPosts(
                request.getUserId(), request.getCursor(), request.getPageSize(), callerUserId);
        ListUserPostsResponse.Builder builder = ListUserPostsResponse.newBuilder()
                .setCode(SUCCESS_CODE)
                .setMessage(SUCCESS_MESSAGE)
                .setNextCursor(result.getNextCursor() == null ? "" : result.getNextCursor())
                .setHasMore(result.isHasMore());
        for (PostInfoDTO item : result.getItems()) {
            builder.addItems(toProto(item));
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void actionLike(ActionLikeRequest request, StreamObserver<ActionLikeResponse> responseObserver) {
        long callerUserId = GrpcUserContext.requireUserId();
        if (request.getPostId() <= 0L) {
            throw new PostBusinessException(PostErrorCode.INVALID_ARGUMENT, "postId 非法");
        }
        boolean success = postLikeService.actionLike(callerUserId, request.getPostId(), request.getLike());
        responseObserver.onNext(ActionLikeResponse.newBuilder()
                .setCode(SUCCESS_CODE)
                .setMessage(SUCCESS_MESSAGE)
                .setSuccess(success)
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void createComment(CreateCommentRequest request, StreamObserver<CreateCommentResponse> responseObserver) {
        long callerUserId = GrpcUserContext.requireUserId();
        long commentId = postCommentService.createComment(callerUserId, request.getPostId(), request.getContent());
        responseObserver.onNext(CreateCommentResponse.newBuilder()
                .setCode(SUCCESS_CODE)
                .setMessage(SUCCESS_MESSAGE)
                .setCommentId(commentId)
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void listComments(ListCommentsRequest request, StreamObserver<ListCommentsResponse> responseObserver) {
        if (request.getPostId() <= 0L) {
            throw new PostBusinessException(PostErrorCode.INVALID_ARGUMENT, "postId 非法");
        }
        ListCommentsResult result = postCommentService.listComments(
                request.getPostId(), request.getCursor(), request.getPageSize());
        ListCommentsResponse.Builder builder = ListCommentsResponse.newBuilder()
                .setCode(SUCCESS_CODE)
                .setMessage(SUCCESS_MESSAGE)
                .setNextCursor(result.getNextCursor() == null ? "" : result.getNextCursor())
                .setHasMore(result.isHasMore());
        for (CommentInfoDTO item : result.getItems()) {
            builder.addItems(toProto(item));
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void deleteComment(DeleteCommentRequest request, StreamObserver<DeleteCommentResponse> responseObserver) {
        long callerUserId = GrpcUserContext.requireUserId();
        postCommentService.deleteComment(callerUserId, request.getCommentId());
        responseObserver.onNext(DeleteCommentResponse.newBuilder()
                .setCode(SUCCESS_CODE)
                .setMessage(SUCCESS_MESSAGE)
                .setSuccess(true)
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void deletePost(DeletePostRequest request, StreamObserver<DeletePostResponse> responseObserver) {
        long callerUserId = GrpcUserContext.requireUserId();
        postWriteService.deletePost(callerUserId, request.getPostId());
        responseObserver.onNext(DeletePostResponse.newBuilder()
                .setCode(SUCCESS_CODE)
                .setMessage(SUCCESS_MESSAGE)
                .setSuccess(true)
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void getRecommendFeed(GetRecommendFeedRequest request, StreamObserver<GetRecommendFeedResponse> responseObserver) {
        long callerUserId = GrpcUserContext.requireUserId();
        FeedResult feedResult = feedService.getRecommendFeed(
                callerUserId, request.getPageSize(), request.getCursor());
        GetRecommendFeedResponse.Builder builder = GetRecommendFeedResponse.newBuilder()
                .setCode(SUCCESS_CODE)
                .setMessage(SUCCESS_MESSAGE)
                .setNextCursor(feedResult.getNextCursor() == null ? "" : feedResult.getNextCursor())
                .setHasMore(feedResult.isHasMore());
        for (PostInfoDTO item : feedResult.getItems()) {
            builder.addItems(toProto(item));
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    static PostInfo toProto(PostInfoDTO dto) {
        return PostInfo.newBuilder()
                .setPostId(dto.getPostId())
                .setUserId(dto.getUserId())
                .setContent(dto.getContent())
                .addAllImageKeys(dto.getImageKeys())
                .setLikeCount(dto.getLikeCount())
                .setCommentCount(dto.getCommentCount())
                .setIsLiked(dto.isLiked())
                .setCreatedAtSeconds(dto.getCreatedAtSeconds())
                .build();
    }

    static CommentInfo toProto(CommentInfoDTO dto) {
        return CommentInfo.newBuilder()
                .setCommentId(dto.getCommentId())
                .setPostId(dto.getPostId())
                .setUserId(dto.getUserId())
                .setContent(dto.getContent())
                .setCreatedAtSeconds(dto.getCreatedAtSeconds())
                .build();
    }

    public static List<PostInfo> buildMockFeedItems() {
        PostInfoDTO dto = new PostInfoDTO();
        dto.setPostId(MOCK_POST_ID);
        dto.setUserId(MOCK_AUTHOR_USER_ID);
        dto.setContent("阶段 mock Feed 帖子");
        dto.getImageKeys().add("post/30001/202606/mock-feed.jpg");
        dto.setLikeCount(0);
        dto.setCommentCount(0);
        dto.setLiked(false);
        dto.setCreatedAtSeconds(Instant.now().getEpochSecond());
        return List.of(toProto(dto));
    }
}
