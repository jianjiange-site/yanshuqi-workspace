package com.dating.gateway.client;

import com.dating.gateway.converter.PostReqBuilder;
import com.dating.gateway.dto.CreatePostReq;
import com.dating.gateway.exception.GatewayBizException;
import com.dating.gateway.exception.GatewayErrorCode;
import com.dating.post.grpc.proto.ActionLikeRequest;
import com.dating.post.grpc.proto.CreatePostRequest;
import com.dating.post.grpc.proto.CreatePostResponse;
import com.dating.post.grpc.proto.PostServiceGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PostGrpcClientTest {

    @Mock
    private PostServiceGrpc.PostServiceBlockingStub postServiceStub;

    private PostGrpcClient postGrpcClient;

    @BeforeEach
    void setUp() {
        postGrpcClient = new PostGrpcClient();
        ReflectionTestUtils.setField(postGrpcClient, "postServiceStub", postServiceStub);
        ReflectionTestUtils.setField(postGrpcClient, "deadlineSeconds", 5L);
        when(postServiceStub.withDeadlineAfter(anyLong(), any(TimeUnit.class))).thenReturn(postServiceStub);
        when(postServiceStub.withInterceptors(any())).thenReturn(postServiceStub);
    }

    @Test
    void createPost_shouldBuildCorrectRequest() {
        when(postServiceStub.createPost(any())).thenReturn(
                CreatePostResponse.newBuilder().setPostId(88001L).build());

        CreatePostReq req = new CreatePostReq();
        req.setContent("hello");
        CreatePostRequest protoReq = PostReqBuilder.buildCreatePost(req);
        postGrpcClient.createPost(77001L, protoReq);

        ArgumentCaptor<CreatePostRequest> captor = ArgumentCaptor.forClass(CreatePostRequest.class);
        verify(postServiceStub).createPost(captor.capture());
        assertEquals("hello", captor.getValue().getContent());
    }

    @Test
    void stubWithMetadata_shouldBuildUserIdMetadata() {
        var stub = postGrpcClient.stubWithMetadata(77001L);
        org.junit.jupiter.api.Assertions.assertNotNull(stub);
        // x-user-id 注入细节见 PostGrpcMetadataSupportTest
    }

    @Test
    void actionLike_unlikeShouldSetLikeFalse() {
        when(postServiceStub.actionLike(any())).thenReturn(
                com.dating.post.grpc.proto.ActionLikeResponse.newBuilder().setSuccess(true).build());

        postGrpcClient.actionLike(77001L, PostReqBuilder.buildActionLike(88001L, false));

        ArgumentCaptor<ActionLikeRequest> captor = ArgumentCaptor.forClass(ActionLikeRequest.class);
        verify(postServiceStub).actionLike(captor.capture());
        assertEquals(88001L, captor.getValue().getPostId());
        assertFalse(captor.getValue().getLike());
    }

    @Test
    void unavailable_shouldMapToUpstreamUnavailable() {
        when(postServiceStub.getPostDetail(any())).thenThrow(Status.UNAVAILABLE.asRuntimeException());

        GatewayBizException ex = assertThrows(GatewayBizException.class,
                () -> postGrpcClient.getPostDetail(1L, PostReqBuilder.buildGetPostDetail(1L)));
        assertEquals(GatewayErrorCode.UPSTREAM_UNAVAILABLE, ex.getErrorCode());
    }

    @Test
    void postNotFound_shouldMapToPostNotFoundCode() {
        StatusRuntimeException grpcEx = Status.NOT_FOUND.withDescription("POST_NOT_FOUND").asRuntimeException();
        GatewayBizException ex = postGrpcClient.mapPostException(grpcEx);
        assertEquals(GatewayErrorCode.POST_NOT_FOUND, ex.getErrorCode());
    }
}
