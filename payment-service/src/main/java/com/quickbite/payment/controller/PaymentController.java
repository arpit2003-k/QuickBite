package com.quickbite.payment.controller;

import com.quickbite.payment.dto.*;
import com.quickbite.payment.entity.WalletStatement;
import com.quickbite.payment.service.PaymentService;
import com.quickbite.payment.service.WalletService;
import com.quickbite.payment.util.RoleValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@Slf4j
@Tag(name = "Payment & Wallet", description = "Process payments, manage wallet, refunds")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private WalletService walletService;

    // Payment endpoints
    @PostMapping("/process")
    @Operation(summary = "Process payment for an order")
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request,
                                                          @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId != null) request.setCustomerId(userId);
        return ResponseEntity.ok(paymentService.processPayment(request));
    }

    @PostMapping("/refund")
    @Operation(summary = "Refund payment for cancelled order")
    public ResponseEntity<PaymentResponse> refundPayment(@RequestBody RefundRequestDTO request) {
        return ResponseEntity.ok(paymentService.refundPayment(request.getOrderId(), 
                                                              request.getCustomerId(), 
                                                              request.getAmount()));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payment details by order ID")
    public ResponseEntity<PaymentResponse> getPaymentByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
    }

    // Wallet endpoints
    @PostMapping("/wallet/add")
    @Operation(summary = "Add money to wallet")
    public ResponseEntity<WalletBalanceResponse> addMoney(@Valid @RequestBody AddMoneyRequest request,
                                                          @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId != null) request.setCustomerId(userId);
        return ResponseEntity.ok(walletService.addMoney(request));
    }

    @GetMapping("/wallet/balance")
    @Operation(summary = "Get wallet balance")
    public ResponseEntity<WalletBalanceResponse> getBalance(@RequestParam Long customerId,
                                                            @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                                            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        if (!"ADMIN".equals(userRole)) {
            RoleValidator.checkUserId(customerId, userId);
        }
        return ResponseEntity.ok(walletService.getBalance(customerId));
    }

    @GetMapping("/wallet/statement")
    @Operation(summary = "Get wallet transaction statement")
    public ResponseEntity<List<WalletStatement>> getStatement(@RequestParam Long customerId,
                                                              @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                                              @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        if (!"ADMIN".equals(userRole)) {
            RoleValidator.checkUserId(customerId, userId);
        }
        return ResponseEntity.ok(walletService.getStatement(customerId));
    }

    // Razorpay Integration Endpoints
    @PostMapping("/razorpay/create-order")
    @Operation(summary = "Create Razorpay Order for online payment")
    public ResponseEntity<RazorpayOrderResponse> createRazorpayOrder(@RequestParam Double amount,
                                                                      @RequestHeader(value = "X-User-Id") Long userId) {
        return ResponseEntity.ok(paymentService.createRazorpayOrder(amount, userId));
    }

    @PostMapping("/razorpay/verify")
    @Operation(summary = "Verify Razorpay Payment Signature")
    public ResponseEntity<Boolean> verifyRazorpaySignature(@RequestParam String orderId,
                                                            @RequestParam String paymentId,
                                                            @RequestParam String signature) {
        return ResponseEntity.ok(paymentService.verifySignature(orderId, paymentId, signature));
    }
}

