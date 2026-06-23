package com.dating.gateway.service.impl;

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
import com.dating.gateway.exception.GatewayBizException;
import com.dating.gateway.exception.GatewayErrorCode;
import com.dating.gateway.service.PaymentBffService;
import com.dating.gateway.support.GatewayFeatureNotReadySupport;
import com.dating.gateway.support.PaymentParamSupport;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Payment BFF 生产实现：payment-service 尚无正式 proto，prod/dev 统一返回 not ready。
 */
@Service
@Profile("!mock & !test")
public class PaymentBffServiceImpl implements PaymentBffService {

    @Override
    public List<ProductVO> listProducts(long callerUserId) {
        throw GatewayFeatureNotReadySupport.paymentNotReady();
    }

    @Override
    public OrderVO createOrder(long callerUserId, CreateOrderReq req) {
        validateCreateOrder(req);
        throw GatewayFeatureNotReadySupport.paymentNotReady();
    }

    @Override
    public OrderVO verifyPayment(long callerUserId, VerifyPaymentReq req) {
        validateVerifyPayment(req);
        throw GatewayFeatureNotReadySupport.paymentNotReady();
    }

    @Override
    public CoinsVO getCoins(long callerUserId) {
        throw GatewayFeatureNotReadySupport.paymentNotReady();
    }

    @Override
    public List<CoinLedgerVO> listCoinLedger(long callerUserId, int page, int size) {
        PaymentParamSupport.clampPage(page);
        PaymentParamSupport.clampSize(size);
        throw GatewayFeatureNotReadySupport.paymentNotReady();
    }

    @Override
    public BalanceVO getBalance(long callerUserId) {
        throw GatewayFeatureNotReadySupport.paymentNotReady();
    }

    @Override
    public SubscriptionVO getSubscription(long callerUserId) {
        throw GatewayFeatureNotReadySupport.paymentNotReady();
    }

    @Override
    public String bindWithdrawAccount(long callerUserId, BindAccountReq req) {
        validateBindAccount(req);
        throw GatewayFeatureNotReadySupport.paymentNotReady();
    }

    @Override
    public String withdraw(long callerUserId, WithdrawReq req) {
        validateWithdraw(req);
        throw GatewayFeatureNotReadySupport.paymentNotReady();
    }

    @Override
    public List<HistoryVO> listHistory(long callerUserId, int page, int size) {
        PaymentParamSupport.clampPage(page);
        PaymentParamSupport.clampSize(size);
        throw GatewayFeatureNotReadySupport.paymentNotReady();
    }

    private void validateCreateOrder(CreateOrderReq req) {
        PaymentParamSupport.requireText(req.getProductId(), "productId");
        PaymentParamSupport.requireText(req.getPaymentMethod(), "paymentMethod");
        PaymentParamSupport.requireText(req.getCurrency(), "currency");
        if (req.getPlatform() == null) {
            throw new GatewayBizException(GatewayErrorCode.INVALID_ARGUMENT, "platform 不能为空");
        }
    }

    private void validateVerifyPayment(VerifyPaymentReq req) {
        PaymentParamSupport.requireText(req.getOrderId(), "orderId");
        PaymentParamSupport.requireText(req.getReceiptData(), "receiptData");
    }

    private void validateBindAccount(BindAccountReq req) {
        PaymentParamSupport.requireText(req.getType(), "type");
        PaymentParamSupport.requireText(req.getAccountIdentifier(), "accountIdentifier");
        PaymentParamSupport.requireText(req.getHolderName(), "holderName");
    }

    private void validateWithdraw(WithdrawReq req) {
        PaymentParamSupport.requireText(req.getAccountId(), "accountId");
        PaymentParamSupport.requireText(req.getIdempotencyKey(), "idempotencyKey");
    }
}
