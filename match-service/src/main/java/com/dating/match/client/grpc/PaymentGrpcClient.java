package com.dating.match.client.grpc;

import com.dating.match.client.PaymentClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * payment-service gRPC 金币客户端占位；ConsumeCoins RPC 尚未定义。
 */
@Component
@ConditionalOnProperty(prefix = "app.match.external", name = "payment-client-mode", havingValue = "grpc")
public class PaymentGrpcClient implements PaymentClient {

    private static final Logger log = LoggerFactory.getLogger(PaymentGrpcClient.class);

    @Override
    public boolean consumeCoins(long userId, int amount, String idempotencyKey) {
        log.error("payment-service ConsumeCoins not implemented userId={} amount={} key={}",
                userId, amount, idempotencyKey);
        throw GrpcClientSupport.notImplemented("payment-service", "ConsumeCoins");
    }
}
