package com.quickbite.review.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "restaurant-service", url = "http://localhost:8082")
public interface RestaurantClient {
    @PutMapping("/api/restaurants/internal/rating/{restaurantId}")
    void updateRestaurantRating(@PathVariable("restaurantId") Long restaurantId,
                                @RequestParam("rating") Double rating);
}
