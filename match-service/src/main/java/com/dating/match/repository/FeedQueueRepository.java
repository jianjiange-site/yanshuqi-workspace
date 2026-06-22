package com.dating.match.repository;

import com.dating.match.recommend.FeedQueueItem;

import java.time.Duration;
import java.util.List;

/**
 * Feed Redis LIST 抽象。
 */
public interface FeedQueueRepository {

    void pushAll(long userId, List<FeedQueueItem> items, Duration ttl);

    /**
     * D1 覆盖写入：DEL + RPUSH；D0 冷启动使用 {@link #pushAll} 追加/首次写入。
     */
    void replaceAll(long userId, List<FeedQueueItem> items, Duration ttl);

    List<FeedQueueItem> leftPop(long userId, int count);

    long size(long userId);
}
