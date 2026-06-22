package com.dating.match.client;

import com.dating.match.client.grpc.ImGrpcClient;
import com.dating.match.client.grpc.PaymentGrpcClient;
import com.dating.match.client.grpc.UserServiceCandidateClient;
import com.dating.match.health.MinioInfraHealthChecker;
import com.dating.match.health.NacosInfraHealthChecker;
import com.dating.match.health.PostgresInfraHealthChecker;
import com.dating.match.health.RedisInfraHealthChecker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@SpringBootTest
@ActiveProfiles("test")
class ExternalClientModeConfigTest {

    @MockBean
    private PostgresInfraHealthChecker postgresInfraHealthChecker;
    @MockBean
    private RedisInfraHealthChecker redisInfraHealthChecker;
    @MockBean
    private NacosInfraHealthChecker nacosInfraHealthChecker;
    @MockBean
    private MinioInfraHealthChecker minioInfraHealthChecker;

    @Autowired
    private CandidateClient candidateClient;
    @Autowired
    private PaymentClient paymentClient;
    @Autowired
    private ImClient imClient;

    @Test
    void defaultMockMode_shouldLoadMockCandidateClient() {
        assertInstanceOf(MockCandidateClient.class, candidateClient);
    }

    @Test
    void defaultMockMode_shouldLoadMockPaymentClient() {
        assertInstanceOf(MockPaymentClient.class, paymentClient);
    }

    @Test
    void defaultMockMode_shouldLoadMockImClient() {
        assertInstanceOf(MockImClient.class, imClient);
    }

    @SpringBootTest
    @ActiveProfiles("test")
    @TestPropertySource(properties = {
            "app.match.external.user-client-mode=grpc",
            "app.match.external.payment-client-mode=grpc",
            "app.match.external.im-client-mode=grpc",
            "grpc.client.user-service.address=static://127.0.0.1:9091",
            "grpc.client.payment-service.address=static://127.0.0.1:9095",
            "grpc.client.im-service.address=static://127.0.0.1:9093"
    })
    static class GrpcModeTest {

        @MockBean
        private PostgresInfraHealthChecker postgresInfraHealthChecker;
        @MockBean
        private RedisInfraHealthChecker redisInfraHealthChecker;
        @MockBean
        private NacosInfraHealthChecker nacosInfraHealthChecker;
        @MockBean
        private MinioInfraHealthChecker minioInfraHealthChecker;

        @Autowired
        private CandidateClient candidateClient;
        @Autowired
        private PaymentClient paymentClient;
        @Autowired
        private ImClient imClient;

        @Test
        void grpcMode_shouldLoadGrpcPlaceholderClients() {
            assertInstanceOf(UserServiceCandidateClient.class, candidateClient);
            assertInstanceOf(PaymentGrpcClient.class, paymentClient);
            assertInstanceOf(ImGrpcClient.class, imClient);
        }
    }
}
