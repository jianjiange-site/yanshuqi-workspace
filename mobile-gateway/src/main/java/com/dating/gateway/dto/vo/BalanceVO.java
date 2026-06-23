package com.dating.gateway.dto.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "钱包余额")
public class BalanceVO {

    private String currency;
    private Long availableBalanceCent;
    private Long frozenBalanceCent;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Long getAvailableBalanceCent() {
        return availableBalanceCent;
    }

    public void setAvailableBalanceCent(Long availableBalanceCent) {
        this.availableBalanceCent = availableBalanceCent;
    }

    public Long getFrozenBalanceCent() {
        return frozenBalanceCent;
    }

    public void setFrozenBalanceCent(Long frozenBalanceCent) {
        this.frozenBalanceCent = frozenBalanceCent;
    }
}
