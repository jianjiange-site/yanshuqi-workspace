package com.dating.match.dto;

/**
 * 单条访问记录查询结果，visitId 为 profile_visit.biz_id。
 */
public class VisitInfoDto {

    private long visitId;
    private long fromUserId;
    private int visitCount;
    private long firstVisitedAtMs;
    private long lastVisitedAtMs;

    public long getVisitId() {
        return visitId;
    }

    public void setVisitId(long visitId) {
        this.visitId = visitId;
    }

    public long getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(long fromUserId) {
        this.fromUserId = fromUserId;
    }

    public int getVisitCount() {
        return visitCount;
    }

    public void setVisitCount(int visitCount) {
        this.visitCount = visitCount;
    }

    public long getFirstVisitedAtMs() {
        return firstVisitedAtMs;
    }

    public void setFirstVisitedAtMs(long firstVisitedAtMs) {
        this.firstVisitedAtMs = firstVisitedAtMs;
    }

    public long getLastVisitedAtMs() {
        return lastVisitedAtMs;
    }

    public void setLastVisitedAtMs(long lastVisitedAtMs) {
        this.lastVisitedAtMs = lastVisitedAtMs;
    }
}
