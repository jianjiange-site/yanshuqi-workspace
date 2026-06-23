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
import com.dating.gateway.support.PaymentParamSupport;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Payment BFF mock 实现：仅 mock/test profile 启用，禁止用于 prod。
 */
@Service
@Profile({"mock", "test"})
public class MockPaymentBffServiceImpl implements PaymentBffService {

    @Override
    public List<ProductVO> listProducts(long callerUserId) {
        List<ProductVO> products = new ArrayList<>(2);
        products.add(buildProduct("coin_pack_100", "100 Coins Pack", "COIN", 999L, "USD"));
        products.add(buildProduct("premium_monthly", "Premium Monthly", "SUBSCRIPTION", 999L, "USD"));
        return products;
    }

    @Override
    public OrderVO createOrder(long callerUserId, CreateOrderReq req) {
        validateCreateOrder(req);
        OrderVO vo = new OrderVO();
        vo.setOrderId("mock-order-" + UUID.randomUUID());
        vo.setStatus("PENDING");
        vo.setProductId(req.getProductId());
        return vo;
    }

    @Override
    public OrderVO verifyPayment(long callerUserId, VerifyPaymentReq req) {
        validateVerifyPayment(req);
        OrderVO vo = new OrderVO();
        vo.setOrderId(req.getOrderId());
        vo.setStatus("VERIFIED");
        return vo;
    }

    @Override
    public CoinsVO getCoins(long callerUserId) {
        CoinsVO vo = new CoinsVO();
        vo.setBalance(100L);
        return vo;
    }

    @Override
    public List<CoinLedgerVO> listCoinLedger(long callerUserId, int page, int size) {
        int safePage = PaymentParamSupport.clampPage(page);
        int safeSize = PaymentParamSupport.clampSize(size);
        if (safePage > 1) {
            return List.of();
        }
        CoinLedgerVO entry = new CoinLedgerVO();
        entry.setLedgerId("mock-ledger-1");
        entry.setChangeAmount(100L);
        entry.setBalanceAfter(100L);
        entry.setReason("MOCK_REGISTER_REWARD");
        entry.setCreatedAt(System.currentTimeMillis());
        return List.of(entry);
    }

    @Override
    public BalanceVO getBalance(long callerUserId) {
        BalanceVO vo = new BalanceVO();
        vo.setCurrency("USD");
        vo.setAvailableBalanceCent(0L);
        vo.setFrozenBalanceCent(0L);
        return vo;
    }

    @Override
    public SubscriptionVO getSubscription(long callerUserId) {
        SubscriptionVO vo = new SubscriptionVO();
        vo.setStatus("FREE");
        vo.setTier("NONE");
        return vo;
    }

    @Override
    public String bindWithdrawAccount(long callerUserId, BindAccountReq req) {
        validateBindAccount(req);
        return "mock-account-" + callerUserId;
    }

    @Override
    public String withdraw(long callerUserId, WithdrawReq req) {
        validateWithdraw(req);
        return "mock-withdraw-" + UUID.randomUUID();
    }

    @Override
    public List<HistoryVO> listHistory(long callerUserId, int page, int size) {
        PaymentParamSupport.clampPage(page);
        PaymentParamSupport.clampSize(size);
        return List.of();
    }

    private ProductVO buildProduct(String productId, String name, String type, long priceCent, String currency) {
        ProductVO vo = new ProductVO();
        vo.setProductId(productId);
        vo.setName(name);
        vo.setProductType(type);
        vo.setPriceCent(priceCent);
        vo.setCurrency(currency);
        return vo;
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
