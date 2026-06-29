package com.quickbite.order.service;

import com.quickbite.order.client.*;
import com.quickbite.order.dto.*;
import com.quickbite.order.entity.Order;
import com.quickbite.order.exception.CustomException;
import com.quickbite.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CartClient cartClient;
    @Mock
    private PaymentClient paymentClient;
    @Mock
    private DeliveryClient deliveryClient;
    @Mock
    private RestaurantClient restaurantClient;

    @InjectMocks
    private OrderServiceImpl orderService;

    private OrderRequest orderRequest;
    private CartDTO mockCart;
    private Order mockOrder;

    @BeforeEach
    void setUp() {
        orderRequest = new OrderRequest();
        orderRequest.setCustomerId(1L);
        orderRequest.setPaymentMode("COD");
        orderRequest.setDeliveryAddress("123 Street");

        mockCart = new CartDTO();
        mockCart.setRestaurantId(10L);
        mockCart.setTotalPrice(500.0);
        List<CartItemDTO> items = new ArrayList<>();
        CartItemDTO item = new CartItemDTO();
        item.setName("Burger");
        item.setPrice(250.0);
        item.setQuantity(2);
        items.add(item);
        mockCart.setItems(items);

        mockOrder = new Order();
        mockOrder.setOrderId(100L);
        mockOrder.setOrderNumber("ORD123");
        mockOrder.setCustomerId(1L);
        mockOrder.setOrderStatus(Order.OrderStatus.PLACED);
        mockOrder.setFinalAmount(500.0);
        mockOrder.setRestaurantId(10L);
        mockOrder.setItems(new ArrayList<>());
    }

    @Test
    void placeOrder_Success_ReturnsOrderResponse() {
        when(cartClient.getCart(1L)).thenReturn(mockCart);
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);
        when(paymentClient.processPayment(any())).thenReturn(new PaymentResponseDTO(1L, "PAID", "Success", "TXN123"));

        RestaurantDTO mockRestaurant = new RestaurantDTO();
        mockRestaurant.setLatitude(12.34);
        mockRestaurant.setLongitude(56.78);
        mockRestaurant.setAddress("Restaurant Address");
        when(restaurantClient.getRestaurantById(any())).thenReturn(mockRestaurant);

        OrderResponse response = orderService.placeOrder(orderRequest);

        assertNotNull(response);
        verify(cartClient).clearCart(1L);
    }

    @Test
    void placeOrder_EmptyCart_ThrowsCustomException() {
        mockCart.setItems(Collections.emptyList());
        when(cartClient.getCart(1L)).thenReturn(mockCart);

        assertThrows(CustomException.class, () -> orderService.placeOrder(orderRequest));
    }

    @Test
    void placeOrder_PaymentFailed_CancelsOrder() {
        when(cartClient.getCart(1L)).thenReturn(mockCart);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order o = i.getArgument(0);
            o.setOrderId(100L);
            return o;
        });
        when(paymentClient.processPayment(any())).thenThrow(new RuntimeException("API Error"));

        assertThrows(CustomException.class, () -> orderService.placeOrder(orderRequest));

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, atLeastOnce()).save(orderCaptor.capture());
        assertEquals(Order.OrderStatus.CANCELLED, orderCaptor.getValue().getOrderStatus());
    }

    @Test
    void updateOrderStatus_Success_UpdatesStatus() {
        OrderStatusUpdateDTO req = new OrderStatusUpdateDTO(100L, "CONFIRMED");
        when(orderRepository.findById(100L)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        OrderResponse response = orderService.updateOrderStatus(req);

        assertEquals("CONFIRMED", response.getOrderStatus());
    }

    @Test
    void cancelOrder_Success_UpdatesStatus() {
        mockOrder.setModeOfPayment("COD");
        when(orderRepository.findById(100L)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        OrderResponse response = orderService.cancelOrder(100L, 1L);

        assertEquals("CANCELLED", response.getOrderStatus());
    }

    @Test
    void cancelOrder_Unauthorized_ThrowsCustomException() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(mockOrder));

        assertThrows(CustomException.class, () -> orderService.cancelOrder(100L, 999L));
    }

    @Test
    void reorder_Success_CreatesNewOrder() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);
        when(paymentClient.processPayment(any())).thenReturn(new PaymentResponseDTO(1L, "PAID", "Success", "TXN123"));

        RestaurantDTO mockRestaurant = new RestaurantDTO();
        mockRestaurant.setLatitude(12.34);
        mockRestaurant.setLongitude(56.78);
        mockRestaurant.setAddress("Restaurant Address");
        when(restaurantClient.getRestaurantById(any())).thenReturn(mockRestaurant);

        OrderResponse response = orderService.reorder(100L, 1L, "COD");

        assertNotNull(response);
    }
}