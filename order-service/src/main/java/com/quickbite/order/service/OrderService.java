package com.quickbite.order.service;

import com.quickbite.order.dto.OrderRequest;
import com.quickbite.order.dto.OrderResponse;
import com.quickbite.order.dto.OrderStatusUpdateDTO;
import com.quickbite.order.dto.OrderTrackingResponse;

import java.util.List;

public interface OrderService {
    OrderResponse placeOrder(OrderRequest request);
    OrderResponse getOrderById(Long orderId);
    List<OrderResponse> getOrdersByCustomer(Long customerId);
    List<OrderResponse> getOrdersByRestaurant(Long restaurantId);
    List<OrderResponse> getOrdersByDeliveryAgent(Long agentId);
    OrderTrackingResponse getOrderTracking(Long orderId);
    OrderResponse updateOrderStatus(OrderStatusUpdateDTO request);
    OrderResponse cancelOrder(Long orderId, Long customerId);
    OrderResponse reorder(Long previousOrderId, Long customerId, String paymentMode);
}
