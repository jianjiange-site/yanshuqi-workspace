package com.dating.match.service;

import com.dating.match.client.MockImClient;
import com.dating.match.constant.MatchOutboxAction;
import com.dating.match.constant.MatchOutboxStatus;
import com.dating.match.entity.MatchOutboxEntity;
import com.dating.match.manager.MatchOutboxManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchOutboxRetryServiceTest {

  @Mock
  private MatchOutboxManager matchOutboxManager;

  private MockImClient imClient;
  private MatchOutboxRetryService retryService;

  @BeforeEach
  void setUp() {
    imClient = new MockImClient();
    retryService = new MatchOutboxRetryService(matchOutboxManager, imClient);
  }

  @Test
  void pendingSuccess_shouldMarkDone() {
    MatchOutboxEntity entity = pending(1L, 0);
    when(matchOutboxManager.listPendingForRetry(10)).thenReturn(List.of(entity));

    retryService.retryPending(10);

    verify(matchOutboxManager).markDone(1L);
    verify(matchOutboxManager, never()).markDead(any());
  }

  @Test
  void mockImFail_shouldIncreaseAttempts() {
    imClient.setFailCount(1);
    MatchOutboxEntity entity = pending(2L, 0);
    when(matchOutboxManager.listPendingForRetry(10)).thenReturn(List.of(entity));

    retryService.retryPending(10);

    verify(matchOutboxManager).increaseAttemptsAndDelay(eq(2L), any(Instant.class));
    verify(matchOutboxManager, never()).markDone(2L);
  }

  @Test
  void exceedMaxAttempts_shouldMarkDead() {
    imClient.setFailCount(1);
    MatchOutboxEntity entity = pending(3L, 4);
    when(matchOutboxManager.listPendingForRetry(10)).thenReturn(List.of(entity));

    retryService.retryPending(10);

    verify(matchOutboxManager).markDead(3L);
  }

  @Test
  void doneOrDead_shouldNotProcess() {
    MatchOutboxEntity done = pending(4L, 0);
    done.setStatus(MatchOutboxStatus.DONE);
    when(matchOutboxManager.listPendingForRetry(10)).thenReturn(List.of(done));

    retryService.retryPending(10);

    verify(matchOutboxManager, never()).markDone(4L);
    verify(matchOutboxManager, never()).markDead(4L);
  }

  private static MatchOutboxEntity pending(long id, int attempts) {
    MatchOutboxEntity entity = new MatchOutboxEntity();
    entity.setId(id);
    entity.setMatchBizId(90001L);
    entity.setAction(MatchOutboxAction.ENSURE_CONVERSATION);
    entity.setPayloadJson("{\"matchBizId\":90001}");
    entity.setAttempts(attempts);
    entity.setStatus(MatchOutboxStatus.PENDING);
    entity.setNextRetryAt(OffsetDateTime.now(ZoneOffset.UTC));
    return entity;
  }
}
