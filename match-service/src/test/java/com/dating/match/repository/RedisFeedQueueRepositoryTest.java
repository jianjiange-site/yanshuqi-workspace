package com.dating.match.repository;

import com.dating.match.constant.RedisKeyConstants;
import com.dating.match.recommend.FeedQueueItem;
import com.dating.match.support.InMemoryFeedQueueRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisFeedQueueRepositoryTest {

  private static final long USER_ID = 10001L;

  @Mock
  private StringRedisTemplate stringRedisTemplate;

  @Mock
  private ListOperations<String, String> listOperations;

  @Test
  void feedQueueItem_shouldEncodeAndDecode() {
    FeedQueueItem item = new FeedQueueItem(20001L, 1);
    assertEquals("20001:1", item.encode());
    FeedQueueItem decoded = FeedQueueItem.decode("30001:2");
    assertEquals(30001L, decoded.getTargetUserId());
    assertEquals(2, decoded.getTargetUserType());
  }

  @Test
  void replaceAll_shouldDeleteAndPushFreshQueue() {
    when(stringRedisTemplate.opsForList()).thenReturn(listOperations);
    RedisFeedQueueRepository repository = new RedisFeedQueueRepository(stringRedisTemplate);
    String key = RedisKeyConstants.feedKey(USER_ID);
    List<FeedQueueItem> items = List.of(new FeedQueueItem(30001L, 2), new FeedQueueItem(30002L, 2));
    Duration ttl = Duration.ofDays(7);
    repository.replaceAll(USER_ID, items, ttl);
    verify(stringRedisTemplate).delete(key);
    verify(listOperations).rightPushAll(key, List.of("30001:2", "30002:2"));
    verify(stringRedisTemplate).expire(key, ttl);
  }

  @Test
  void inMemoryReplaceAll_shouldReplaceNotAppend() {
    InMemoryFeedQueueRepository memoryRepository = new InMemoryFeedQueueRepository();
    memoryRepository.pushAll(USER_ID, List.of(new FeedQueueItem(20001L, 1)), Duration.ofDays(7));
    memoryRepository.replaceAll(USER_ID, List.of(new FeedQueueItem(30001L, 2)), Duration.ofDays(7));
    assertEquals(1, memoryRepository.size(USER_ID));
    assertTrue(memoryRepository.wasLastReplace());
    assertEquals(30001L, memoryRepository.leftPop(USER_ID, 1).get(0).getTargetUserId());
  }

  @Test
  void pushAll_shouldSetTtlOnRedisKey() {
    when(stringRedisTemplate.opsForList()).thenReturn(listOperations);
    RedisFeedQueueRepository repository = new RedisFeedQueueRepository(stringRedisTemplate);
    List<FeedQueueItem> items = List.of(new FeedQueueItem(20001L, 1), new FeedQueueItem(20002L, 1));
    Duration ttl = Duration.ofDays(7);
    repository.pushAll(USER_ID, items, ttl);
    String key = RedisKeyConstants.feedKey(USER_ID);
    verify(listOperations).rightPushAll(key, List.of("20001:1", "20002:1"));
    verify(stringRedisTemplate).expire(key, ttl);
  }

  @Test
  void leftPop_shouldDecodeElementsFromRedis() {
    when(stringRedisTemplate.opsForList()).thenReturn(listOperations);
    RedisFeedQueueRepository repository = new RedisFeedQueueRepository(stringRedisTemplate);
    when(listOperations.leftPop(RedisKeyConstants.feedKey(USER_ID))).thenReturn("20001:1", (String) null);
    List<FeedQueueItem> popped = repository.leftPop(USER_ID, 2);
    assertEquals(1, popped.size());
    assertEquals(20001L, popped.get(0).getTargetUserId());
  }

  @Test
  void inMemoryPushAll_shouldIncreaseSize() {
    InMemoryFeedQueueRepository memoryRepository = new InMemoryFeedQueueRepository();
    memoryRepository.pushAll(USER_ID, List.of(
        new FeedQueueItem(20001L, 1),
        new FeedQueueItem(20002L, 1)), Duration.ofDays(7));
    assertEquals(2, memoryRepository.size(USER_ID));
  }

  @Test
  void inMemoryLeftPop_shouldReturnFifoOrder() {
    InMemoryFeedQueueRepository memoryRepository = new InMemoryFeedQueueRepository();
    memoryRepository.pushAll(USER_ID, List.of(
        new FeedQueueItem(20001L, 1),
        new FeedQueueItem(20002L, 1)), Duration.ofDays(7));
    List<FeedQueueItem> popped = memoryRepository.leftPop(USER_ID, 2);
    assertEquals(20001L, popped.get(0).getTargetUserId());
    assertEquals(20002L, popped.get(1).getTargetUserId());
  }
}
