package com.quickbite.order.service;

import com.quickbite.order.client.CartClient;
import com.quickbite.order.client.DeliveryClient;
import com.quickbite.order.client.PaymentClient;
import com.quickbite.order.dto.*;
import com.quickbite.order.entity.Order;
import com.quickbite.order.entity.OrderItem;
import com.quickbite.order.exception.CustomException;
import com.quickbite.order.repository.OrderRepository;
import com.quickbite.order.util.OrderNumberGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.quickbite.order.config.RabbitMQConfig;
import com.quickbite.order.client.AuthClient;
import com.quickbite.order.dto.OrderEventDTO;
import com.quickbite.order.dto.UserDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    private static final Pattern GPS_PATTERN = Pattern.compile("\\[GPS:\\s*(-?\\d+(?:\\.\\d+)?)\\s*,\\s*(-?\\d+(?:\\.\\d+)?)\\]");
    private static final double AVERAGE_CITY_SPEED_KMPH = 25.0;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartClient cartClient;

    @Autowired
    private PaymentClient paymentClient;

    @Autowired
    private DeliveryClient deliveryClient;
    
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AuthClient authClient;

    @Autowired
    private com.quickbite.order.client.RestaurantClient restaurantClient;

    // Helper: convert Order entity to OrderResponse
    private OrderResponse toResponse(Order order) {
        List<OrderItemDTO> itemDTOs = order.getItems().stream()
                .map(item -> new OrderItemDTO(item.getName(), item.getPrice(), item.getQuantity(), item.getCustomization()))
                .collect(Collectors.toList());
        return new OrderResponse(
                order.getOrderId(),
                order.getOrderNumber(),
                order.getCustomerId(),
                order.getRestaurantId(),
                order.getDeliveryAgentId(),
                order.getTotalAmount(),
                order.getDiscount(),
                order.getFinalAmount(),
                order.getModeOfPayment(),
                order.getOrderStatus().name(),
                order.getOrderDate(),
                order.getDeliveryAddress(),
                order.getCustomerLatitude(),
                order.getCustomerLongitude(),
                itemDTOs
        );
    }

    @Override
    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {
    	
        log.info("Placing order for customer: {}", request.getCustomerId());

        // 1. Fetch cart from Cart Service
        CartDTO cart;
        try {
            cart = cartClient.getCart(request.getCustomerId());
        } catch (Exception e) {
            throw new CustomException("Unable to fetch cart. Please try again.");
        }

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new CustomException("Cart is empty. Add items before placing order.");
        }

        Order order = new Order();
        order.setOrderNumber(OrderNumberGenerator.generate());
        order.setCustomerId(request.getCustomerId());
        order.setRestaurantId(cart.getRestaurantId());
        order.setTotalAmount(cart.getTotalPrice());
        order.setDiscount(0.0);
        order.setFinalAmount(cart.getTotalPrice());
        order.setModeOfPayment(request.getPaymentMode());
        order.setOrderStatus(Order.OrderStatus.PLACED);
        order.setOrderDate(LocalDateTime.now());
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setCustomerLatitude(request.getCustomerLatitude());
        order.setCustomerLongitude(request.getCustomerLongitude());
        order.setSpecialInstructions(request.getSpecialInstructions());

        // Snapshot cart items into order items
        for (CartItemDTO cartItem : cart.getItems()) {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setMenuItemId(cartItem.getMenuItemId());
            item.setName(cartItem.getName());
            item.setPrice(cartItem.getPrice());
            item.setQuantity(cartItem.getQuantity());
            item.setCustomization(cartItem.getCustomization());
            order.getItems().add(item);
        }

        Order savedOrder = orderRepository.save(order);

        // Process payment
        PaymentRequestDTO finalPaymentReq = new PaymentRequestDTO();
        finalPaymentReq.setOrderId(savedOrder.getOrderId());
        finalPaymentReq.setCustomerId(request.getCustomerId());
        finalPaymentReq.setAmount(savedOrder.getFinalAmount());
        finalPaymentReq.setMode(request.getPaymentMode());

        try {
            PaymentResponseDTO paymentResp = paymentClient.processPayment(finalPaymentReq);
            if (!"PAID".equals(paymentResp.getStatus()) && !"COD".equals(request.getPaymentMode())) {
                // For non-COD, payment must be PAID
                throw new CustomException("Payment failed: " + paymentResp.getMessage());
            }
            log.info("Payment processed: {}", paymentResp);
        } catch (Exception e) {
            // Payment failed – cancel order
            order.setOrderStatus(Order.OrderStatus.CANCELLED);
            order.setCancelledAt(LocalDateTime.now());
            orderRepository.save(order);
            throw new CustomException("Payment failed: " + e.getMessage());
        }

        // Clear cart after successful order
        try {
            cartClient.clearCart(request.getCustomerId());
        } catch (Exception e) {
            log.warn("Failed to clear cart after order: {}", e.getMessage());
        }

        // Assign delivery agent (optional, can be async)
        try {
            com.quickbite.order.dto.RestaurantDTO restaurant = restaurantClient.getRestaurantById(savedOrder.getRestaurantId());
            
            DeliveryAssignmentDTO assignReq = new DeliveryAssignmentDTO();
            assignReq.setOrderId(savedOrder.getOrderId());
            assignReq.setRestaurantId(savedOrder.getRestaurantId());
            assignReq.setRestaurantLat(restaurant.getLatitude());
            assignReq.setRestaurantLng(restaurant.getLongitude());
            assignReq.setCustomerId(savedOrder.getCustomerId());
            assignReq.setDeliveryAddress(savedOrder.getDeliveryAddress());
            assignReq.setPickupAddress(restaurant.getAddress());

            Long agentId = deliveryClient.assignDeliveryAgent(assignReq);
            savedOrder.setDeliveryAgentId(agentId);
            orderRepository.save(savedOrder);
            log.info("Delivery agent assigned: {}", agentId);
        } catch (Exception e) {
            log.warn("Delivery assignment failed (order still placed): {}", e.getMessage());
        }

        log.info("Order placed successfully: {}", savedOrder.getOrderNumber());
     // ✅ Publish event (using the existing savedOrder variable)
        publishOrderEvent(savedOrder, "PLACED");
        return toResponse(savedOrder);
    }

    @Override
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException("Order not found"));
        return toResponse(order);
    }

    @Override
    public List<OrderResponse> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerId(customerId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getOrdersByRestaurant(Long restaurantId) {
        return orderRepository.findByRestaurantId(restaurantId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getOrdersByDeliveryAgent(Long agentId) {
        return orderRepository.findByDeliveryAgentId(agentId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public OrderTrackingResponse getOrderTracking(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException("Order not found"));

        RestaurantDTO restaurant = restaurantClient.getRestaurantById(order.getRestaurantId());
        TrackingLocationPoint restaurantPoint = new TrackingLocationPoint(
                restaurant.getName(),
                restaurant.getLatitude(),
                restaurant.getLongitude()
        );

        CoordinatePair customerCoordinates = resolveCustomerCoordinates(order);
        TrackingLocationPoint customerPoint = new TrackingLocationPoint(
                "Customer",
                customerCoordinates.latitude(),
                customerCoordinates.longitude()
        );

        TrackingLocationPoint deliveryAgentPoint = null;
        TrackingDistanceInfo agentToRestaurant = null;
        TrackingDistanceInfo agentToCustomer = null;

        if (order.getDeliveryAgentId() != null) {
            try {
                AgentResponse agent = deliveryClient.getAgentById(order.getDeliveryAgentId());
                if (agent.getCurrentLatitude() != null && agent.getCurrentLongitude() != null) {
                    deliveryAgentPoint = new TrackingLocationPoint(
                            agent.getFullName(),
                            agent.getCurrentLatitude(),
                            agent.getCurrentLongitude()
                    );
                    agentToRestaurant = buildDistanceInfo(
                            agent.getCurrentLatitude(),
                            agent.getCurrentLongitude(),
                            restaurant.getLatitude(),
                            restaurant.getLongitude()
                    );
                    agentToCustomer = buildDistanceInfo(
                            agent.getCurrentLatitude(),
                            agent.getCurrentLongitude(),
                            customerCoordinates.latitude(),
                            customerCoordinates.longitude()
                    );
                }
            } catch (Exception e) {
                log.warn("Unable to fetch delivery agent live location for order {}: {}", orderId, e.getMessage());
            }
        }

        TrackingDistanceInfo restaurantToCustomer = buildDistanceInfo(
                restaurant.getLatitude(),
                restaurant.getLongitude(),
                customerCoordinates.latitude(),
                customerCoordinates.longitude()
        );

        return new OrderTrackingResponse(
                order.getOrderId(),
                order.getOrderNumber(),
                order.getOrderStatus().name(),
                restaurantPoint,
                customerPoint,
                deliveryAgentPoint,
                restaurantToCustomer,
                agentToRestaurant,
                agentToCustomer
        );
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(OrderStatusUpdateDTO request) {
        log.info("Updating order status: orderId={}, newStatus={}", request.getOrderId(), request.getStatus());

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new CustomException("Order not found"));

        Order.OrderStatus newStatus;
        try {
            newStatus = Order.OrderStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException("Invalid status value");
        }

        // Validate status transition (simplified)
        if (order.getOrderStatus() == Order.OrderStatus.CANCELLED || order.getOrderStatus() == Order.OrderStatus.DELIVERED) {
            throw new CustomException("Cannot update status of cancelled or delivered order");
        }

        order.setOrderStatus(newStatus);
        switch (newStatus) {
            case CONFIRMED -> order.setConfirmedAt(LocalDateTime.now());
            case PREPARING -> order.setPreparingAt(LocalDateTime.now());
            case PICKED_UP -> order.setPickedUpAt(LocalDateTime.now());
            case DELIVERED -> {
                order.setDeliveredAt(LocalDateTime.now());
                if (order.getDeliveryAgentId() != null) {
                    try {
                        deliveryClient.markDelivered(order.getDeliveryAgentId(), order.getOrderId());
                    } catch (Exception e) {
                        log.warn("Failed to notify delivery service of completion: {}", e.getMessage());
                    }
                }
            }
            case CANCELLED -> order.setCancelledAt(LocalDateTime.now());
        }
        
     // After order is updated, add this line before return
        Order updated = orderRepository.save(order);
        
        // ✅ THEN publish the event (after 'updated' exists)
        publishOrderEvent(updated, newStatus.name());
        return toResponse(updated);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long customerId) {
        log.info("Cancelling order: orderId={}, customerId={}", orderId, customerId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException("Order not found"));

        if (!order.getCustomerId().equals(customerId)) {
            throw new CustomException("You are not authorized to cancel this order");
        }

        if (order.getOrderStatus() != Order.OrderStatus.PLACED && order.getOrderStatus() != Order.OrderStatus.CONFIRMED) {
            throw new CustomException("Order cannot be cancelled after it is preparing or later");
        }

        // Process refund if payment was not COD
        if (!"COD".equals(order.getModeOfPayment())) {
            try {
                RefundRequestDTO refundReq = new RefundRequestDTO();
                refundReq.setOrderId(orderId);
                refundReq.setCustomerId(customerId);
                refundReq.setAmount(order.getFinalAmount());
                PaymentResponseDTO refundResp = paymentClient.refundPayment(refundReq);
                log.info("Refund processed: {}", refundResp);
            } catch (Exception e) {
                log.error("Refund failed: {}", e.getMessage());
                throw new CustomException("Refund failed: " + e.getMessage());
            }
        }

        order.setOrderStatus(Order.OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        Order updated = orderRepository.save(order);
        publishOrderEvent(updated, "CANCELLED");
        return toResponse(updated);
    }
    
    // ADDED During the Notification service creation 
    private void publishOrderEvent(Order order, String status) {
        try {
            OrderEventDTO event = new OrderEventDTO();
            event.setOrderId(order.getOrderId());
            event.setCustomerId(order.getCustomerId());
            event.setRestaurantId(order.getRestaurantId());
            event.setStatus(status);
            event.setEventTime(LocalDateTime.now());
            
            // 1. Try to get customer details
            try {
                UserDTO customer = authClient.getUserById(order.getCustomerId());
                if (customer != null) {
                    event.setCustomerEmail(customer.getEmail());
                    event.setCustomerPhone(customer.getPhone());
                }
            } catch (Exception e) {
                log.warn("Failed to fetch customer details for order event: {}", e.getMessage());
            }

            // 2. Try to get restaurant and owner details
            try {
                RestaurantDTO restaurant = restaurantClient.getRestaurantById(order.getRestaurantId());
                if (restaurant != null) {
                    Long ownerId = restaurant.getOwnerId();
                    if (ownerId != null) {
                        event.setRestaurantOwnerId(ownerId);
                        UserDTO owner = authClient.getUserById(ownerId);
                        if (owner != null) {
                            event.setRestaurantOwnerEmail(owner.getEmail());
                            event.setRestaurantOwnerPhone(owner.getPhone());
                        }
                    } else {
                        log.warn("Restaurant {} response did not include ownerId; skipping owner notification enrichment", order.getRestaurantId());
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to fetch restaurant owner details for order event: {}", e.getMessage());
            }

            // 3. Try to get delivery agent details.
            // Orders store the delivery service's agentId, but notifications
            // must target the auth userId so the agent bell can load them.
            if (order.getDeliveryAgentId() != null) {
                try {
                    AgentResponse assignedAgent = deliveryClient.getAgentById(order.getDeliveryAgentId());
                    if (assignedAgent != null && assignedAgent.getUserId() != null) {
                        UserDTO agentUser = authClient.getUserById(assignedAgent.getUserId());
                        event.setDeliveryAgentId(assignedAgent.getUserId());
                        event.setDeliveryAgentPhone(assignedAgent.getPhone());
                        if (agentUser != null) {
                            event.setDeliveryAgentEmail(agentUser.getEmail());
                            if (event.getDeliveryAgentPhone() == null) {
                                event.setDeliveryAgentPhone(agentUser.getPhone());
                            }
                        }
                    } else {
                        log.warn("Assigned delivery agent {} did not resolve to a userId for notifications", order.getDeliveryAgentId());
                    }
                } catch (Exception e) {
                    log.warn("Failed to fetch delivery agent details for order event: {}", e.getMessage());
                }
            }
            
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, event);
            log.info("📤 Order event published for order {}: {}", order.getOrderId(), status);
        } catch (Exception e) {
            log.error("Failed to publish order event: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public OrderResponse reorder(Long previousOrderId, Long customerId, String paymentMode) {
        log.info("Reorder from orderId={} for customerId={}", previousOrderId, customerId);

        Order previousOrder = orderRepository.findById(previousOrderId)
                .orElseThrow(() -> new CustomException("Previous order not found"));

        if (!previousOrder.getCustomerId().equals(customerId)) {
            throw new CustomException("You are not authorized to reorder from this order");
        }

        // Create new order with same items
        Order newOrder = new Order();
        newOrder.setOrderNumber(OrderNumberGenerator.generate());
        newOrder.setCustomerId(customerId);
        newOrder.setRestaurantId(previousOrder.getRestaurantId());
        newOrder.setTotalAmount(previousOrder.getTotalAmount());
        newOrder.setDiscount(0.0);
        newOrder.setFinalAmount(previousOrder.getFinalAmount());
        newOrder.setModeOfPayment(paymentMode);
        newOrder.setOrderStatus(Order.OrderStatus.PLACED);
        newOrder.setOrderDate(LocalDateTime.now());
        newOrder.setDeliveryAddress(previousOrder.getDeliveryAddress());
        newOrder.setCustomerLatitude(previousOrder.getCustomerLatitude());
        newOrder.setCustomerLongitude(previousOrder.getCustomerLongitude());

        for (OrderItem oldItem : previousOrder.getItems()) {
            OrderItem newItem = new OrderItem();
            newItem.setOrder(newOrder);
            newItem.setMenuItemId(oldItem.getMenuItemId());
            newItem.setName(oldItem.getName());
            newItem.setPrice(oldItem.getPrice());
            newItem.setQuantity(oldItem.getQuantity());
            newItem.setCustomization(oldItem.getCustomization());
            newOrder.getItems().add(newItem);
        }

        Order saved = orderRepository.save(newOrder);

        // Process payment (same as placeOrder)
        PaymentRequestDTO paymentReq = new PaymentRequestDTO();
        paymentReq.setOrderId(saved.getOrderId());
        paymentReq.setCustomerId(customerId);
        paymentReq.setAmount(saved.getFinalAmount());
        paymentReq.setMode(paymentMode);
        try {
            PaymentResponseDTO paymentResp = paymentClient.processPayment(paymentReq);
            if (!"PAID".equals(paymentResp.getStatus()) && !"COD".equals(paymentMode)) {
                throw new CustomException("Payment failed");
            }
        } catch (Exception e) {
            saved.setOrderStatus(Order.OrderStatus.CANCELLED);
            orderRepository.save(saved);
            throw new CustomException("Payment failed: " + e.getMessage());
        }

        // Assign delivery agent (same as placeOrder)
        try {
            com.quickbite.order.dto.RestaurantDTO restaurant = restaurantClient.getRestaurantById(saved.getRestaurantId());
            DeliveryAssignmentDTO assignReq = new DeliveryAssignmentDTO();
            assignReq.setOrderId(saved.getOrderId());
            assignReq.setRestaurantId(saved.getRestaurantId());
            assignReq.setRestaurantLat(restaurant.getLatitude());
            assignReq.setRestaurantLng(restaurant.getLongitude());
            assignReq.setCustomerId(saved.getCustomerId());
            assignReq.setDeliveryAddress(saved.getDeliveryAddress());
            assignReq.setPickupAddress(restaurant.getAddress());

            Long agentId = deliveryClient.assignDeliveryAgent(assignReq);
            saved.setDeliveryAgentId(agentId);
            orderRepository.save(saved);
        } catch (Exception e) {
            log.warn("Delivery assignment failed during reorder: {}", e.getMessage());
        }

        log.info("Reorder placed: {}", saved.getOrderNumber());
        publishOrderEvent(saved, "PLACED");
        return toResponse(saved);
    }

    private TrackingDistanceInfo buildDistanceInfo(Double fromLat, Double fromLng, Double toLat, Double toLng) {
        if (fromLat == null || fromLng == null || toLat == null || toLng == null) {
            return null;
        }

        double distanceKm = haversineKm(fromLat, fromLng, toLat, toLng);
        int estimatedMinutes = (int) Math.max(1, Math.round((distanceKm / AVERAGE_CITY_SPEED_KMPH) * 60));
        return new TrackingDistanceInfo(roundToTwoDecimals(distanceKm), estimatedMinutes, "HAVERSINE");
    }

    private CoordinatePair resolveCustomerCoordinates(Order order) {
        if (order.getCustomerLatitude() != null && order.getCustomerLongitude() != null) {
            return new CoordinatePair(order.getCustomerLatitude(), order.getCustomerLongitude());
        }

        Matcher matcher = GPS_PATTERN.matcher(order.getDeliveryAddress() == null ? "" : order.getDeliveryAddress());
        if (matcher.find()) {
            return new CoordinatePair(
                    Double.parseDouble(matcher.group(1)),
                    Double.parseDouble(matcher.group(2))
            );
        }

        throw new CustomException("Customer coordinates are unavailable for this order");
    }

    private double haversineKm(double fromLat, double fromLng, double toLat, double toLng) {
        double earthRadiusKm = 6371.0;
        double latDistance = Math.toRadians(toLat - fromLat);
        double lngDistance = Math.toRadians(toLng - fromLng);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(fromLat)) * Math.cos(Math.toRadians(toLat))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private record CoordinatePair(Double latitude, Double longitude) {}
}
