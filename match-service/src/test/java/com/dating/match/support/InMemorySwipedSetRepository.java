package com.dating.match.support;

import com.dating.match.repository.SwipedSetRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 单测用 swiped set 记录。
 */
public class InMemorySwipedSetRepository implements SwipedSetRepository {

    private final List<String> entries = new ArrayList<>();

    @Override
    public void addSwiped(long userId, long targetUserId) {
        entries.add(userId + ":" + targetUserId);
    }

    @Override
    public Set<Long> findSwipedTargets(long userId, Collection<Long> targetUserIds) {
        Set<Long> swiped = new HashSet<>();
        if (targetUserIds == null) {
            return swiped;
        }
        for (Long targetUserId : targetUserIds) {
            if (targetUserId != null && contains(userId, targetUserId)) {
                swiped.add(targetUserId);
            }
        }
        return swiped;
    }

    public boolean contains(long userId, long targetUserId) {
        return entries.contains(userId + ":" + targetUserId);
    }

    public int size() {
        return entries.size();
    }
}
