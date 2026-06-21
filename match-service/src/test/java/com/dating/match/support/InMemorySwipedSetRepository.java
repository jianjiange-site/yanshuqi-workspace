package com.dating.match.support;

import com.dating.match.repository.SwipedSetRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * 单测用 swiped set 记录。
 */
public class InMemorySwipedSetRepository implements SwipedSetRepository {

    private final List<String> entries = new ArrayList<>();

    @Override
    public void addSwiped(long userId, long targetUserId) {
        entries.add(userId + ":" + targetUserId);
    }

    public boolean contains(long userId, long targetUserId) {
        return entries.contains(userId + ":" + targetUserId);
    }

    public int size() {
        return entries.size();
    }
}
