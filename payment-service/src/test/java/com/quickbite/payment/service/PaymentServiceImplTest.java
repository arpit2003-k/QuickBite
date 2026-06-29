package com.quickbite.payment.service;

import com.quickbite.payment.dto.*;
import com.quickbite.payment.entity.Payment;
import com.quickbite.payment.entity.Wallet;
import com.quickbite.payment.exception.CustomException;
import com.quickbite.payment.repository.PaymentRepository;
import com.quickbite.payment.repository.WalletRepository;
import com.razorpay.Order;
import com.razorpay.OrderClient;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private WalletRepository walletRepository;
    @Mock private WalletService walletService;
    @Mock private RazorpayClient razorpayClient;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private PaymentRequest paymentRequest;
    private Payment mockPayment;
    private Wallet mockWallet;

    @BeforeEach
    void setUp() {
        paymentRequest = new PaymentRequest();
        paymentRequest.setOrderId(10L);
        paymentRequest.setCustomerId(1L);
        paymentRequest.setAmount(100.0);
        paymentRequest.setMode("COD");

        mockPayment = new Payment();
        mockPayment.setPaymentId(100L);
        mockPayment.setOrderId(10L);
        mockPayment.setStatus(Payment.PaymentStatus.PAID);
        mockPayment.setTransactionId("TXN123");

        mockWallet = new Wallet();
        mockWallet.setCustomerId(1L);
        mockWallet.setBalance(500.0);

        ReflectionTestUtils.setField(paymentService, "keyId", "testKey");
    }

    @Test
    void processPayment_CODSuccess_ReturnsResponse() {
        when(paymentRepository.findByOrderId(10L)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenReturn(mockPayment);

        PaymentResponse response = paymentService.processPayment(paymentRequest);

        assertEquals("PAID", response.getStatus());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void processPayment_DuplicateOrder_ThrowsCustomException() {
        when(paymentRepository.findByOrderId(10L)).thenReturn(Optional.of(mockPayment));

        assertThrows(CustomException.class, () -> paymentService.processPayment(paymentRequest));
    }

    @Test
    void processPayment_WalletSuccess_ReturnsResponse() {
        paymentRequest.setMode("WALLET");
        when(paymentRepository.findByOrderId(10L)).thenReturn(Optional.empty());
        when(walletRepository.findByCustomerId(1L)).thenReturn(Optional.of(mockWallet));
        when(paymentRepository.save(any(Payment.class))).thenReturn(mockPayment);

        PaymentResponse response = paymentService.processPayment(paymentRequest);

        assertEquals("PAID", response.getStatus());
        verify(walletService).deductFromWallet(eq(1L), eq(100.0), anyString(), anyString());
    }

    @Test
    void processPayment_InsufficientWallet_ThrowsCustomException() {
        paymentRequest.setMode("WALLET");
        mockWallet.setBalance(50.0);
        when(paymentRepository.findByOrderId(10L)).thenReturn(Optional.empty());
        when(walletRepository.findByCustomerId(1L)).thenReturn(Optional.of(mockWallet));

        assertThrows(CustomException.class, () -> paymentService.processPayment(paymentRequest));
    }

    @Test
    void refundPayment_Success_ReturnsResponse() {
        when(paymentRepository.findByOrderId(10L)).thenReturn(Optional.of(mockPayment));

        PaymentResponse response = paymentService.refundPayment(10L, 1L, 100.0);

        assertEquals("REFUNDED", response.getStatus());
        verify(walletService).addToWallet(eq(1L), eq(100.0), anyString(), anyString());
    }

    @Test
    void refundPayment_AlreadyRefunded_ThrowsCustomException() {
        mockPayment.setStatus(Payment.PaymentStatus.REFUNDED);
        when(paymentRepository.findByOrderId(10L)).thenReturn(Optional.of(mockPayment));

        assertThrows(CustomException.class, () -> paymentService.refundPayment(10L, 1L, 100.0));
    }

    @Test
    void createRazorpayOrder_Success_ReturnsOrder() throws Exception {
        OrderClient mockOrderClient = mock(OrderClient.class);
        razorpayClient.orders = mockOrderClient;
        Order mockRazorpayOrder = mock(Order.class);
        
        when(mockOrderClient.create(any(JSONObject.class))).thenReturn(mockRazorpayOrder);
        when(mockRazorpayOrder.get("id")).thenReturn("order_123");
        when(mockRazorpayOrder.get("amount")).thenReturn(10000);

        RazorpayOrderResponse response = paymentService.createRazorpayOrder(100.0, 1L);

        assertEquals("order_123", response.getRazorpayOrderId());
        assertEquals("testKey", response.getKeyId());
    }
}
