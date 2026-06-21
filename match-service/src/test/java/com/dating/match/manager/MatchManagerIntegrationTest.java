package com.dating.match.manager;

import com.dating.match.health.MinioInfraHealthChecker;
import com.dating.match.health.NacosInfraHealthChecker;
import com.dating.match.health.PostgresInfraHealthChecker;
import com.dating.match.health.RedisInfraHealthChecker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@ActiveProfiles("postgres-test")
class MatchManagerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("match_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",
                () -> POSTGRES.getJdbcUrl() + "?currentSchema=match_center&stringtype=unspecified");
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @MockBean
    private PostgresInfraHealthChecker postgresInfraHealthChecker;

    @MockBean
    private RedisInfraHealthChecker redisInfraHealthChecker;

    @MockBean
    private NacosInfraHealthChecker nacosInfraHealthChecker;

    @MockBean
    private MinioInfraHealthChecker minioInfraHealthChecker;

    @Autowired
    private SwipeHistoryManager swipeHistoryManager;

    @Autowired
    private MatchManager matchManager;

    @Autowired
    private MatchOutboxManager matchOutboxManager;

    @Autowired
    private ProfileVisitManager profileVisitManager;

    @Test
    void swipeHistory_insertIfAbsent_shouldBeIdempotentByUserTarget() {
        var first = swipeHistoryManager.insertIfAbsent(1001L, 2002L, 1, 2, null);
        var second = swipeHistoryManager.insertIfAbsent(1001L, 2002L, 1, 1, null);

        assertNotNull(first.getBizId());
        assertEquals(first.getId(), second.getId());
        assertEquals(first.getBizId(), second.getBizId());
    }

    @Test
    void match_insertIfAbsent_shouldNormalizePair() {
        var ab = matchManager.insertIfAbsent(3001L, 4002L, "SWIPE_MATCH");
        var ba = matchManager.insertIfAbsent(4002L, 3001L, "SWIPE_MATCH");

        assertSame(ab.getId(), ba.getId());
        assertEquals(ab.getBizId(), ba.getBizId());
        assertEquals(3001L, ab.getUserIdLow());
        assertEquals(4002L, ab.getUserIdHigh());
    }

    @Test
    void profileVisit_upsertVisit_shouldIncrementVisitCount() {
        profileVisitManager.upsertVisit(5001L, 6002L);
        profileVisitManager.upsertVisit(5001L, 6002L);

        var visit = profileVisitManager.findByPair(5001L, 6002L);
        assertNotNull(visit);
        assertEquals(2, visit.getVisitCount());
    }

    @Test
    void matchOutbox_createPending_shouldPersistPendingRow() {
        var match = matchManager.insertIfAbsent(7001L, 8002L, "SWIPE_SUPER_HI");
        var outbox = matchOutboxManager.createPending(
                match.getBizId(), "ENSURE_CONVERSATION", "{\"matchBizId\":" + match.getBizId() + "}",
                java.time.Instant.now());

        assertNotNull(outbox.getBizId());
        assertEquals(MatchOutboxManager.STATUS_PENDING, outbox.getStatus());
        assertEquals(match.getBizId(), outbox.getMatchBizId());
    }
}
