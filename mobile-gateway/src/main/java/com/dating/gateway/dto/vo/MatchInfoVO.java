package com.dating.gateway.dto.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "单条匹配信息")
public class MatchInfoVO {

    @Schema(description = "匹配业务 ID（matches.biz_id）")
    private Long matchId;
    private Long partnerUserId;
    private String partnerNickname;
    private List<String> partnerPhotoKeys = new ArrayList<>();
    private Long matchedAtMs;
    private String source;

    public Long getMatchId() {
        return matchId;
    }

    public void setMatchId(Long matchId) {
        this.matchId = matchId;
    }

    public Long getPartnerUserId() {
        return partnerUserId;
    }

    public void setPartnerUserId(Long partnerUserId) {
        this.partnerUserId = partnerUserId;
    }

    public String getPartnerNickname() {
        return partnerNickname;
    }

    public void setPartnerNickname(String partnerNickname) {
        this.partnerNickname = partnerNickname;
    }

    public List<String> getPartnerPhotoKeys() {
        return partnerPhotoKeys;
    }

    public void setPartnerPhotoKeys(List<String> partnerPhotoKeys) {
        this.partnerPhotoKeys = partnerPhotoKeys;
    }

    public Long getMatchedAtMs() {
        return matchedAtMs;
    }

    public void setMatchedAtMs(Long matchedAtMs) {
        this.matchedAtMs = matchedAtMs;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
