package com.dating.match.client;

import com.dating.match.client.grpc.ImGrpcClient;
import com.dating.match.recommend.ColdStartService;
import com.dating.match.recommend.D1Generator;
import com.dating.match.service.MatchOutboxRetryService;
import com.dating.match.service.QuotaService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 防腐层边界：业务 service 依赖接口而非 Mock 实现。
 */
class ClientBoundaryTest {

    @Test
    void quotaService_shouldDependOnPaymentClientInterface() {
        assertDependencyType(QuotaService.class, PaymentClient.class);
    }

    @Test
    void coldStartService_shouldDependOnCandidateClientInterface() {
        assertDependencyType(ColdStartService.class, CandidateClient.class);
    }

    @Test
    void d1Generator_shouldDependOnCandidateClientInterface() {
        assertTrue(hasDependency(D1Generator.class, CandidateClient.class)
                || hasDependency(D1Generator.class, com.dating.match.recommend.PreferenceBuilder.class));
    }

    @Test
    void matchOutboxRetryService_shouldDependOnImClientInterface() {
        assertDependencyType(MatchOutboxRetryService.class, ImClient.class);
    }

    @Test
    void productionServices_shouldNotReferenceMockImplementations() {
        assertTrue(QuotaService.class.getName().contains("QuotaService"));
        assertTrue(!containsFieldType(QuotaService.class, MockPaymentClient.class));
        assertTrue(!containsFieldType(MatchOutboxRetryService.class, ImGrpcClient.class));
    }

    private static void assertDependencyType(Class<?> serviceClass, Class<?> expectedInterface) {
        assertTrue(hasDependency(serviceClass, expectedInterface),
                serviceClass.getSimpleName() + " should depend on " + expectedInterface.getSimpleName());
    }

    private static boolean hasDependency(Class<?> serviceClass, Class<?> expectedType) {
        for (Constructor<?> constructor : serviceClass.getConstructors()) {
            for (Parameter parameter : constructor.getParameters()) {
                if (expectedType.isAssignableFrom(parameter.getType())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean containsFieldType(Class<?> serviceClass, Class<?> type) {
        for (var field : serviceClass.getDeclaredFields()) {
            if (type.isAssignableFrom(field.getType())) {
                return true;
            }
        }
        return false;
    }
}
