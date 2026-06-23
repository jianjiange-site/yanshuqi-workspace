package com.dating.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "获取 LiveKit call token 请求")
public class CallTokenReq {

    @Schema(description = "对端用户 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long peerId;

    public Long getPeerId() {
        return peerId;
    }

    public void setPeerId(Long peerId) {
        this.peerId = peerId;
    }
}
