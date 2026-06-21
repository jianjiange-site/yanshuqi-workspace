package com.dating.gateway.dto.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "划卡结果")
public class SwipeResultVO {

    @Schema(description = "匹配业务 ID（matches.biz_id），未匹配时为 0 或 null")
    private Long matchId;

    public Long getMatchId() {
        return matchId;
    }

    public void setMatchId(Long matchId) {
        this.matchId = matchId;
    }
}
