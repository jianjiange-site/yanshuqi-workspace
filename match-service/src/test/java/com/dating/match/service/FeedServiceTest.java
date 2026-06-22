package com.dating.match.service;

import com.dating.match.client.MockCandidateClient;
import com.dating.match.client.MockPaymentClient;
import com.dating.match.client.MockSubscriptionClient;
import com.dating.match.config.MatchProperties;
import com.dating.match.constant.UserTypeConstant;
import com.dating.match.recommend.CandidateProfile;
import com.dating.match.recommend.ColdStartService;
import com.dating.match.recommend.FeedMergeService;
import com.dating.match.recommend.FeedQueueItem;
import com.dating.match.support.InMemoryFeedQueueRepository;
import com.dating.match.support.InMemoryQuotaHashRepository;
import com.dating.match.support.InMemorySwipedSetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeedServiceTest {

  private static final long CALLER = 10001L;

  private InMemoryFeedQueueRepository feedQueueRepository;
  private InMemorySwipedSetRepository swipedSetRepository;
  private QuotaService quotaService;
  private MockCandidateClient candidateClient;
  private FeedService feedService;

  @BeforeEach
  void setUp() {
    feedQueueRepository = new InMemoryFeedQueueRepository();
    swipedSetRepository = new InMemorySwipedSetRepository();
    quotaService = new QuotaService(
        new InMemoryQuotaHashRepository(),
        new MockSubscriptionClient(),
        new MockPaymentClient(),
        new MatchProperties());
    candidateClient = new MockCandidateClient();
    MatchProperties properties = new MatchProperties();
    properties.getFeed().setQueueSize(240);
    properties.getFeed().setColdStartBhRatio(0.20);
    properties.getFeed().setQueueTtlDays(7);
    ColdStartService coldStartService = new ColdStartService(
        candidateClient,
        new FeedMergeService(),
        feedQueueRepository,
        properties);
    feedService = new FeedService(
        quotaService,
        feedQueueRepository,
        swipedSetRepository,
        coldStartService,
        candidateClient);
  }

  @Test
  void getTodayFeed_shouldPopExistingQueue() {
    feedQueueRepository.pushAll(CALLER, List.of(
        new FeedQueueItem(20001L, UserTypeConstant.BH),
        new FeedQueueItem(20002L, UserTypeConstant.BH)), Duration.ofDays(7));
    var result = feedService.getTodayFeed(CALLER, 2);
    assertEquals(2, result.getCards().size());
    assertFalse(result.isExhausted());
  }

  @Test
  void getTodayFeed_shouldTriggerColdStartWhenEmpty() {
    var result = feedService.getTodayFeed(CALLER, 5);
    assertFalse(result.isExhausted());
    assertEquals(5, result.getCards().size());
    assertTrue(feedQueueRepository.size(CALLER) > 0);
  }

  @Test
  void getTodayFeed_shouldFilterSwipedTargets() {
    feedQueueRepository.pushAll(CALLER, List.of(
        new FeedQueueItem(20001L, UserTypeConstant.BH),
        new FeedQueueItem(20002L, UserTypeConstant.BH)), Duration.ofDays(7));
    swipedSetRepository.addSwiped(CALLER, 20001L);
    var result = feedService.getTodayFeed(CALLER, 1);
    assertEquals(1, result.getCards().size());
    assertEquals(20002L, result.getCards().get(0).getUserId());
  }

  @Test
  void getTodayFeed_shouldContinuePopAfterFilter() {
    feedQueueRepository.pushAll(CALLER, List.of(
        new FeedQueueItem(20001L, UserTypeConstant.BH),
        new FeedQueueItem(20002L, UserTypeConstant.BH),
        new FeedQueueItem(20003L, UserTypeConstant.BH)), Duration.ofDays(7));
    swipedSetRepository.addSwiped(CALLER, 20001L);
    var result = feedService.getTodayFeed(CALLER, 2);
    assertEquals(2, result.getCards().size());
  }

  @Test
  void getTodayFeed_shouldReturnExhaustedWhenCardsQuotaUsedUp() {
    for (int i = 0; i < 50; i++) {
      quotaService.consumeLeftSwipe(CALLER);
    }
    var result = feedService.getTodayFeed(CALLER, 5);
    assertTrue(result.isExhausted());
    assertTrue(result.getCards().isEmpty());
  }

  @Test
  void getTodayFeed_shouldNotConsumeCardsQuotaOnPop() {
    feedQueueRepository.pushAll(CALLER, List.of(new FeedQueueItem(20001L, UserTypeConstant.BH)), Duration.ofDays(7));
    feedService.getTodayFeed(CALLER, 1);
    assertEquals(0, quotaService.getUsage(CALLER).getCardsUsed());
  }

  @Test
  void getTodayFeed_shouldPopD1QueueBeforeColdStart() {
    feedQueueRepository.replaceAll(CALLER, List.of(
        new FeedQueueItem(30001L, UserTypeConstant.DH),
        new FeedQueueItem(30002L, UserTypeConstant.DH)), Duration.ofDays(7));
    var result = feedService.getTodayFeed(CALLER, 2);
    assertEquals(2, result.getCards().size());
    assertEquals(30001L, result.getCards().get(0).getUserId());
    assertFalse(result.isExhausted());
  }

  @Test
  void getTodayFeed_shouldStillColdStartWhenD1QueueEmpty() {
    assertEquals(0, feedQueueRepository.size(CALLER));
    var result = feedService.getTodayFeed(CALLER, 3);
    assertEquals(3, result.getCards().size());
    assertTrue(feedQueueRepository.size(CALLER) > 0);
  }

  @Test
  void getTodayFeed_shouldFilterSwipedAfterD1Queue() {
    feedQueueRepository.replaceAll(CALLER, List.of(
        new FeedQueueItem(20001L, UserTypeConstant.BH),
        new FeedQueueItem(20002L, UserTypeConstant.BH)), Duration.ofDays(7));
    swipedSetRepository.addSwiped(CALLER, 20001L);
    var result = feedService.getTodayFeed(CALLER, 1);
    assertEquals(20002L, result.getCards().get(0).getUserId());
  }

  @Test
  void getTodayFeed_shouldCapCountAt20() {
    feedQueueRepository.pushAll(CALLER, buildItems(25, 20001L), Duration.ofDays(7));
    assertEquals(20, feedService.getTodayFeed(CALLER, 30).getCards().size());
  }

  @Test
  void getTodayFeed_shouldSkipMissingProfile() {
    MatchProperties properties = new MatchProperties();
    properties.getFeed().setQueueSize(240);
    ColdStartService noopColdStart = new ColdStartService(
        candidateClient,
        new FeedMergeService(),
        feedQueueRepository,
        properties) {
      @Override
      public int buildAndPush(long callerUserId) {
        return 0;
      }
    };
    FeedService service = new FeedService(
        quotaService,
        feedQueueRepository,
        swipedSetRepository,
        noopColdStart,
        candidateClient);
    feedQueueRepository.pushAll(CALLER, List.of(new FeedQueueItem(99999L, UserTypeConstant.BH)), Duration.ofDays(7));
    var result = service.getTodayFeed(CALLER, 1);
    assertTrue(result.getCards().isEmpty());
  }

  private static List<FeedQueueItem> buildItems(int count, long baseId) {
    return java.util.stream.LongStream.range(0, count)
        .mapToObj(i -> new FeedQueueItem(baseId + i, UserTypeConstant.BH))
        .toList();
  }
}
