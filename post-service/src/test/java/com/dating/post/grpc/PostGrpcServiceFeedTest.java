package com.dating.post.grpc;

import com.dating.post.dto.FeedResult;
import com.dating.post.dto.PostInfoDTO;
import com.dating.post.service.FeedService;
import com.dating.post.service.PostCommentService;
import com.dating.post.service.PostLikeService;
import com.dating.post.service.PostReadService;
import com.dating.post.service.PostWriteService;
import com.dating.post.grpc.proto.GetRecommendFeedRequest;
import com.dating.post.grpc.proto.GetRecommendFeedResponse;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostGrpcServiceFeedTest {

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
    void getRecommendFeed_shouldDelegateToFeedService() {
        GrpcUserContext.setUserId(10001L);
        PostInfoDTO dto = new PostInfoDTO();
        dto.setPostId(88001L);
        dto.setUserId(20002L);
        dto.setContent("feed");
        FeedResult feedResult = new FeedResult();
        feedResult.setItems(List.of(dto));
        feedResult.setNextCursor("10:0");
        feedResult.setHasMore(true);
        when(feedService.getRecommendFeed(10001L, 10, "0")).thenReturn(feedResult);

        AtomicReference<GetRecommendFeedResponse> captured = new AtomicReference<>();
        postGrpcService.getRecommendFeed(
                GetRecommendFeedRequest.newBuilder().setPageSize(10).setCursor("0").build(),
                observer(captured));

        assertEquals(1, captured.get().getItemsCount());
        assertEquals("feed", captured.get().getItems(0).getContent());
        assertTrue(captured.get().getHasMore());
        assertEquals("10:0", captured.get().getNextCursor());
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
