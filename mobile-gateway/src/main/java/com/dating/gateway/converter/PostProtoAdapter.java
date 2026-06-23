package com.dating.gateway.converter;

import com.dating.gateway.dto.vo.CommentVO;
import com.dating.gateway.dto.vo.PostDetailVO;
import com.dating.gateway.dto.vo.PostVO;
import com.dating.post.grpc.proto.CommentInfo;
import com.dating.post.grpc.proto.GetPostDetailResponse;
import com.dating.post.grpc.proto.GetRecommendFeedResponse;
import com.dating.post.grpc.proto.ListCommentsResponse;
import com.dating.post.grpc.proto.ListUserPostsResponse;
import com.dating.post.grpc.proto.PostInfo;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * post proto 响应 → Swagger VO 转换器，不含业务逻辑。
 */
public final class PostProtoAdapter {

    private PostProtoAdapter() {
    }

    public static PostDetailVO toPostDetailVO(GetPostDetailResponse resp) {
        if (resp == null || !resp.hasPost()) {
            return null;
        }
        return toPostDetailVO(resp.getPost());
    }

    public static PostDetailVO toPostDetailVO(PostInfo post) {
        PostDetailVO vo = new PostDetailVO();
        fillPostFields(vo, post);
        return vo;
    }

    public static PostVO toPostVO(PostInfo post) {
        if (post == null || post.getPostId() <= 0) {
            return null;
        }
        PostVO vo = new PostVO();
        fillPostFields(vo, post);
        return vo;
    }

    public static List<PostVO> toPostVOList(GetRecommendFeedResponse resp) {
        if (resp == null) {
            return Collections.emptyList();
        }
        return toPostVOList(resp.getItemsList());
    }

    public static List<PostVO> toPostVOList(ListUserPostsResponse resp) {
        if (resp == null) {
            return Collections.emptyList();
        }
        return toPostVOList(resp.getItemsList());
    }

    public static List<PostVO> toPostVOList(List<PostInfo> items) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }
        List<PostVO> result = new ArrayList<>(items.size());
        for (PostInfo item : items) {
            PostVO vo = toPostVO(item);
            if (vo != null) {
                result.add(vo);
            }
        }
        return result;
    }

    public static CommentVO toCommentVO(CommentInfo comment) {
        if (comment == null || comment.getCommentId() <= 0) {
            return null;
        }
        CommentVO vo = new CommentVO();
        vo.setCommentId(comment.getCommentId());
        vo.setPostId(comment.getPostId());
        vo.setUserId(comment.getUserId());
        vo.setContent(emptyToNull(comment.getContent()));
        vo.setCreatedAtSeconds(comment.getCreatedAtSeconds() > 0 ? comment.getCreatedAtSeconds() : null);
        return vo;
    }

    public static List<CommentVO> toCommentVOList(ListCommentsResponse resp) {
        if (resp == null || resp.getItemsList().isEmpty()) {
            return new ArrayList<>();
        }
        List<CommentVO> result = new ArrayList<>();
        for (CommentInfo item : resp.getItemsList()) {
            CommentVO vo = toCommentVO(item);
            if (vo != null) {
                result.add(vo);
            }
        }
        return result;
    }

    private static void fillPostFields(PostVO vo, PostInfo post) {
        vo.setPostId(post.getPostId());
        vo.setUserId(post.getUserId());
        vo.setContent(emptyToNull(post.getContent()));
        vo.setImageKeys(new ArrayList<>(post.getImageKeysList()));
        vo.setLikeCount(post.getLikeCount());
        vo.setCommentCount(post.getCommentCount());
        vo.setIsLiked(post.getIsLiked());
        vo.setCreatedAtSeconds(post.getCreatedAtSeconds() > 0 ? post.getCreatedAtSeconds() : null);
    }

    private static String emptyToNull(String value) {
        return StringUtils.hasText(value) ? value : null;
    }
}
