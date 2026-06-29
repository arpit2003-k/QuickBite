package com.quickbite.review.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "delivery-service", url = "http://localhost:8087")
public interface DeliveryClient {
    @PutMapping("/api/delivery/internal/rating/{agentId}")
    void updateDeliveryAgentRating(@PathVariable("agentId") Long agentId,
                                   @RequestParam("rating") Double rating);
}
