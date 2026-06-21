package com.dating.gateway.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * SuperHi 请求体；callerUserId 由 gateway 鉴权上下文注入。
 */
@Schema(description = "SuperHi 请求")
public class SuperHiReq {

    @Schema(description = "目标用户业务 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long targetUserId;

    @Schema(description = "客户端幂等请求 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String clientRequestId;

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getClientRequestId() {
        return clientRequestId;
    }

    public void setClientRequestId(String clientRequestId) {
        this.clientRequestId = clientRequestId;
    }
}
