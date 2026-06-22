package com.dating.match.support;

import com.dating.match.recommend.FeedQueueItem;
import com.dating.match.repository.FeedQueueRepository;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 单测用 Feed LIST。
 */
public class InMemoryFeedQueueRepository implements FeedQueueRepository {

    private final ConcurrentHashMap<Long, Deque<FeedQueueItem>> store = new ConcurrentHashMap<>();
    private Duration lastTtl;
    private boolean lastReplace;

    @Override
    public void pushAll(long userId, List<FeedQueueItem> items, Duration ttl) {
        if (items == null || items.isEmpty()) {
            return;
        }
        Deque<FeedQueueItem> queue = store.computeIfAbsent(userId, key -> new ArrayDeque<>());
        queue.addAll(items);
        lastTtl = ttl;
        lastReplace = false;
    }

    @Override
    public void replaceAll(long userId, List<FeedQueueItem> items, Duration ttl) {
        Deque<FeedQueueItem> queue = new ArrayDeque<>();
        if (items != null && !items.isEmpty()) {
            queue.addAll(items);
        }
        store.put(userId, queue);
        lastTtl = ttl;
        lastReplace = true;
    }

    @Override
    public List<FeedQueueItem> leftPop(long userId, int count) {
        Deque<FeedQueueItem> queue = store.get(userId);
        if (queue == null || count <= 0) {
            return List.of();
        }
        List<FeedQueueItem> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            FeedQueueItem item = queue.pollFirst();
            if (item == null) {
                break;
            }
            result.add(item);
        }
        return result;
    }

    @Override
    public long size(long userId) {
        Deque<FeedQueueItem> queue = store.get(userId);
        return queue == null ? 0L : queue.size();
    }

    public Duration getLastTtl() {
        return lastTtl;
    }

    public boolean wasLastReplace() {
        return lastReplace;
    }
}
