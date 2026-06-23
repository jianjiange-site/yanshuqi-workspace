package com.dating.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "创建支付订单请求")
public class CreateOrderReq {

    private String productId;
    private String paymentMethod;
    private String currency;
    private Integer platform;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Integer getPlatform() {
        return platform;
    }

    public void setPlatform(Integer platform) {
        this.platform = platform;
    }
}
