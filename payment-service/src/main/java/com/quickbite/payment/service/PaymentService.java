package com.quickbite.payment.service;

import com.quickbite.payment.dto.PaymentRequest;
import com.quickbite.payment.dto.PaymentResponse;
import com.quickbite.payment.dto.RazorpayOrderResponse;

public interface PaymentService {
    PaymentResponse processPayment(PaymentRequest request);
    PaymentResponse refundPayment(Long orderId, Long customerId, Double amount);
    PaymentResponse getPaymentByOrderId(Long orderId);
    
    // Razorpay methods
    RazorpayOrderResponse createRazorpayOrder(Double amount, Long customerId);
    boolean verifySignature(String orderId, String paymentId, String signature);
}
