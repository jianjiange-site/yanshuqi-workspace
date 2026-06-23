package com.dating.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "发布帖子请求")
public class CreatePostReq {

    @Schema(description = "正文内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Schema(description = "图片 object key 列表，可为空")
    private List<String> imageKeys;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getImageKeys() {
        return imageKeys;
    }

    public void setImageKeys(List<String> imageKeys) {
        this.imageKeys = imageKeys;
    }
}
