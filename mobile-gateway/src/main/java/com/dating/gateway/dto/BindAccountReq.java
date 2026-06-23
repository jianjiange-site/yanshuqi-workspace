package com.dating.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "绑定提现账户请求")
public class BindAccountReq {

    private String type;
    private String accountIdentifier;
    private String holderName;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAccountIdentifier() {
        return accountIdentifier;
    }

    public void setAccountIdentifier(String accountIdentifier) {
        this.accountIdentifier = accountIdentifier;
    }

    public String getHolderName() {
        return holderName;
    }

    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }
}
