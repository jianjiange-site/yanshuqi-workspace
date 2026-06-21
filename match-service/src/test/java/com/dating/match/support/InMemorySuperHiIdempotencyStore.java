package com.dating.match.support;

import com.dating.match.dto.SuperHiResult;
import com.dating.match.service.SuperHiIdempotencyStore;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemorySuperHiIdempotencyStore implements SuperHiIdempotencyStore {

    private final Map<String, SuperHiResult> store = new ConcurrentHashMap<>();

    @Override
    public Optional<SuperHiResult> find(long userId, String clientRequestId) {
        return Optional.ofNullable(store.get(userId + ":" + clientRequestId));
    }

    @Override
    public void save(long userId, String clientRequestId, SuperHiResult result) {
        store.put(userId + ":" + clientRequestId, result);
    }
}
