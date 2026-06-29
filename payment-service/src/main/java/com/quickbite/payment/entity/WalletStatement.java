package com.quickbite.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_statements")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletStatement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long statementId;

    private Long customerId;

    private Double amount;

    @Enumerated(EnumType.STRING)
    private TransactionType type;   // CREDIT, DEBIT

    private String description;     // "Added money", "Paid for order #123", "Refund for order #124"

    private String referenceId;     // orderId or transactionId

    private LocalDateTime createdAt;

    public enum TransactionType {
        CREDIT, DEBIT
    }
}
