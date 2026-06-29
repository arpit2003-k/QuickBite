package com.quickbite.order.controller;

import com.quickbite.order.dto.OrderRequest;
import com.quickbite.order.dto.OrderResponse;
import com.quickbite.order.dto.OrderStatusUpdateDTO;
import com.quickbite.order.dto.OrderTrackingResponse;
import com.quickbite.order.service.OrderService;
import com.quickbite.order.util.RoleValidator;
import com.quickbite.order.exception.CustomException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Slf4j
@Tag(name = "Order", description = "Place, track, cancel, reorder")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    @Operation(summary = "Place a new order (uses cart and payment)")
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody OrderRequest request,
                                                    @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId != null) request.setCustomerId(userId);
        return ResponseEntity.ok(orderService.placeOrder(request));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    @GetMapping("/{orderId}/tracking")
    @Operation(summary = "Get live tracking data for an order")
    public ResponseEntity<OrderTrackingResponse> getOrderTracking(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderTracking(orderId));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get all orders of a customer")
    public ResponseEntity<List<OrderResponse>> getCustomerOrders(@PathVariable Long customerId) {
        return ResponseEntity.ok(orderService.getOrdersByCustomer(customerId));
    }

    @GetMapping("/restaurant/{restaurantId}")
    @Operation(summary = "Get all orders for a restaurant")
    public ResponseEntity<List<OrderResponse>> getRestaurantOrders(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(orderService.getOrdersByRestaurant(restaurantId));
    }

    @GetMapping("/agent/{agentId}")
    @Operation(summary = "Get all orders assigned to a delivery agent")
    public ResponseEntity<List<OrderResponse>> getAgentOrders(@PathVariable Long agentId) {
        return ResponseEntity.ok(orderService.getOrdersByDeliveryAgent(agentId));
    }

    @PutMapping("/status")
    @Operation(summary = "Update order status (restaurant owner or admin)")
    public ResponseEntity<OrderResponse> updateStatus(@Valid @RequestBody OrderStatusUpdateDTO request,
                                                      @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        // Only restaurant owners or admin can update status
		/*
		 * if (!"RESTAURANT_OWNER".equals(userRole) && !"ADMIN".equals(userRole)) {
		 * throw new
		 * CustomException("Access denied. Required role: RESTAURANT_OWNER or ADMIN"); }
		 */
        return ResponseEntity.ok(orderService.updateOrderStatus(request));
    }

    @DeleteMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel order (customer)")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long orderId, 
                                                     @RequestParam Long customerId,
                                                     @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                                     @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        if (!"ADMIN".equals(userRole)) {
            RoleValidator.checkUserId(customerId, userId);
        }
        return ResponseEntity.ok(orderService.cancelOrder(orderId, customerId));
    }

    @PostMapping("/reorder")
    @Operation(summary = "Reorder from past order")
    public ResponseEntity<OrderResponse> reorder(@RequestParam Long previousOrderId,
                                                  @RequestParam Long customerId,
                                                  @RequestParam String paymentMode,
                                                  @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                                  @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        if (!"ADMIN".equals(userRole)) {
            RoleValidator.checkUserId(customerId, userId);
        }
        return ResponseEntity.ok(orderService.reorder(previousOrderId, customerId, paymentMode));
    }
}
