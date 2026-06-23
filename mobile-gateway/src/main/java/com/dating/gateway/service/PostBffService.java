package com.dating.gateway.service;

import com.dating.gateway.dto.CreateCommentReq;
import com.dating.gateway.dto.CreatePostReq;
import com.dating.gateway.dto.vo.CommentVO;
import com.dating.gateway.dto.vo.PostDetailVO;
import com.dating.gateway.dto.vo.PostVO;

import java.util.List;

/**
 * Post BFF：REST 编排与 VO 转换，不含发帖/点赞/评论业务逻辑。
 */
public interface PostBffService {

    long createPost(long callerUserId, CreatePostReq req);

    PostDetailVO getPostDetail(long callerUserId, long postId);

    boolean deletePost(long callerUserId, long postId);

    List<PostVO> getRecommendFeed(long callerUserId, int pageSize, String cursor);

    List<PostVO> listUserPosts(long callerUserId, long userId, int pageSize, String cursor);

    boolean likePost(long callerUserId, long postId);

    boolean unlikePost(long callerUserId, long postId);

    List<CommentVO> listComments(long callerUserId, long postId, int pageSize, String cursor);

    long createComment(long callerUserId, long postId, CreateCommentReq req);

    boolean deleteComment(long callerUserId, long commentId);
}
