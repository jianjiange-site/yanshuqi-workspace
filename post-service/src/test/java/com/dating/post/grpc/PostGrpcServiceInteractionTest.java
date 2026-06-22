package com.dating.post.grpc;

import com.dating.post.dto.CommentInfoDTO;
import com.dating.post.dto.ListCommentsResult;
import com.dating.post.service.FeedService;
import com.dating.post.service.PostCommentService;
import com.dating.post.service.PostLikeService;
import com.dating.post.service.PostReadService;
import com.dating.post.service.PostWriteService;
import com.dating.post.grpc.proto.ActionLikeRequest;
import com.dating.post.grpc.proto.ActionLikeResponse;
import com.dating.post.grpc.proto.CreateCommentRequest;
import com.dating.post.grpc.proto.CreateCommentResponse;
import com.dating.post.grpc.proto.DeleteCommentRequest;
import com.dating.post.grpc.proto.DeleteCommentResponse;
import com.dating.post.grpc.proto.ListCommentsRequest;
import com.dating.post.grpc.proto.ListCommentsResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostGrpcServiceInteractionTest {

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
    void actionLike_shouldDelegate() {
        GrpcUserContext.setUserId(10001L);
        when(postLikeService.actionLike(10001L, 88001L, true)).thenReturn(true);

        AtomicReference<ActionLikeResponse> captured = new AtomicReference<>();
        postGrpcService.actionLike(
                ActionLikeRequest.newBuilder().setPostId(88001L).setLike(true).build(),
                observer(captured));

        assertTrue(captured.get().getSuccess());
    }

    @Test
    void createComment_shouldReturnCommentId() {
        GrpcUserContext.setUserId(10001L);
        when(postCommentService.createComment(10001L, 88001L, "nice")).thenReturn(99001L);

        AtomicReference<CreateCommentResponse> captured = new AtomicReference<>();
        postGrpcService.createComment(
                CreateCommentRequest.newBuilder().setPostId(88001L).setContent("nice").build(),
                observer(captured));

        assertEquals(99001L, captured.get().getCommentId());
    }

    @Test
    void listComments_shouldMapItems() {
        CommentInfoDTO dto = new CommentInfoDTO();
        dto.setCommentId(99001L);
        dto.setPostId(88001L);
        dto.setUserId(10001L);
        dto.setContent("hello");
        ListCommentsResult result = new ListCommentsResult();
        result.setItems(List.of(dto));
        when(postCommentService.listComments(88001L, "0", 20)).thenReturn(result);

        AtomicReference<ListCommentsResponse> captured = new AtomicReference<>();
        postGrpcService.listComments(
                ListCommentsRequest.newBuilder().setPostId(88001L).setCursor("0").setPageSize(20).build(),
                observer(captured));

        assertEquals(1, captured.get().getItemsCount());
    }

    @Test
    void deleteComment_shouldDelegate() {
        GrpcUserContext.setUserId(10001L);
        doNothing().when(postCommentService).deleteComment(10001L, 99001L);

        AtomicReference<DeleteCommentResponse> captured = new AtomicReference<>();
        postGrpcService.deleteComment(
                DeleteCommentRequest.newBuilder().setCommentId(99001L).build(),
                observer(captured));

        assertTrue(captured.get().getSuccess());
        verify(postCommentService).deleteComment(10001L, 99001L);
    }

    private <T> io.grpc.stub.StreamObserver<T> observer(AtomicReference<T> captured) {
        return new io.grpc.stub.StreamObserver<>() {
            @Override
            public void onNext(T value) {
                captured.set(value);
            }

            @Override
            public void onError(Throwable t) {
                throw new RuntimeException(t);
            }

            @Override
            public void onCompleted() {
            }
        };
    }
}
