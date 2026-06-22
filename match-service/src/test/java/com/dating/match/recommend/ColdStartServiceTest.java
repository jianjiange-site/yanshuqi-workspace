package com.dating.match.recommend;

import com.dating.match.client.MockCandidateClient;
import com.dating.match.config.MatchProperties;
import com.dating.match.support.InMemoryFeedQueueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ColdStartServiceTest {

  private static final long CALLER = 10001L;

  private InMemoryFeedQueueRepository feedQueueRepository;
  private MockCandidateClient candidateClient;
  private ColdStartService coldStartService;

  @BeforeEach
  void setUp() {
    feedQueueRepository = new InMemoryFeedQueueRepository();
    candidateClient = new MockCandidateClient();
    MatchProperties properties = new MatchProperties();
    properties.getFeed().setQueueSize(240);
    properties.getFeed().setColdStartBhRatio(0.20);
    properties.getFeed().setQueueTtlDays(7);
    coldStartService = new ColdStartService(
        candidateClient,
        new FeedMergeService(),
        feedQueueRepository,
        properties);
  }

  @Test
  void buildAndPush_shouldWriteFeedQueue() {
    int written = coldStartService.buildAndPush(CALLER);
    assertEquals(240, written);
    assertEquals(240, feedQueueRepository.size(CALLER));
    assertEquals(Duration.ofDays(7), feedQueueRepository.getLastTtl());
  }

  @Test
  void buildAndPush_shouldFillWhenBhInsufficient() {
    candidateClient.resetPools(240, 10);
    int written = coldStartService.buildAndPush(CALLER);
    assertEquals(240, written);
  }

  @Test
  void buildAndPush_shouldExcludeCallerUserId() {
    candidateClient.resetPools(5, 5);
    coldStartService.buildAndPush(MockCandidateClient.BH_ID_BASE);
    assertFalse(feedQueueRepository.leftPop(MockCandidateClient.BH_ID_BASE, 100).stream()
        .anyMatch(item -> item.getTargetUserId() == MockCandidateClient.BH_ID_BASE));
  }

  @Test
  void buildAndPush_shouldUseMockPools() {
    coldStartService.buildAndPush(CALLER);
    assertTrue(feedQueueRepository.size(CALLER) > 0);
  }
}
