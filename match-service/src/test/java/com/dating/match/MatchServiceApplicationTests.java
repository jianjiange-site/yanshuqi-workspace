package com.dating.match;

import com.dating.match.health.MinioInfraHealthChecker;
import com.dating.match.health.NacosInfraHealthChecker;
import com.dating.match.health.PostgresInfraHealthChecker;
import com.dating.match.health.RedisInfraHealthChecker;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class MatchServiceApplicationTests {

    // 阶段 0 最小修复：测试 profile 排除 DataSource/Redis，需 Mock 基建健康检查器
    @MockBean
    private PostgresInfraHealthChecker postgresInfraHealthChecker;

    @MockBean
    private RedisInfraHealthChecker redisInfraHealthChecker;

    @MockBean
    private NacosInfraHealthChecker nacosInfraHealthChecker;

    @MockBean
    private MinioInfraHealthChecker minioInfraHealthChecker;

    @Test
    void contextLoads() {
    }
}
