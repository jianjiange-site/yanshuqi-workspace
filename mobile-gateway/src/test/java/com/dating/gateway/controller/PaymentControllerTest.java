package com.dating.gateway.controller;

import com.dating.gateway.resolver.CallerUserResolver;
import com.dating.gateway.service.PaymentBffService;
import com.dating.gateway.service.impl.MockPaymentBffServiceImpl;
import com.dating.gateway.service.impl.PaymentBffServiceImpl;
import com.dating.gateway.support.GatewayFeatureNotReadySupport;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentControllerTest {

    @Nested
    @WebMvcTest(PaymentController.class)
    @AutoConfigureMockMvc(addFilters = false)
    class NotReadyTests {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private CallerUserResolver callerUserResolver;

        @MockBean
        private PaymentBffService paymentBffService;

        @Test
        void products_shouldReturnNotReady() throws Exception {
            when(callerUserResolver.resolveCallerUserId(any())).thenReturn(10001L);
            when(paymentBffService.listProducts(10001L))
                    .thenThrow(GatewayFeatureNotReadySupport.paymentNotReady());

            mockMvc.perform(get("/api/v1/payment/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(10701))
                    .andExpect(jsonPath("$.message").value("payment-service 尚未就绪"));
        }

        @Test
        void coins_shouldReturnNotReady() throws Exception {
            when(callerUserResolver.resolveCallerUserId(any())).thenReturn(10001L);
            when(paymentBffService.getCoins(10001L))
                    .thenThrow(GatewayFeatureNotReadySupport.paymentNotReady());

            mockMvc.perform(get("/api/v1/payment/coins"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(10701));
        }
    }

    @Nested
    @WebMvcTest(PaymentController.class)
    @AutoConfigureMockMvc(addFilters = false)
    @Import(PaymentBffServiceImpl.class)
    @ActiveProfiles("prod")
    class ValidationTests {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private CallerUserResolver callerUserResolver;

        @Test
        void createOrder_missingProductId_shouldReturnInvalidArgument() throws Exception {
            when(callerUserResolver.resolveCallerUserId(any())).thenReturn(10001L);

            mockMvc.perform(post("/api/v1/payment/order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"paymentMethod\":\"IAP\",\"currency\":\"USD\",\"platform\":1}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(10400))
                    .andExpect(jsonPath("$.message").value("productId 不能为空"));
        }

        @Test
        void withdraw_missingIdempotencyKey_shouldReturnInvalidArgument() throws Exception {
            when(callerUserResolver.resolveCallerUserId(any())).thenReturn(10001L);

            mockMvc.perform(post("/api/v1/payment/withdraw")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"accountId\":\"acc-1\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(10400))
                    .andExpect(jsonPath("$.message").value("idempotencyKey 不能为空"));
        }
    }

    @Nested
    @WebMvcTest(PaymentController.class)
    @AutoConfigureMockMvc(addFilters = false)
    @Import(MockPaymentBffServiceImpl.class)
    @ActiveProfiles("test")
    class MockProfileTests {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private CallerUserResolver callerUserResolver;

        @Test
        void products_shouldReturnMockData() throws Exception {
            when(callerUserResolver.resolveCallerUserId(any())).thenReturn(10001L);

            mockMvc.perform(get("/api/v1/payment/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data[0].productId").value("coin_pack_100"))
                    .andExpect(jsonPath("$.data[1].productId").value("premium_monthly"));
        }

        @Test
        void coins_shouldReturnMockBalance() throws Exception {
            when(callerUserResolver.resolveCallerUserId(any())).thenReturn(10001L);

            mockMvc.perform(get("/api/v1/payment/coins"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.balance").value(100));
        }
    }
}
