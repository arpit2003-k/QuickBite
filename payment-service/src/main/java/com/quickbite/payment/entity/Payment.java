package com.quickbite.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long customerId;

    private Double amount;

    @Enumerated(EnumType.STRING)
    private PaymentMode mode;   // COD, CARD, UPI, WALLET

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;   // PENDING, PAID, FAILED, REFUNDED

    private String transactionId;   // Unique ID from gateway or generated

    private String currency = "INR";

    private LocalDateTime paidAt;

    private LocalDateTime refundedAt;

    public enum PaymentMode {
        COD, CARD, UPI, WALLET, ONLINE
    }

    public enum PaymentStatus {
        PENDING, PAID, FAILED, REFUNDED
    }
}
