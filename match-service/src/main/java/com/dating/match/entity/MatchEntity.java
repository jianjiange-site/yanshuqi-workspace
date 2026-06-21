package com.dating.match.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.OffsetDateTime;

/**
 * 匹配关系实体，对应 match_center.match（PostgreSQL 保留字表名）。
 */
@TableName(value = "\"match\"")
public class MatchEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 匹配业务主键，对外 matchId 映射该字段。 */
    private Long bizId;

    private Long userIdLow;
    private Long userIdHigh;
    private OffsetDateTime matchedAt;
    private String source;
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

    public Long getUserIdLow() {
        return userIdLow;
    }

    public void setUserIdLow(Long userIdLow) {
        this.userIdLow = userIdLow;
    }

    public Long getUserIdHigh() {
        return userIdHigh;
    }

    public void setUserIdHigh(Long userIdHigh) {
        this.userIdHigh = userIdHigh;
    }

    public OffsetDateTime getMatchedAt() {
        return matchedAt;
    }

    public void setMatchedAt(OffsetDateTime matchedAt) {
        this.matchedAt = matchedAt;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
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
