package com.dating.gateway.dto.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "OpenIM 用户 token")
public class ImTokenVO {

    private String userId;
    private String imToken;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getImToken() {
        return imToken;
    }

    public void setImToken(String imToken) {
        this.imToken = imToken;
    }
}
