package com.dating.match.client;

import com.dating.match.subscription.SubscriptionTier;

/**
 * 订阅档位查询客户端，后续替换为 payment-service gRPC。
 */
public interface SubscriptionClient {

    SubscriptionTier getTier(long userId);
}
