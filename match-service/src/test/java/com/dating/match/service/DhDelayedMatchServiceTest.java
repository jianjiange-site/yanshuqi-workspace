package com.dating.match.service;

import com.dating.match.config.MatchProperties;
import com.dating.match.constant.MatchSourceConstant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DhDelayedMatchServiceTest {

  private static final long USER = 10001L;
  private static final long DH = 30003L;

  @Mock
  private MatchCreationService matchCreationService;

  private ThreadPoolTaskScheduler scheduler;
  private DhDelayedMatchService dhDelayedMatchService;

  @BeforeEach
  void setUp() {
    MatchProperties properties = new MatchProperties();
    properties.setDhDelayedMatchMinMs(10);
    properties.setDhDelayedMatchMaxMs(50);

    scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(2);
    scheduler.setThreadNamePrefix("test-dh-delayed-");
    scheduler.initialize();

    dhDelayedMatchService = new DhDelayedMatchService(scheduler, matchCreationService, properties);
  }

  @AfterEach
  void tearDown() {
    scheduler.shutdown();
  }

  @Test
  void rightDh_shouldCreateMatchAfterDelay() {
    dhDelayedMatchService.scheduleDelayedMatch(USER, DH);
    verify(matchCreationService, timeout(500))
        .createMatch(USER, DH, MatchSourceConstant.SWIPE_MATCH);
  }

  @Test
  void delayedFailure_shouldNotAffectCaller() {
    doThrow(new RuntimeException("db down"))
        .when(matchCreationService).createMatch(USER, DH, MatchSourceConstant.SWIPE_MATCH);
    dhDelayedMatchService.scheduleDelayedMatch(USER, DH);
    verify(matchCreationService, timeout(500))
        .createMatch(USER, DH, MatchSourceConstant.SWIPE_MATCH);
  }
}
