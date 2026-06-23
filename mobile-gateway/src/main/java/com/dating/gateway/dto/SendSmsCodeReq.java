package com.dating.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "发送短信验证码请求")
public class SendSmsCodeReq {

    @NotBlank(message = "phone 不能为空")
    @Schema(description = "E.164 手机号", example = "+8613812345678")
    private String phone;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
