package com.quickbite.delivery.service;

import com.quickbite.delivery.dto.*;
import com.quickbite.delivery.entity.DeliveryAgent;

import java.util.List;

public interface DeliveryService {
    AgentResponse registerAgent(AgentRegistrationRequest request);
    AgentResponse verifyAgent(Long agentId, boolean verified);
    AgentResponse toggleAvailability(Long agentId, boolean isAvailable, Long requestingUserId);
    AgentResponse updateLocation(LocationUpdateRequest request, Long requestingUserId);
    List<AgentResponse> getNearbyAvailableAgents(double lat, double lng, double radius);
    Long assignOrder(AssignmentRequest request);
    AgentResponse markOrderDelivered(Long agentId, Long orderId, Long requestingUserId);
    EarningsResponse getEarnings(Long agentId, Long requestingUserId);
    AgentResponse getAgentById(Long agentId);
    AgentResponse getAgentByUserId(Long userId);
    List<AgentResponse> listAllAgents();
    void updateAgentRating(Long agentId, Double newAvgRating);
}
