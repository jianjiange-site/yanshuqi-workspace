package com.dating.gateway.controller;

import com.dating.gateway.service.impl.ImBffServiceImpl;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OpenImCallbackControllerTest {

    @Nested
    @WebMvcTest(OpenImCallbackController.class)
    @AutoConfigureMockMvc(addFilters = false)
    @Import(ImBffServiceImpl.class)
    @ActiveProfiles("prod")
    class CallbackTests {

        @Autowired
        private MockMvc mockMvc;

        @Test
        void callback_withoutJwt_shouldBeAccessible() throws Exception {
            mockMvc.perform(post("/callback/openim/callbackBeforeSendSingleMsg")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("operationID", "test-op-001")
                            .content("{\"sendID\":\"10001\",\"recvID\":\"10002\",\"content\":\"hello\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.errCode").value(10803))
                    .andExpect(jsonPath("$.errMsg").value("OpenIM callback 转发尚未就绪"));
        }

        @Test
        void callback_shouldAcceptCommandAndOperationId() throws Exception {
            mockMvc.perform(post("/callback/openim/callbackAfterSendSingleMsg")
                            .header("operationID", "op-123")
                            .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.errDlt").value("callbackCommand=callbackAfterSendSingleMsg"));
        }
    }

    @Nested
    @WebMvcTest(OpenImCallbackController.class)
    @AutoConfigureMockMvc(addFilters = false)
    @Import(com.dating.gateway.service.impl.MockImBffServiceImpl.class)
    @ActiveProfiles("test")
    class MockCallbackTests {

        @Autowired
        private MockMvc mockMvc;

        @Test
        void callback_mockProfile_shouldReturnSuccessShape() throws Exception {
            mockMvc.perform(post("/callback/openim/callbackBeforeSendSingleMsg")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"sendID\":\"10001\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.actionCode").value(0))
                    .andExpect(jsonPath("$.errCode").value(0));
        }
    }
}
