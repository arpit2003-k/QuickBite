package com.quickbite.menu.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "restaurant-service", url = "http://localhost:8082") // or use service discovery
public interface RestaurantClient {
    @GetMapping("/api/restaurants/{restaurantId}")
    Object getRestaurant(@PathVariable("restaurantId") Long restaurantId);
 // Return type can be Map or String; we only care if call succeeds (200 OK)
}