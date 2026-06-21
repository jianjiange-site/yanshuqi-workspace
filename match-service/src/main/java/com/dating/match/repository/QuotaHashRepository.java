package com.dating.match.repository;

/**
 * 配额 Redis Hash 读写抽象，便于单测注入内存实现。
 */
public interface QuotaHashRepository {

    long increment(String quotaKey, String field, long delta);

    long get(String quotaKey, String field);

    void ensureTtl(String quotaKey, long ttlSeconds);
}
