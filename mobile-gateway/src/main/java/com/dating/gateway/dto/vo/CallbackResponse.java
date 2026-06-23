package com.dating.gateway.dto.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * OpenIM 服务端回调响应体（非 {@link com.dating.gateway.common.Result} 包装）。
 */
@Schema(description = "OpenIM 回调响应")
public class CallbackResponse {

    private Integer actionCode;
    private Integer errCode;
    private String errMsg;
    private String errDlt;
    private String nextCode;

    public Integer getActionCode() {
        return actionCode;
    }

    public void setActionCode(Integer actionCode) {
        this.actionCode = actionCode;
    }

    public Integer getErrCode() {
        return errCode;
    }

    public void setErrCode(Integer errCode) {
        this.errCode = errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public String getErrDlt() {
        return errDlt;
    }

    public void setErrDlt(String errDlt) {
        this.errDlt = errDlt;
    }

    public String getNextCode() {
        return nextCode;
    }

    public void setNextCode(String nextCode) {
        this.nextCode = nextCode;
    }
}
