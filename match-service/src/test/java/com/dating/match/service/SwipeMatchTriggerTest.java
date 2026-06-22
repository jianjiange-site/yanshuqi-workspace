package com.dating.match.service;

import com.dating.match.config.MatchProperties;
import com.dating.match.client.MockPaymentClient;
import com.dating.match.client.MockSubscriptionClient;
import com.dating.match.client.MockTargetUserTypeResolver;
import com.dating.match.constant.MatchSourceConstant;
import com.dating.match.constant.SwipeDirectionConstant;
import com.dating.match.constant.UserTypeConstant;
import com.dating.match.entity.UserSwipeHistoryEntity;
import com.dating.match.grpc.proto.SwipeDirection;
import com.dating.match.manager.SwipeHistoryManager;
import com.dating.match.support.FakeSwipeLockExecutor;
import com.dating.match.support.InMemoryQuotaHashRepository;
import com.dating.match.support.InMemorySuperHiIdempotencyStore;
import com.dating.match.support.InMemorySwipedSetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SwipeMatchTriggerTest {

  private static final long CALLER = 10001L;
  private static final long TARGET = 20002L;

  @Mock
  private SwipeHistoryManager swipeHistoryManager;

  @Mock
  private MatchCreationService matchCreationService;

  @Mock
  private DhDelayedMatchService dhDelayedMatchService;

  private MockTargetUserTypeResolver targetUserTypeResolver;
  private SwipeService swipeService;

  @BeforeEach
  void setUp() {
    targetUserTypeResolver = new MockTargetUserTypeResolver();
    swipeService = new SwipeService(
        new FakeSwipeLockExecutor(),
        swipeHistoryManager,
        new QuotaService(new InMemoryQuotaHashRepository(), new MockSubscriptionClient(), new MockPaymentClient(), new MatchProperties()),
        new InMemorySwipedSetRepository(),
        new InMemorySuperHiIdempotencyStore(),
        targetUserTypeResolver,
        matchCreationService,
        dhDelayedMatchService);
  }

  private void stubNewSwipe() {
    when(swipeHistoryManager.findByUserIdAndTargetUserId(CALLER, TARGET)).thenReturn(Optional.empty());
    when(swipeHistoryManager.insertIfAbsent(eq(CALLER), eq(TARGET), anyInt(), anyInt(), isNull()))
        .thenReturn(new UserSwipeHistoryEntity());
  }

  @Test
  void left_shouldNotCreateMatch() {
    stubNewSwipe();
    assertEquals(0L, swipeService.swipe(CALLER, TARGET, SwipeDirection.LEFT).getMatchId());
    verify(matchCreationService, never()).createMatch(anyLong(), anyLong(), anyString());
    verify(dhDelayedMatchService, never()).scheduleDelayedMatch(anyLong(), anyLong());
  }

  @Test
  void rightBh_targetNotLikeMe_shouldNotCreateMatch() {
    stubNewSwipe();
    when(swipeHistoryManager.hasPositiveSwipe(TARGET, CALLER)).thenReturn(false);
    assertEquals(0L, swipeService.swipe(CALLER, TARGET, SwipeDirection.RIGHT).getMatchId());
    verify(matchCreationService, never()).createMatch(anyLong(), anyLong(), anyString());
  }

  @Test
  void rightBh_targetRightMe_shouldCreateMatchImmediately() {
    stubNewSwipe();
    when(swipeHistoryManager.hasPositiveSwipe(TARGET, CALLER)).thenReturn(true);
    when(matchCreationService.createMatch(CALLER, TARGET, MatchSourceConstant.SWIPE_MATCH)).thenReturn(55501L);
    assertEquals(55501L, swipeService.swipe(CALLER, TARGET, SwipeDirection.RIGHT).getMatchId());
  }

  @Test
  void rightBh_targetSuperHiMe_shouldCreateMatchImmediately() {
    stubNewSwipe();
    when(swipeHistoryManager.hasPositiveSwipe(TARGET, CALLER)).thenReturn(true);
    when(matchCreationService.createMatch(CALLER, TARGET, MatchSourceConstant.SWIPE_MATCH)).thenReturn(55502L);
    assertEquals(55502L, swipeService.swipe(CALLER, TARGET, SwipeDirection.RIGHT).getMatchId());
  }

  @Test
  void rightDh_shouldScheduleDelayedMatchOnly() {
    stubNewSwipe();
    targetUserTypeResolver.setUserType(TARGET, UserTypeConstant.DH);
    assertEquals(0L, swipeService.swipe(CALLER, TARGET, SwipeDirection.RIGHT).getMatchId());
    verify(dhDelayedMatchService).scheduleDelayedMatch(CALLER, TARGET);
    verify(matchCreationService, never()).createMatch(anyLong(), anyLong(), anyString());
  }

  @Test
  void superHiBh_shouldCreateMatchImmediately() {
    stubNewSwipe();
    when(matchCreationService.createMatch(CALLER, TARGET, MatchSourceConstant.SWIPE_SUPER_HI)).thenReturn(66601L);
    assertEquals(66601L, swipeService.superHi(CALLER, TARGET, "req-bh").getMatchId());
  }

  @Test
  void superHiDh_shouldCreateMatchImmediately() {
    stubNewSwipe();
    targetUserTypeResolver.setUserType(TARGET, UserTypeConstant.DH);
    when(matchCreationService.createMatch(CALLER, TARGET, MatchSourceConstant.SWIPE_SUPER_HI)).thenReturn(66602L);
    assertEquals(66602L, swipeService.superHi(CALLER, TARGET, "req-dh").getMatchId());
  }

  @Test
  void duplicateUserTarget_shouldNotUpgradeSuperHiOrCreateMatch() {
    when(swipeHistoryManager.findByUserIdAndTargetUserId(CALLER, TARGET))
        .thenReturn(Optional.of(history(SwipeDirectionConstant.LEFT)));
    assertEquals(0L, swipeService.superHi(CALLER, TARGET, "req-dup").getMatchId());
    verify(matchCreationService, never()).createMatch(anyLong(), anyLong(), anyString());
    verify(swipeHistoryManager, never()).insertIfAbsent(anyLong(), anyLong(), anyInt(), anyInt(), isNull());
  }

  private static UserSwipeHistoryEntity history(int direction) {
    UserSwipeHistoryEntity entity = new UserSwipeHistoryEntity();
    entity.setDirection(direction);
    return entity;
  }
}
