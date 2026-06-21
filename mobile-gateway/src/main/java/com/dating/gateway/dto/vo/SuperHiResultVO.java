package com.dating.gateway.dto.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "SuperHi 结果")
public class SuperHiResultVO {

    @Schema(description = "匹配业务 ID（matches.biz_id）")
    private Long matchId;

    @Schema(description = "本次消耗金币数")
    private Integer coinsUsed;

    public Long getMatchId() {
        return matchId;
    }

    public void setMatchId(Long matchId) {
        this.matchId = matchId;
    }

    public Integer getCoinsUsed() {
        return coinsUsed;
    }

    public void setCoinsUsed(Integer coinsUsed) {
        this.coinsUsed = coinsUsed;
    }
}
