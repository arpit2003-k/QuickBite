package com.quickbite.order.client;

import com.quickbite.order.dto.RestaurantDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "restaurant-service", url = "http://localhost:8082")
public interface RestaurantClient {

    @GetMapping("/api/restaurants/{restaurantId}")
    RestaurantDTO getRestaurantById(@PathVariable("restaurantId") Long restaurantId);
}
