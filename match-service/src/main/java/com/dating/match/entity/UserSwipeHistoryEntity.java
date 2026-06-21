package com.dating.match.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.OffsetDateTime;

/**
 * 划卡历史实体，对应 match_center.user_swipe_history。
 */
@TableName("user_swipe_history")
public class UserSwipeHistoryEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 划卡记录业务主键，对外引用使用该字段。 */
    private Long bizId;

    private Long userId;
    private Long targetUserId;
    private Integer targetUserType;
    private Integer direction;
    private OffsetDateTime swipedAt;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }

    public Integer getTargetUserType() {
        return targetUserType;
    }

    public void setTargetUserType(Integer targetUserType) {
        this.targetUserType = targetUserType;
    }

    public Integer getDirection() {
        return direction;
    }

    public void setDirection(Integer direction) {
        this.direction = direction;
    }

    public OffsetDateTime getSwipedAt() {
        return swipedAt;
    }

    public void setSwipedAt(OffsetDateTime swipedAt) {
        this.swipedAt = swipedAt;
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
