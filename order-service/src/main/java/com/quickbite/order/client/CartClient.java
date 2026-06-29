package com.quickbite.order.client;

import com.quickbite.order.dto.CartDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "cart-service", url = "http://localhost:8084")
public interface CartClient {

    @GetMapping("/api/cart/{customerId}")
    CartDTO getCart(@PathVariable("customerId") Long customerId);

    @DeleteMapping("/api/cart/{customerId}")
    void clearCart(@PathVariable("customerId") Long customerId);
}
