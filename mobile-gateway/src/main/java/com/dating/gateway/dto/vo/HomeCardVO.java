package com.dating.gateway.dto.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "主页卡片视图")
public class HomeCardVO {

    private Long selfUserId;
    private UserProfileVO target;

    public Long getSelfUserId() {
        return selfUserId;
    }

    public void setSelfUserId(Long selfUserId) {
        this.selfUserId = selfUserId;
    }

    public UserProfileVO getTarget() {
        return target;
    }

    public void setTarget(UserProfileVO target) {
        this.target = target;
    }
}
