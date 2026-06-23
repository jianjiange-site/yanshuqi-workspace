package com.dating.gateway.controller;

import com.dating.gateway.dto.vo.AvatarVO;
import com.dating.gateway.dto.vo.PresignAvatarUploadVO;
import com.dating.gateway.exception.GatewayBizException;
import com.dating.gateway.exception.GatewayErrorCode;
import com.dating.gateway.resolver.CallerUserResolver;
import com.dating.gateway.service.UploadBffService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UploadController.class)
@AutoConfigureMockMvc(addFilters = false)
class UploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UploadBffService uploadBffService;

    @MockBean
    private CallerUserResolver callerUserResolver;

    @Test
    void presign_shouldReturnPresignedUrl() throws Exception {
        when(callerUserResolver.resolveCallerUserId(any())).thenReturn(10001L);
        PresignAvatarUploadVO vo = new PresignAvatarUploadVO();
        vo.setPresignedUrl("https://minio/presigned");
        vo.setObjectKey("avatars/u1/key.jpg");
        vo.setExpiresAtMs(999999L);
        when(uploadBffService.presignAvatar(eq(10001L), any())).thenReturn(vo);

        mockMvc.perform(post("/api/v1/upload/presign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ext\":\"jpg\",\"expectedSizeBytes\":102400}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.presignedUrl").value("https://minio/presigned"))
                .andExpect(jsonPath("$.data.objectKey").value("avatars/u1/key.jpg"));

        verify(uploadBffService).presignAvatar(eq(10001L), any());
    }

    @Test
    void presign_invalidExt_shouldBeRejected() throws Exception {
        when(callerUserResolver.resolveCallerUserId(any())).thenReturn(10001L);
        when(uploadBffService.presignAvatar(eq(10001L), any()))
                .thenThrow(new GatewayBizException(GatewayErrorCode.INVALID_ARGUMENT, "ext 仅支持 jpg/jpeg/png/webp"));

        mockMvc.perform(post("/api/v1/upload/presign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ext\":\"gif\",\"expectedSizeBytes\":102400}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(10400));
    }

    @Test
    void presign_sizeExceeds10Mb_shouldBeRejected() throws Exception {
        when(callerUserResolver.resolveCallerUserId(any())).thenReturn(10001L);
        when(uploadBffService.presignAvatar(eq(10001L), any()))
                .thenThrow(new GatewayBizException(GatewayErrorCode.INVALID_ARGUMENT, "文件大小不能超过 10MB"));

        mockMvc.perform(post("/api/v1/upload/presign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ext\":\"jpg\",\"expectedSizeBytes\":10485761}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(10400));
    }

    @Test
    void confirm_shouldReturnAvatar() throws Exception {
        when(callerUserResolver.resolveCallerUserId(any())).thenReturn(10001L);
        AvatarVO vo = new AvatarVO();
        vo.setOriginalKey("avatars/u1/key.jpg");
        when(uploadBffService.confirmAvatar(eq(10001L), any())).thenReturn(vo);

        mockMvc.perform(post("/api/v1/upload/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"objectKey\":\"avatars/u1/key.jpg\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.originalKey").value("avatars/u1/key.jpg"));

        verify(uploadBffService).confirmAvatar(eq(10001L), any());
    }
}
