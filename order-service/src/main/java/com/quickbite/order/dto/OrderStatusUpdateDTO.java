package com.quickbite.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatusUpdateDTO {
    @NotNull private Long orderId;
    @NotNull private String status;   // CONFIRMED, PREPARING, PICKED_UP, DELIVERED, CANCELLED
}
