package com.dating.user;

import com.dating.user.health.MinioInfraHealthChecker;
import com.dating.user.health.NacosInfraHealthChecker;
import com.dating.user.health.PostgresInfraHealthChecker;
import com.dating.user.health.RedisInfraHealthChecker;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceApplicationTests {

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
