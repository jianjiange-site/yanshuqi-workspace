package com.dating.gateway.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 划卡请求体；callerUserId 由 gateway 鉴权上下文注入，App 不传 userId。
 */
@Schema(description = "划卡请求")
public class SwipeReq {

    @Schema(description = "目标用户业务 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long targetUserId;

    @Schema(description = "划卡方向：LEFT / RIGHT", requiredMode = Schema.RequiredMode.REQUIRED)
    private String direction;

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
}
