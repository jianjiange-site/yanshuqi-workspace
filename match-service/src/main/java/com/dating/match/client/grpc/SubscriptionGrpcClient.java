package com.dating.match.client.grpc;

import com.dating.match.client.SubscriptionClient;
import com.dating.match.subscription.SubscriptionTier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * payment-service gRPC 订阅档位客户端占位；GetSubscription RPC 尚未定义。
 */
@Component
@ConditionalOnProperty(prefix = "app.match.external", name = "payment-client-mode", havingValue = "grpc")
public class SubscriptionGrpcClient implements SubscriptionClient {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionGrpcClient.class);

    @Override
    public SubscriptionTier getTier(long userId) {
        log.error("payment-service GetSubscription not implemented userId={}", userId);
        throw GrpcClientSupport.notImplemented("payment-service", "GetSubscription");
    }
}
