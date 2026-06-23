package com.dating.gateway.service.impl;

import com.dating.gateway.client.PostGrpcClient;
import com.dating.gateway.converter.PostProtoAdapter;
import com.dating.gateway.converter.PostReqBuilder;
import com.dating.gateway.dto.CreateCommentReq;
import com.dating.gateway.dto.CreatePostReq;
import com.dating.gateway.dto.vo.CommentVO;
import com.dating.gateway.dto.vo.PostDetailVO;
import com.dating.gateway.dto.vo.PostVO;
import com.dating.gateway.service.PostBffService;
import com.dating.gateway.support.PostParamSupport;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Post BFF 实现：校验 → proto → gRPC（metadata 注入 x-user-id）→ VO。
 */
@Service
@Profile("!test")
public class PostBffServiceImpl implements PostBffService {

    private final PostGrpcClient postGrpcClient;

    public PostBffServiceImpl(PostGrpcClient postGrpcClient) {
        this.postGrpcClient = postGrpcClient;
    }

    @Override
    public long createPost(long callerUserId, CreatePostReq req) {
        PostParamSupport.validateCreatePost(req);
        var response = postGrpcClient.createPost(callerUserId, PostReqBuilder.buildCreatePost(req));
        return response.getPostId();
    }

    @Override
    public PostDetailVO getPostDetail(long callerUserId, long postId) {
        var response = postGrpcClient.getPostDetail(callerUserId, PostReqBuilder.buildGetPostDetail(postId));
        return PostProtoAdapter.toPostDetailVO(response);
    }

    @Override
    public boolean deletePost(long callerUserId, long postId) {
        return postGrpcClient.deletePost(callerUserId, PostReqBuilder.buildDeletePost(postId)).getSuccess();
    }

    @Override
    public List<PostVO> getRecommendFeed(long callerUserId, int pageSize, String cursor) {
        int safePageSize = PostParamSupport.clampFeedPageSize(pageSize);
        var response = postGrpcClient.getRecommendFeed(
                callerUserId, PostReqBuilder.buildGetRecommendFeed(cursor, safePageSize));
        return PostProtoAdapter.toPostVOList(response);
    }

    @Override
    public List<PostVO> listUserPosts(long callerUserId, long userId, int pageSize, String cursor) {
        int safePageSize = PostParamSupport.clampListPageSize(pageSize);
        var response = postGrpcClient.listUserPosts(
                callerUserId, PostReqBuilder.buildListUserPosts(userId, cursor, safePageSize));
        return PostProtoAdapter.toPostVOList(response);
    }

    @Override
    public boolean likePost(long callerUserId, long postId) {
        return postGrpcClient.actionLike(callerUserId, PostReqBuilder.buildActionLike(postId, true)).getSuccess();
    }

    @Override
    public boolean unlikePost(long callerUserId, long postId) {
        return postGrpcClient.actionLike(callerUserId, PostReqBuilder.buildActionLike(postId, false)).getSuccess();
    }

    @Override
    public List<CommentVO> listComments(long callerUserId, long postId, int pageSize, String cursor) {
        int safePageSize = PostParamSupport.clampListPageSize(pageSize);
        var response = postGrpcClient.listComments(
                callerUserId, PostReqBuilder.buildListComments(postId, cursor, safePageSize));
        return PostProtoAdapter.toCommentVOList(response);
    }

    @Override
    public long createComment(long callerUserId, long postId, CreateCommentReq req) {
        PostParamSupport.validateCreateComment(postId, req);
        var response = postGrpcClient.createComment(
                callerUserId, PostReqBuilder.buildCreateComment(postId, req.getContent().trim()));
        return response.getCommentId();
    }

    @Override
    public boolean deleteComment(long callerUserId, long commentId) {
        return postGrpcClient.deleteComment(callerUserId, PostReqBuilder.buildDeleteComment(commentId)).getSuccess();
    }
}
