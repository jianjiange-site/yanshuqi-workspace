package com.dating.match.service;

import com.dating.match.client.MockTargetUserTypeResolver;
import com.dating.match.constant.MatchOutboxAction;
import com.dating.match.constant.MatchSourceConstant;
import com.dating.match.constant.UserTypeConstant;
import com.dating.match.dto.MatchInsertResult;
import com.dating.match.entity.MatchEntity;
import com.dating.match.manager.MatchManager;
import com.dating.match.manager.MatchOutboxManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchCreationServiceTest {

  private static final long USER_A = 10001L;
  private static final long USER_B = 20002L;

  @Mock
  private MatchManager matchManager;

  @Mock
  private MatchOutboxManager matchOutboxManager;

  private MockTargetUserTypeResolver targetUserTypeResolver;
  private MatchCreationService matchCreationService;

  @BeforeEach
  void setUp() {
    targetUserTypeResolver = new MockTargetUserTypeResolver();
    matchCreationService = new MatchCreationService(matchManager, matchOutboxManager, targetUserTypeResolver);
  }

  @Test
  void firstCreate_shouldReturnBizIdAndWriteOutbox() {
    MatchEntity entity = new MatchEntity();
    entity.setBizId(90001L);
    entity.setId(1L);
    when(matchManager.insertIfAbsentWithResult(USER_A, USER_B, MatchSourceConstant.SWIPE_MATCH))
        .thenReturn(new MatchInsertResult(entity, true));

    long bizId = matchCreationService.createMatch(USER_A, USER_B, MatchSourceConstant.SWIPE_MATCH);

    assertEquals(90001L, bizId);
    verify(matchOutboxManager, times(3)).createPending(eq(90001L), any(), any(), any(Instant.class));
  }

  @Test
  void duplicatePair_shouldReturnSameBizIdWithoutOutbox() {
    MatchEntity entity = new MatchEntity();
    entity.setBizId(90001L);
    when(matchManager.insertIfAbsentWithResult(USER_A, USER_B, MatchSourceConstant.SWIPE_MATCH))
        .thenReturn(new MatchInsertResult(entity, false));
    when(matchManager.insertIfAbsentWithResult(USER_B, USER_A, MatchSourceConstant.SWIPE_MATCH))
        .thenReturn(new MatchInsertResult(entity, false));

    assertEquals(90001L, matchCreationService.createMatch(USER_A, USER_B, MatchSourceConstant.SWIPE_MATCH));
    assertEquals(90001L, matchCreationService.createMatch(USER_B, USER_A, MatchSourceConstant.SWIPE_MATCH));
    verify(matchOutboxManager, never()).createPending(any(), any(), any(), any());
  }

  @Test
  void outbox_shouldUseMatchBizIdNotPhysicalId() {
    MatchEntity entity = new MatchEntity();
    entity.setBizId(88888L);
    entity.setId(99L);
    when(matchManager.insertIfAbsentWithResult(USER_A, USER_B, MatchSourceConstant.SWIPE_SUPER_HI))
        .thenReturn(new MatchInsertResult(entity, true));

    matchCreationService.createMatch(USER_A, USER_B, MatchSourceConstant.SWIPE_SUPER_HI);

    ArgumentCaptor<Long> matchBizIdCaptor = ArgumentCaptor.forClass(Long.class);
    verify(matchOutboxManager).createPending(
        matchBizIdCaptor.capture(), eq(MatchOutboxAction.ENSURE_CONVERSATION), any(), any(Instant.class));
    assertEquals(88888L, matchBizIdCaptor.getValue());
  }

  @Test
  void dhInvolved_shouldWriteTriggerDhOpening() {
    targetUserTypeResolver.setUserType(USER_B, UserTypeConstant.DH);
    MatchEntity entity = new MatchEntity();
    entity.setBizId(70001L);
    when(matchManager.insertIfAbsentWithResult(USER_A, USER_B, MatchSourceConstant.SWIPE_MATCH))
        .thenReturn(new MatchInsertResult(entity, true));

    matchCreationService.createMatch(USER_A, USER_B, MatchSourceConstant.SWIPE_MATCH);

    verify(matchOutboxManager).createPending(
        eq(70001L), eq(MatchOutboxAction.TRIGGER_DH_OPENING), any(), any(Instant.class));
  }
}
