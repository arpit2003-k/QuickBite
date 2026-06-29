package com.quickbite.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderTrackingResponse {
    private Long orderId;
    private String orderNumber;
    private String orderStatus;
    private TrackingLocationPoint restaurant;
    private TrackingLocationPoint customer;
    private TrackingLocationPoint deliveryAgent;
    private TrackingDistanceInfo restaurantToCustomer;
    private TrackingDistanceInfo agentToRestaurant;
    private TrackingDistanceInfo agentToCustomer;
}
