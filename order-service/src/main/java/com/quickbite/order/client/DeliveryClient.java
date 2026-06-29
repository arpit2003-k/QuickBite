package com.quickbite.order.client;

import com.quickbite.order.dto.AgentResponse;
import com.quickbite.order.dto.DeliveryAssignmentDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "delivery-service", url = "http://localhost:8087")
public interface DeliveryClient {

    @PostMapping("/api/delivery/assign")
    Long assignDeliveryAgent(@RequestBody DeliveryAssignmentDTO request);

    @GetMapping("/api/delivery/internal/agents/{agentId}")
    AgentResponse getAgentById(@PathVariable("agentId") Long agentId);

    @PostMapping("/api/delivery/delivered")
    void markDelivered(@RequestParam("agentId") Long agentId, @RequestParam("orderId") Long orderId);
}
