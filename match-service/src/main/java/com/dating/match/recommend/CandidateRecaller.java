package com.dating.match.recommend;

import com.dating.match.client.CandidateClient;
import com.dating.match.config.MatchProperties;
import com.dating.match.constant.UserTypeConstant;
import com.dating.match.manager.SwipeHistoryManager;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * D1 候选召回：mock 池 + 排除已 swipe target 与 caller 自身。
 */
@Component
@Profile("!test")
public class CandidateRecaller {

    private static final int SWIPED_LOOKUP_LIMIT = 2000;

    private final CandidateClient candidateClient;
    private final SwipeHistoryManager swipeHistoryManager;
    private final MatchProperties matchProperties;

    public CandidateRecaller(CandidateClient candidateClient,
                               SwipeHistoryManager swipeHistoryManager,
                               MatchProperties matchProperties) {
        this.candidateClient = candidateClient;
        this.swipeHistoryManager = swipeHistoryManager;
        this.matchProperties = matchProperties;
    }

    public D1CandidatePools recall(long userId, PreferenceProfile preference) {
        int poolSize = matchProperties.getFeed().getQueueSize();
        Set<Long> excluded = new HashSet<>(swipeHistoryManager.listAllSwipedTargetIds(userId, SWIPED_LOOKUP_LIMIT));
        excluded.add(userId);

        List<CandidateProfile> dhCandidates = candidateClient.listDhCandidates(userId, poolSize).stream()
                .filter(p -> !excluded.contains(p.getUserId()))
                .collect(Collectors.toList());
        List<CandidateProfile> bhCandidates = candidateClient.listBhCandidates(userId, poolSize).stream()
                .filter(p -> !excluded.contains(p.getUserId()))
                .collect(Collectors.toList());
        return new D1CandidatePools(dhCandidates, bhCandidates);
    }
}
