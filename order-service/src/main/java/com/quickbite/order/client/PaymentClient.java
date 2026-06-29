package com.quickbite.order.client;

import com.quickbite.order.dto.PaymentRequestDTO;
import com.quickbite.order.dto.PaymentResponseDTO;
import com.quickbite.order.dto.RefundRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service", url = "http://localhost:8086")
public interface PaymentClient {

    @PostMapping("/api/payments/process")
    PaymentResponseDTO processPayment(@RequestBody PaymentRequestDTO request);

    @PostMapping("/api/payments/refund")
    PaymentResponseDTO refundPayment(@RequestBody RefundRequestDTO request);
}
