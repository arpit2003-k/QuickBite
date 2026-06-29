package com.quickbite.delivery.service;

import com.quickbite.delivery.dto.*;
import com.quickbite.delivery.entity.DeliveryAgent;
import com.quickbite.delivery.exception.CustomException;
import com.quickbite.delivery.repository.DeliveryAgentRepository;
import com.quickbite.delivery.util.RoleValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DeliveryServiceImpl implements DeliveryService {

    @Autowired
    private DeliveryAgentRepository agentRepository;

    private AgentResponse toResponse(DeliveryAgent agent) {
        return new AgentResponse(agent.getAgentId(), agent.getUserId(), agent.getFullName(),
                agent.getPhone(), agent.getVehicleType(), agent.getVehicleNumber(),
                agent.getIsAvailable(), agent.getIsVerified(), agent.getAvgRating(),
                agent.getTotalDeliveries(), agent.getTotalEarnings(),
                agent.getCurrentLatitude(), agent.getCurrentLongitude());
    }

    @Override
    @Transactional
    public AgentResponse registerAgent(AgentRegistrationRequest request) {
        log.info("Registering delivery agent for userId: {}", request.getUserId());
        if (agentRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new CustomException("Agent already registered");
        }
        DeliveryAgent agent = new DeliveryAgent();
        agent.setUserId(request.getUserId());
        agent.setFullName(request.getFullName());
        agent.setPhone(request.getPhone());
        agent.setVehicleType(request.getVehicleType());
        agent.setVehicleNumber(request.getVehicleNumber());
        agent.setRegisteredAt(LocalDateTime.now());
        agent.setIsVerified(false);
        agent.setIsAvailable(false);
        return toResponse(agentRepository.save(agent));
    }

    @Override
    @Transactional
    public AgentResponse verifyAgent(Long agentId, boolean verified) {
        log.info("Verifying agent {}: {}", agentId, verified);
        DeliveryAgent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new CustomException("Agent not found"));
        agent.setIsVerified(verified);
        if (verified) agent.setVerifiedAt(LocalDateTime.now());
        return toResponse(agentRepository.save(agent));
    }

    @Override
    @Transactional
    public AgentResponse toggleAvailability(Long agentId, boolean isAvailable, Long requestingUserId) {
        DeliveryAgent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new CustomException("Agent not found"));
        // Only the agent themselves can change their availability
       // RoleValidator.checkUserId(agent.getUserId(), requestingUserId);
        agent.setIsAvailable(isAvailable);
        return toResponse(agentRepository.save(agent));
    }

    @Override
    @Transactional
    public AgentResponse updateLocation(LocationUpdateRequest request, Long requestingUserId) {
        DeliveryAgent agent = agentRepository.findById(request.getAgentId())
                .orElseThrow(() -> new CustomException("Agent not found"));
        //RoleValidator.checkUserId(agent.getUserId(), requestingUserId);
        agent.setCurrentLatitude(request.getLatitude());
        agent.setCurrentLongitude(request.getLongitude());
        return toResponse(agentRepository.save(agent));
    }

    @Override
    public List<AgentResponse> getNearbyAvailableAgents(double lat, double lng, double radius) {
        return agentRepository.findNearbyAvailableAgents(lat, lng, radius)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Long assignOrder(AssignmentRequest request) {
        log.info("Assigning order {} to nearest agent", request.getOrderId());
        List<DeliveryAgent> nearby = agentRepository.findNearbyAvailableAgents(
                request.getRestaurantLat(), request.getRestaurantLng(), 10.0);
        if (nearby.isEmpty()) {
            throw new CustomException("No available delivery agent nearby");
        }
        // The repository returns the nearest eligible agent first.
        DeliveryAgent assignedAgent = nearby.get(0);
        assignedAgent.setIsAvailable(false);
        agentRepository.save(assignedAgent);
        Long agentId = assignedAgent.getAgentId();
        log.info("Assigned order {} to agent {}", request.getOrderId(), agentId);
        // In a real system, you would call Order Service to set agentId
        return agentId;
    }

    @Override
    @Transactional
    public AgentResponse markOrderDelivered(Long agentId, Long orderId, Long requestingUserId) {
        DeliveryAgent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new CustomException("Agent not found"));
        
        System.out.println("checking the error");
        //RoleValidator.checkUserId(agent.getUserId(), requestingUserId);
        double deliveryFee = 50.0;
        agent.setTotalEarnings(agent.getTotalEarnings() + deliveryFee);
        agent.setTotalDeliveries(agent.getTotalDeliveries() + 1);
        agent.setIsAvailable(true);
        // Optionally call Order Service to update status to DELIVERED
        return toResponse(agentRepository.save(agent));
    }

    @Override
    public EarningsResponse getEarnings(Long agentId, Long requestingUserId) {
        DeliveryAgent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new CustomException("Agent not found"));
        //RoleValidator.checkUserId(agent.getUserId(), requestingUserId);
        return new EarningsResponse(agent.getAgentId(), agent.getTotalEarnings(),
                agent.getTotalDeliveries(), agent.getAvgRating());
    }

    @Override
    public AgentResponse getAgentById(Long agentId) {
        // Admin or agent themselves can view; we'll handle in controller
        return toResponse(agentRepository.findById(agentId)
                .orElseThrow(() -> new CustomException("Agent not found")));
    }

    @Override
    public AgentResponse getAgentByUserId(Long userId) {
        return toResponse(agentRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException("Agent profile not found for this user")));
    }

    @Override
    public List<AgentResponse> listAllAgents() {
        return agentRepository.findAll().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateAgentRating(Long agentId, Double newAvgRating) {
        DeliveryAgent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new CustomException("Agent not found"));
        agent.setAvgRating(newAvgRating);
        agentRepository.save(agent);
        log.info("Updated avgRating for agent {} to {}", agentId, newAvgRating);
    }
}
