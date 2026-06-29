package com.quickbite.payment.service;

import com.quickbite.payment.dto.PaymentRequest;
import com.quickbite.payment.dto.PaymentResponse;
import com.quickbite.payment.dto.RazorpayOrderResponse;
import com.quickbite.payment.entity.Payment;
import com.quickbite.payment.entity.Wallet;
import com.quickbite.payment.exception.CustomException;
import com.quickbite.payment.repository.PaymentRepository;
import com.quickbite.payment.repository.WalletRepository;
import com.quickbite.payment.util.TransactionIdGenerator;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletService walletService;

    @Autowired
    private RazorpayClient razorpayClient;

    @Value("${razorpay.key-id}")
    private String keyId;

    @Value("${razorpay.key-secret}")
    private String keySecret;

    @Override
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment for orderId: {}, customerId: {}, amount: {}, mode: {}",
                request.getOrderId(), request.getCustomerId(), request.getAmount(), request.getMode());

        // Check if payment already exists for this order
        if (paymentRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new CustomException("Payment already processed for this order");
        }

        Payment.PaymentMode mode;
        try {
            mode = Payment.PaymentMode.valueOf(request.getMode().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException("Invalid payment mode. Allowed: COD, CARD, UPI, WALLET, ONLINE");
        }

        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setCustomerId(request.getCustomerId());
        payment.setAmount(request.getAmount());
        payment.setMode(mode);
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setTransactionId(TransactionIdGenerator.generate());

        // Handle payment based on mode
        if (mode == Payment.PaymentMode.COD) {
            // COD: payment successful when order is delivered (for now, just mark as PAID)
            payment.setStatus(Payment.PaymentStatus.PAID);
            payment.setPaidAt(LocalDateTime.now());
            log.info("COD payment recorded for orderId: {}", request.getOrderId());
        } 
        else if (mode == Payment.PaymentMode.WALLET) {
            // Deduct from wallet
            Wallet wallet = walletRepository.findByCustomerId(request.getCustomerId())
                    .orElseThrow(() -> new CustomException("Wallet not found for customer"));
            if (wallet.getBalance() < request.getAmount()) {
                throw new CustomException("Insufficient wallet balance");
            }
            // Perform wallet debit
            walletService.deductFromWallet(request.getCustomerId(), request.getAmount(), 
                    "Payment for order #" + request.getOrderId(), String.valueOf(request.getOrderId()));
            payment.setStatus(Payment.PaymentStatus.PAID);
            payment.setPaidAt(LocalDateTime.now());
            log.info("Wallet payment successful for orderId: {}", request.getOrderId());
        }
        else if (mode == Payment.PaymentMode.ONLINE) {
            // Razorpay logic (CARD or UPI)
            payment.setStatus(Payment.PaymentStatus.PAID);
            payment.setPaidAt(LocalDateTime.now());
            log.info("Online payment (Razorpay) recorded for orderId: {}", request.getOrderId());
        }
        else {
            // Fallback for other modes like CARD, UPI if passed directly
            payment.setStatus(Payment.PaymentStatus.PAID);
            payment.setPaidAt(LocalDateTime.now());
            log.info("Direct {} payment recorded for orderId: {}", mode, request.getOrderId());
        }

        Payment saved = paymentRepository.save(payment);
        return new PaymentResponse(saved.getPaymentId(), saved.getStatus().name(), 
                "Payment successful", saved.getTransactionId());
    }

    @Override
    @Transactional
    public PaymentResponse refundPayment(Long orderId, Long customerId, Double amount) {
        log.info("Processing refund for orderId: {}, customerId: {}, amount: {}", orderId, customerId, amount);

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException("Payment not found for this order"));

        if (payment.getStatus() == Payment.PaymentStatus.REFUNDED) {
            throw new CustomException("Payment already refunded");
        }

        // Update payment status
        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        payment.setRefundedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // Refund to wallet (or original mode - simplified to wallet)
        walletService.addToWallet(customerId, amount, 
                "Refund for order #" + orderId, String.valueOf(orderId));

        return new PaymentResponse(payment.getPaymentId(), "REFUNDED", 
                "Refund processed to wallet", payment.getTransactionId());
    }

    @Override
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException("Payment not found for orderId: " + orderId));
        return new PaymentResponse(payment.getPaymentId(), payment.getStatus().name(), 
                "Payment details", payment.getTransactionId());
    }

    @Override
    public RazorpayOrderResponse createRazorpayOrder(Double amount, Long customerId) {
        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", (int) (amount * 100)); // Amount in paise
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "receipt_" + customerId + "_" + System.currentTimeMillis());

            Order order = razorpayClient.orders.create(orderRequest);
            return new RazorpayOrderResponse(order.get("id"), "INR", order.get("amount"), keyId);
        } catch (RazorpayException e) {
            log.error("Razorpay order creation failed: {}", e.getMessage());
            throw new CustomException("Failed to create Razorpay order: " + e.getMessage());
        }
    }

    @Override
    public boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", orderId);
            options.put("razorpay_payment_id", paymentId);
            options.put("razorpay_signature", signature);

            return Utils.verifyPaymentSignature(options, keySecret);
        } catch (Exception e) {
            log.error("Signature verification failed: {}", e.getMessage());
            return false;
        }
    }
}
