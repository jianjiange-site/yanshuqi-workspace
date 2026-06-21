package com.dating.match.grpc;



import com.dating.match.grpc.proto.GetQuotaReq;

import com.dating.match.grpc.proto.GetQuotaResp;

import com.dating.match.service.QuotaService;
import com.dating.match.service.SwipeService;

import io.grpc.stub.StreamObserver;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;

import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;



import java.util.concurrent.atomic.AtomicReference;



import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.Mockito.when;



@ExtendWith(MockitoExtension.class)

class MatchGrpcServiceQuotaTest {



    @Mock

    private SwipeService swipeService;



    @Mock

    private QuotaService quotaService;



    @InjectMocks

    private MatchGrpcService matchGrpcService;



    @Test

    void getQuota_shouldReturnQuotaServiceResponse() {

        when(quotaService.buildQuotaResponse(10001L)).thenReturn(

                GetQuotaResp.newBuilder()

                        .setTier("FREE")

                        .setDailyRightSwipeLimit(5)

                        .setDailyRightSwipeUsed(0)

                        .setDailyCardLimit(50)

                        .setDailyCardUsed(0)

                        .setDailySuperHiLimit(0)

                        .setDailySuperHiUsed(0)

                        .setSuperHiCoinPrice(100)

                        .build());



        AtomicReference<GetQuotaResp> captured = new AtomicReference<>();

        matchGrpcService.getQuota(GetQuotaReq.newBuilder().setCallerUserId(10001L).build(),

                new StreamObserver<>() {

                    @Override

                    public void onNext(GetQuotaResp value) {

                        captured.set(value);

                    }



                    @Override

                    public void onError(Throwable t) {

                    }



                    @Override

                    public void onCompleted() {

                    }

                });



        GetQuotaResp resp = captured.get();

        assertEquals("FREE", resp.getTier());

        assertEquals(5, resp.getDailyRightSwipeLimit());

        assertEquals(50, resp.getDailyCardLimit());

        assertEquals(0, resp.getDailySuperHiLimit());

        assertEquals(100, resp.getSuperHiCoinPrice());

    }

}


