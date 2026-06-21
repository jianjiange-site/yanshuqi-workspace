package com.dating.match.service;

import com.dating.match.dto.SuperHiResult;

import java.util.Optional;

/**
 * SuperHi clientRequestId 幂等存储抽象。
 */
public interface SuperHiIdempotencyStore {

    Optional<SuperHiResult> find(long userId, String clientRequestId);

    void save(long userId, String clientRequestId, SuperHiResult result);
}
