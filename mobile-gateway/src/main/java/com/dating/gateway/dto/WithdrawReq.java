package com.dating.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "提现请求")
public class WithdrawReq {

    private String accountId;
    private String idempotencyKey;
    private Long amountCent;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public Long getAmountCent() {
        return amountCent;
    }

    public void setAmountCent(Long amountCent) {
        this.amountCent = amountCent;
    }
}
