package com.quickbite.order.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    private String orderNumber;   // human-readable unique number

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private Long restaurantId;

    private Long deliveryAgentId;   // assigned later

    private Double totalAmount;
    private Double discount;
    private Double finalAmount;

    private String modeOfPayment;   // COD, WALLET, CARD, UPI

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    private LocalDateTime orderDate;

    private Integer estimatedDelivery;   // minutes

    private String deliveryAddress;
    private Double customerLatitude;
    private Double customerLongitude;

    private String specialInstructions;

    private LocalDateTime confirmedAt;
    private LocalDateTime preparingAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();

    public enum OrderStatus {
        PLACED, CONFIRMED, PREPARING, PICKED_UP, DELIVERED, CANCELLED
    }
}
