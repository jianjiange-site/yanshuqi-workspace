package com.dating.gateway;

import com.dating.gateway.health.MinioInfraHealthChecker;
import com.dating.gateway.health.NacosInfraHealthChecker;
import com.dating.gateway.health.PostgresInfraHealthChecker;
import com.dating.gateway.health.RedisInfraHealthChecker;
import com.dating.gateway.client.UserAuthGrpcClient;
import com.dating.gateway.manager.AuthDeviceManager;
import com.dating.gateway.manager.AuthRefreshTokenManager;
import com.dating.gateway.security.TokenBlacklistService;
import com.dating.gateway.service.MatchGrpcClient;
import com.dating.gateway.service.SmsCodeService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class GatewayServiceApplicationTests {

    // 阶段 0 最小修复：测试 profile 排除 DataSource/Redis，需 Mock 基建健康检查器
    @MockBean
    private PostgresInfraHealthChecker postgresInfraHealthChecker;

    @MockBean
    private RedisInfraHealthChecker redisInfraHealthChecker;

    @MockBean
    private NacosInfraHealthChecker nacosInfraHealthChecker;

    @MockBean
    private MinioInfraHealthChecker minioInfraHealthChecker;

    @MockBean
    private MatchGrpcClient matchGrpcClient;

    @MockBean
    private UserAuthGrpcClient userAuthGrpcClient;

    @MockBean
    private com.dating.gateway.client.UserProfileGrpcClient userProfileGrpcClient;

    @MockBean
    private com.dating.gateway.service.ProfileBffService profileBffService;

    @MockBean
    private com.dating.gateway.service.UploadBffService uploadBffService;

    @MockBean
    private com.dating.gateway.service.HomeBffService homeBffService;

    @MockBean
    private com.dating.gateway.client.PostGrpcClient postGrpcClient;

    @MockBean
    private com.dating.gateway.service.PostBffService postBffService;

    @MockBean
    private AuthDeviceManager authDeviceManager;

    @MockBean
    private AuthRefreshTokenManager authRefreshTokenManager;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    @MockBean
    private SmsCodeService smsCodeService;

    @Test
    void contextLoads() {
    }
}
