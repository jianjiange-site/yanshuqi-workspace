package com.dating.match.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.OffsetDateTime;

/**
 * 主页访问实体，对应 match_center.profile_visit。
 */
@TableName("profile_visit")
public class ProfileVisitEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 访问记录业务主键，对外 visitId 映射该字段。 */
    private Long bizId;

    private Long fromUserId;
    private Long targetUserId;
    private Integer visitCount;
    private OffsetDateTime firstVisitedAt;
    private OffsetDateTime lastVisitedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @TableLogic
    private Integer deleted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBizId() {
        return bizId;
    }

    public void setBizId(Long bizId) {
        this.bizId = bizId;
    }

    public Long getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(Long fromUserId) {
        this.fromUserId = fromUserId;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }

    public Integer getVisitCount() {
        return visitCount;
    }

    public void setVisitCount(Integer visitCount) {
        this.visitCount = visitCount;
    }

    public OffsetDateTime getFirstVisitedAt() {
        return firstVisitedAt;
    }

    public void setFirstVisitedAt(OffsetDateTime firstVisitedAt) {
        this.firstVisitedAt = firstVisitedAt;
    }

    public OffsetDateTime getLastVisitedAt() {
        return lastVisitedAt;
    }

    public void setLastVisitedAt(OffsetDateTime lastVisitedAt) {
        this.lastVisitedAt = lastVisitedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}
