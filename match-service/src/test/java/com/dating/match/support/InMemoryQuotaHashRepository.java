package com.dating.match.support;

import com.dating.match.repository.QuotaHashRepository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 单测用内存配额 Hash。
 */
public class InMemoryQuotaHashRepository implements QuotaHashRepository {

    private final Map<String, Map<String, Long>> store = new ConcurrentHashMap<>();

    @Override
    public long increment(String quotaKey, String field, long delta) {
        Map<String, Long> hash = store.computeIfAbsent(quotaKey, key -> new ConcurrentHashMap<>());
        long next = hash.getOrDefault(field, 0L) + delta;
        hash.put(field, next);
        return next;
    }

    @Override
    public long get(String quotaKey, String field) {
        return store.getOrDefault(quotaKey, Map.of()).getOrDefault(field, 0L);
    }

    @Override
    public void ensureTtl(String quotaKey, long ttlSeconds) {
        // no-op for memory
    }
}
