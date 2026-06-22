package com.dating.post.grpc;

import com.dating.post.dto.ListUserPostsResult;
import com.dating.post.dto.PostCreateCommand;
import com.dating.post.dto.PostInfoDTO;
import com.dating.post.service.FeedService;
import com.dating.post.service.PostCommentService;
import com.dating.post.service.PostLikeService;
import com.dating.post.service.PostReadService;
import com.dating.post.service.PostWriteService;
import com.dating.post.grpc.proto.CreatePostRequest;
import com.dating.post.grpc.proto.CreatePostResponse;
import com.dating.post.grpc.proto.DeletePostRequest;
import com.dating.post.grpc.proto.DeletePostResponse;
import com.dating.post.grpc.proto.GetPostDetailRequest;
import com.dating.post.grpc.proto.GetPostDetailResponse;
import com.dating.post.grpc.proto.ListUserPostsRequest;
import com.dating.post.grpc.proto.ListUserPostsResponse;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostGrpcServicePostTest {

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
    void createPost_shouldDelegateToWriteService() {
        GrpcUserContext.setUserId(10001L);
        when(postWriteService.createPost(any(PostCreateCommand.class))).thenReturn(88001L);

        AtomicReference<CreatePostResponse> captured = new AtomicReference<>();
        postGrpcService.createPost(
                CreatePostRequest.newBuilder().setContent("hello").addImageKeys("post/a.jpg").build(),
                observer(captured));

        assertEquals(88001L, captured.get().getPostId());
    }

    @Test
    void getPostDetail_shouldReturnRealDto() {
        GrpcUserContext.setUserId(10001L);
        PostInfoDTO dto = new PostInfoDTO();
        dto.setPostId(88001L);
        dto.setUserId(10001L);
        dto.setContent("detail");
        dto.setLikeCount(1);
        dto.setCommentCount(2);
        dto.setLiked(true);
        dto.setCreatedAtSeconds(1_700_000_000L);
        when(postReadService.getPostDetail(88001L, 10001L)).thenReturn(dto);

        AtomicReference<GetPostDetailResponse> captured = new AtomicReference<>();
        postGrpcService.getPostDetail(
                GetPostDetailRequest.newBuilder().setPostId(88001L).build(),
                observer(captured));

        assertEquals("detail", captured.get().getPost().getContent());
        assertTrue(captured.get().getPost().getIsLiked());
    }

    @Test
    void listUserPosts_shouldMapItems() {
        PostInfoDTO dto = new PostInfoDTO();
        dto.setPostId(88001L);
        dto.setUserId(20002L);
        dto.setContent("list item");
        ListUserPostsResult listResult = new ListUserPostsResult();
        listResult.setItems(List.of(dto));
        listResult.setHasMore(false);
        listResult.setNextCursor("");
        when(postReadService.listUserPosts(20002L, "0", 20, null)).thenReturn(listResult);

        AtomicReference<ListUserPostsResponse> captured = new AtomicReference<>();
        postGrpcService.listUserPosts(
                ListUserPostsRequest.newBuilder().setUserId(20002L).setCursor("0").setPageSize(20).build(),
                observer(captured));

        assertEquals(1, captured.get().getItemsCount());
    }

    @Test
    void deletePost_shouldDelegateToWriteService() {
        GrpcUserContext.setUserId(10001L);
        doNothing().when(postWriteService).deletePost(10001L, 88001L);

        AtomicReference<DeletePostResponse> captured = new AtomicReference<>();
        postGrpcService.deletePost(
                DeletePostRequest.newBuilder().setPostId(88001L).build(),
                observer(captured));

        assertTrue(captured.get().getSuccess());
        verify(postWriteService).deletePost(eq(10001L), eq(88001L));
    }

    private <T> io.grpc.stub.StreamObserver<T> observer(AtomicReference<T> captured) {
        return new io.grpc.stub.StreamObserver<>() {
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
}
