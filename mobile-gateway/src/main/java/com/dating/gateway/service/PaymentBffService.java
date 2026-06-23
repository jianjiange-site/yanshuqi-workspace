package com.dating.gateway.service;

import com.dating.gateway.dto.BindAccountReq;
import com.dating.gateway.dto.CreateOrderReq;
import com.dating.gateway.dto.VerifyPaymentReq;
import com.dating.gateway.dto.WithdrawReq;
import com.dating.gateway.dto.vo.BalanceVO;
import com.dating.gateway.dto.vo.CoinLedgerVO;
import com.dating.gateway.dto.vo.CoinsVO;
import com.dating.gateway.dto.vo.HistoryVO;
import com.dating.gateway.dto.vo.OrderVO;
import com.dating.gateway.dto.vo.ProductVO;
import com.dating.gateway.dto.vo.SubscriptionVO;

import java.util.List;

/**
 * Payment BFF：REST 编排，真实支付逻辑待 payment-service proto 就绪后接入。
 */
public interface PaymentBffService {

    List<ProductVO> listProducts(long callerUserId);

    OrderVO createOrder(long callerUserId, CreateOrderReq req);

    OrderVO verifyPayment(long callerUserId, VerifyPaymentReq req);

    CoinsVO getCoins(long callerUserId);

    List<CoinLedgerVO> listCoinLedger(long callerUserId, int page, int size);

    BalanceVO getBalance(long callerUserId);

    SubscriptionVO getSubscription(long callerUserId);

    String bindWithdrawAccount(long callerUserId, BindAccountReq req);

    String withdraw(long callerUserId, WithdrawReq req);

    List<HistoryVO> listHistory(long callerUserId, int page, int size);
}
