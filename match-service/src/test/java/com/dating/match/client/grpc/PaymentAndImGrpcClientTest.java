package com.dating.match.client.grpc;

import com.dating.match.client.ImClient;
import com.dating.match.client.PaymentClient;
import com.dating.match.exception.MatchBizException;
import com.dating.match.exception.MatchErrorCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentAndImGrpcClientTest {

    @Test
    void paymentGrpcClient_shouldThrowNotImplemented() {
        PaymentClient client = new PaymentGrpcClient();
        MatchBizException ex = assertThrows(MatchBizException.class,
                () -> client.consumeCoins(10001L, 100, "key-1"));
        assertEquals(MatchErrorCode.EXTERNAL_RPC_NOT_IMPLEMENTED, ex.getErrorCode());
    }

    @Test
    void subscriptionGrpcClient_shouldThrowNotImplemented() {
        SubscriptionGrpcClient client = new SubscriptionGrpcClient();
        MatchBizException ex = assertThrows(MatchBizException.class,
                () -> client.getTier(10001L));
        assertEquals(MatchErrorCode.EXTERNAL_RPC_NOT_IMPLEMENTED, ex.getErrorCode());
    }

    @Test
    void imGrpcClient_shouldFailExplicitly() {
        ImClient client = new ImGrpcClient();
        assertFalse(client.execute("ENSURE_CONVERSATION", "{}"));
    }
}
