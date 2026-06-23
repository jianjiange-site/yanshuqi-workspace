package com.dating.gateway.dto.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "发送短信验证码响应")
public class SendSmsCodeVO {

    @Schema(description = "dev/test mock 验证码，prod 不返回")
    private String mockCode;

    public String getMockCode() {
        return mockCode;
    }

    public void setMockCode(String mockCode) {
        this.mockCode = mockCode;
    }
}
