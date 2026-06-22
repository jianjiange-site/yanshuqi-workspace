package com.dating.post.grpc;

import com.dating.post.dto.FeedResult;
import com.dating.post.dto.ListUserPostsResult;
import com.dating.post.dto.PostCreateCommand;
import com.dating.post.dto.PostInfoDTO;
import com.dating.post.exception.PostBusinessException;
import com.dating.post.exception.PostErrorCode;
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
import com.dating.post.grpc.proto.GetRecommendFeedRequest;
import com.dating.post.grpc.proto.GetRecommendFeedResponse;
import com.dating.post.grpc.proto.ListCommentsRequest;
import com.dating.post.grpc.proto.ListCommentsResponse;
import com.dating.post.service.FeedService;
import com.dating.post.service.PostCommentService;
import com.dating.post.service.PostLikeService;
import com.dating.post.service.PostReadService;
import com.dating.post.service.PostWriteService;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostGrpcServiceTest {

    @Mock
    private PostWriteService postWriteService;

    @Mock
    private PostReadService postReadService;

    @Mock
    private PostLikeService postLikeService;

    @Mock
    private PostCommentService postCommentService;

    @Mock
    private FeedService feedService;

    private PostGrpcService postGrpcService;

    @BeforeEach
    void setUp() {
        postGrpcService = new PostGrpcService(
                postWriteService, postReadService, postLikeService, postCommentService, feedService);
    }

    @AfterEach
    void tearDown() {
        GrpcUserContext.clear();
    }

    @Test
    void createPost_withoutUserId_shouldReject() {
        PostBusinessException exception = assertThrows(PostBusinessException.class, () ->
                postGrpcService.createPost(CreatePostRequest.newBuilder().setContent("hi").build(), noopCreateObserver()));
        assertEquals(PostErrorCode.UNAUTHORIZED, exception.getErrorCode());
    }

    @Test
    void createPost_withUserId_shouldReturnPostId() {
        GrpcUserContext.setUserId(10001L);
        when(postWriteService.createPost(any(PostCreateCommand.class))).thenReturn(88001L);

        AtomicReference<CreatePostResponse> captured = new AtomicReference<>();
        postGrpcService.createPost(
                CreatePostRequest.newBuilder().setContent("hello").addImageKeys("post/1/a.jpg").build(),
                observer(captured));

        assertEquals(88001L, captured.get().getPostId());
    }

    @Test
    void actionLike_shouldDelegate() {
        GrpcUserContext.setUserId(10001L);
        when(postLikeService.actionLike(10001L, 10001L, true)).thenReturn(true);

        AtomicReference<ActionLikeResponse> captured = new AtomicReference<>();
        postGrpcService.actionLike(
                ActionLikeRequest.newBuilder().setPostId(10001L).setLike(true).build(),
                observer(captured));

        assertTrue(captured.get().getSuccess());
    }

    @Test
    void createComment_shouldDelegate() {
        GrpcUserContext.setUserId(10001L);
        when(postCommentService.createComment(10001L, 10001L, "nice")).thenReturn(99001L);

        AtomicReference<CreateCommentResponse> captured = new AtomicReference<>();
        postGrpcService.createComment(
                CreateCommentRequest.newBuilder().setPostId(10001L).setContent("nice").build(),
                observer(captured));

        assertEquals(99001L, captured.get().getCommentId());
    }

    @Test
    void listComments_shouldDelegate() {
        when(postCommentService.listComments(10001L, "", 0)).thenReturn(new com.dating.post.dto.ListCommentsResult());

        AtomicReference<ListCommentsResponse> captured = new AtomicReference<>();
        postGrpcService.listComments(
                ListCommentsRequest.newBuilder().setPostId(10001L).build(),
                observer(captured));

        assertEquals(0, captured.get().getItemsCount());
    }

    @Test
    void deleteComment_shouldDelegate() {
        GrpcUserContext.setUserId(10001L);

        AtomicReference<DeleteCommentResponse> captured = new AtomicReference<>();
        postGrpcService.deleteComment(
                DeleteCommentRequest.newBuilder().setCommentId(20001L).build(),
                observer(captured));

        assertTrue(captured.get().getSuccess());
    }

    @Test
    void deletePost_shouldDelegateToWriteService() {
        GrpcUserContext.setUserId(10001L);
        AtomicReference<DeletePostResponse> captured = new AtomicReference<>();
        postGrpcService.deletePost(
                DeletePostRequest.newBuilder().setPostId(88001L).build(),
                observer(captured));

        assertTrue(captured.get().getSuccess());
    }

    @Test
    void getRecommendFeed_shouldDelegateToFeedService() {
        GrpcUserContext.setUserId(10001L);
        FeedResult feedResult = new FeedResult();
        feedResult.setHasMore(false);
        when(feedService.getRecommendFeed(10001L, 10, "")).thenReturn(feedResult);

        AtomicReference<GetRecommendFeedResponse> captured = new AtomicReference<>();
        postGrpcService.getRecommendFeed(
                GetRecommendFeedRequest.newBuilder().setPageSize(10).build(),
                observer(captured));

        assertEquals(0, captured.get().getItemsCount());
        assertFalse(captured.get().getHasMore());
    }

    private <T> StreamObserver<T> observer(AtomicReference<T> captured) {
        return new StreamObserver<>() {
            @Override
            public void onNext(T value) {
                captured.set(value);
            }

            @Override
            public void onError(Throwable t) {
                if (t instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                throw new RuntimeException(t);
            }

            @Override
            public void onCompleted() {
            }
        };
    }

    private StreamObserver<CreatePostResponse> noopCreateObserver() {
        return new StreamObserver<>() {
            @Override
            public void onNext(CreatePostResponse value) {
            }

            @Override
            public void onError(Throwable t) {
                if (t instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                throw new RuntimeException(t);
            }

            @Override
            public void onCompleted() {
            }
        };
    }
}
