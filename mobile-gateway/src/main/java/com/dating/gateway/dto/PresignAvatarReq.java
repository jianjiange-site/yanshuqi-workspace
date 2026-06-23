package com.dating.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "头像上传 presign 请求")
public class PresignAvatarReq {

    @NotBlank(message = "ext 不能为空")
    @Schema(description = "文件扩展名：jpg/jpeg/png/webp")
    private String ext;

    @NotNull(message = "expectedSizeBytes 不能为空")
    @Schema(description = "预期文件大小（字节），最大 10MB")
    private Long expectedSizeBytes;

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public Long getExpectedSizeBytes() {
        return expectedSizeBytes;
    }

    public void setExpectedSizeBytes(Long expectedSizeBytes) {
        this.expectedSizeBytes = expectedSizeBytes;
    }
}
