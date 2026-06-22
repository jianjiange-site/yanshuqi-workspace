package com.dating.match.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 单条匹配查询结果，matchId 为 match.biz_id。
 */
public class MatchInfoDto {

    private long matchId;
    private long partnerUserId;
    private String partnerNickname;
    private List<String> partnerPhotoKeys = new ArrayList<>();
    private long matchedAtMs;
    private String source;

    public long getMatchId() {
        return matchId;
    }

    public void setMatchId(long matchId) {
        this.matchId = matchId;
    }

    public long getPartnerUserId() {
        return partnerUserId;
    }

    public void setPartnerUserId(long partnerUserId) {
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
        this.partnerPhotoKeys = partnerPhotoKeys != null ? partnerPhotoKeys : new ArrayList<>();
    }

    public long getMatchedAtMs() {
        return matchedAtMs;
    }

    public void setMatchedAtMs(long matchedAtMs) {
        this.matchedAtMs = matchedAtMs;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
