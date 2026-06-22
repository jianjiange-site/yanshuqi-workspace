package com.dating.post.service;

import com.dating.post.repository.ReadHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Feed 已读去重服务。
 * <p>
 * 当前使用 Redis Set 记录已读 postId，实现简单、准确；数据量大时可替换为 Redisson BloomFilter 节省内存。
 */
@Service
public class ReadHistoryService {

    private final ReadHistoryRepository readHistoryRepository;

    public ReadHistoryService(ReadHistoryRepository readHistoryRepository) {
        this.readHistoryRepository = readHistoryRepository;
    }

    public List<Long> filterUnread(long userId, List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return List.of();
        }
        Set<Long> readSet = readHistoryRepository.listReadPostIds(userId);
        List<Long> unread = new ArrayList<>();
        for (Long postId : postIds) {
            if (postId != null && !readSet.contains(postId)) {
                unread.add(postId);
            }
        }
        return unread;
    }

    public void markRead(long userId, Collection<Long> postIds) {
        readHistoryRepository.markRead(userId, postIds);
    }
}
