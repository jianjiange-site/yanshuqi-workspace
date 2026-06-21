package com.dating.match.client;

/**
 * 金币扣减客户端，后续替换为 payment-service gRPC。
 */
public interface PaymentClient {

    /**
     * 扣减金币，幂等键由调用方提供。
     *
     * @return true 表示扣减成功
     */
    boolean consumeCoins(long userId, int amount, String idempotencyKey);
}
