package com.dating.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "创建评论请求")
public class CreateCommentReq {

    @Schema(description = "帖子 ID（可选，须与 path postId 一致）")
    private Long postId;

    @Schema(description = "评论内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
