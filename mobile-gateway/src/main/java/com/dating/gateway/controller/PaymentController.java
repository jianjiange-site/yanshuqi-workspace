package com.dating.gateway.controller;

import com.dating.gateway.common.Result;
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
import com.dating.gateway.resolver.CallerUserResolver;
import com.dating.gateway.service.PaymentBffService;
import com.dating.gateway.support.PaymentParamSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Payment REST 入口：JWT 鉴权后委派 PaymentBffService；prod/dev 下游未就绪时返回明确错误。
 */
@RestController
@RequestMapping("/api/v1/payment")
@Tag(name = "Payment", description = "商品、订单、金币、订阅、提现")
public class PaymentController {

    private final CallerUserResolver callerUserResolver;
    private final PaymentBffService paymentBffService;

    public PaymentController(CallerUserResolver callerUserResolver, PaymentBffService paymentBffService) {
        this.callerUserResolver = callerUserResolver;
        this.paymentBffService = paymentBffService;
    }

    @GetMapping("/products")
    @Operation(summary = "商品列表")
    public Result<List<ProductVO>> products(HttpServletRequest request) {
        long callerUserId = callerUserResolver.resolveCallerUserId(request);
        return Result.ok(paymentBffService.listProducts(callerUserId));
    }

    @PostMapping("/order")
    @Operation(summary = "创建支付订单")
    public Result<OrderVO> createOrder(HttpServletRequest request, @RequestBody CreateOrderReq req) {
        long callerUserId = callerUserResolver.resolveCallerUserId(request);
        return Result.ok(paymentBffService.createOrder(callerUserId, req));
    }

    @PostMapping("/verify")
    @Operation(summary = "支付验单")
    public Result<OrderVO> verify(HttpServletRequest request, @RequestBody VerifyPaymentReq req) {
        long callerUserId = callerUserResolver.resolveCallerUserId(request);
        return Result.ok(paymentBffService.verifyPayment(callerUserId, req));
    }

    @GetMapping("/coins")
    @Operation(summary = "查询金币余额")
    public Result<CoinsVO> coins(HttpServletRequest request) {
        long callerUserId = callerUserResolver.resolveCallerUserId(request);
        return Result.ok(paymentBffService.getCoins(callerUserId));
    }

    @GetMapping("/coins/ledger")
    @Operation(summary = "金币流水")
    public Result<List<CoinLedgerVO>> coinLedger(HttpServletRequest request,
                                                  @RequestParam(value = "page", defaultValue = "1") int page,
                                                  @RequestParam(value = "size", defaultValue = "20") int size) {
        long callerUserId = callerUserResolver.resolveCallerUserId(request);
        int safePage = PaymentParamSupport.clampPage(page);
        int safeSize = PaymentParamSupport.clampSize(size);
        return Result.ok(paymentBffService.listCoinLedger(callerUserId, safePage, safeSize));
    }

    @GetMapping("/balance")
    @Operation(summary = "钱包余额")
    public Result<BalanceVO> balance(HttpServletRequest request) {
        long callerUserId = callerUserResolver.resolveCallerUserId(request);
        return Result.ok(paymentBffService.getBalance(callerUserId));
    }

    @GetMapping("/subscription")
    @Operation(summary = "订阅状态")
    public Result<SubscriptionVO> subscription(HttpServletRequest request) {
        long callerUserId = callerUserResolver.resolveCallerUserId(request);
        return Result.ok(paymentBffService.getSubscription(callerUserId));
    }

    @PostMapping("/withdraw/bind")
    @Operation(summary = "绑定提现账户")
    public Result<String> bindWithdrawAccount(HttpServletRequest request, @RequestBody BindAccountReq req) {
        long callerUserId = callerUserResolver.resolveCallerUserId(request);
        return Result.ok(paymentBffService.bindWithdrawAccount(callerUserId, req));
    }

    @PostMapping("/withdraw")
    @Operation(summary = "发起提现")
    public Result<String> withdraw(HttpServletRequest request, @RequestBody WithdrawReq req) {
        long callerUserId = callerUserResolver.resolveCallerUserId(request);
        return Result.ok(paymentBffService.withdraw(callerUserId, req));
    }

    @GetMapping("/history")
    @Operation(summary = "支付/提现历史")
    public Result<List<HistoryVO>> history(HttpServletRequest request,
                                           @RequestParam(value = "page", defaultValue = "1") int page,
                                           @RequestParam(value = "size", defaultValue = "20") int size) {
        long callerUserId = callerUserResolver.resolveCallerUserId(request);
        int safePage = PaymentParamSupport.clampPage(page);
        int safeSize = PaymentParamSupport.clampSize(size);
        return Result.ok(paymentBffService.listHistory(callerUserId, safePage, safeSize));
    }
}
