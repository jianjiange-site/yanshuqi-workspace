package com.dating.match.dto;

import com.dating.match.entity.MatchEntity;

/**
 * Match 幂等插入结果。
 */
public class MatchInsertResult {

    private final MatchEntity entity;
    private final boolean newlyCreated;

    public MatchInsertResult(MatchEntity entity, boolean newlyCreated) {
        this.entity = entity;
        this.newlyCreated = newlyCreated;
    }

    public MatchEntity getEntity() {
        return entity;
    }

    public boolean isNewlyCreated() {
        return newlyCreated;
    }
}
