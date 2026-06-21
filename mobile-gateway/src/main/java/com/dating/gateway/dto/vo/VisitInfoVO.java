package com.dating.gateway.dto.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "单条访问记录")
public class VisitInfoVO {

    @Schema(description = "访问记录业务 ID（profile_visit.biz_id）")
    private Long visitId;
    private Long fromUserId;
    private Integer visitCount;
    private Long firstVisitedAtMs;
    private Long lastVisitedAtMs;

    public Long getVisitId() {
        return visitId;
    }

    public void setVisitId(Long visitId) {
        this.visitId = visitId;
    }

    public Long getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(Long fromUserId) {
        this.fromUserId = fromUserId;
    }

    public Integer getVisitCount() {
        return visitCount;
    }

    public void setVisitCount(Integer visitCount) {
        this.visitCount = visitCount;
    }

    public Long getFirstVisitedAtMs() {
        return firstVisitedAtMs;
    }

    public void setFirstVisitedAtMs(Long firstVisitedAtMs) {
        this.firstVisitedAtMs = firstVisitedAtMs;
    }

    public Long getLastVisitedAtMs() {
        return lastVisitedAtMs;
    }

    public void setLastVisitedAtMs(Long lastVisitedAtMs) {
        this.lastVisitedAtMs = lastVisitedAtMs;
    }
}
