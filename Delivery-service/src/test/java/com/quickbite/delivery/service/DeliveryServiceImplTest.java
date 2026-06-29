package com.quickbite.delivery.service;

import com.quickbite.delivery.dto.*;
import com.quickbite.delivery.entity.DeliveryAgent;
import com.quickbite.delivery.exception.CustomException;
import com.quickbite.delivery.repository.DeliveryAgentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceImplTest {

    @Mock
    private DeliveryAgentRepository agentRepository;

    @InjectMocks
    private DeliveryServiceImpl deliveryService;

    private AgentRegistrationRequest registrationRequest;
    private DeliveryAgent mockAgent;

    @BeforeEach
    void setUp() {
        registrationRequest = new AgentRegistrationRequest();
        registrationRequest.setUserId(1L);
        registrationRequest.setFullName("Ravi Kumar");
        registrationRequest.setPhone("9876543210");

        mockAgent = new DeliveryAgent();
        mockAgent.setAgentId(10L);
        mockAgent.setUserId(1L);
        mockAgent.setFullName("Ravi Kumar");
        mockAgent.setIsVerified(true);
        mockAgent.setIsAvailable(true);
        mockAgent.setTotalEarnings(500.0);
    }

    @Test
    void registerAgent_Success_ReturnsResponse() {
        when(agentRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(agentRepository.save(any(DeliveryAgent.class))).thenReturn(mockAgent);

        AgentResponse response = deliveryService.registerAgent(registrationRequest);

        assertNotNull(response);
        verify(agentRepository).save(any(DeliveryAgent.class));
    }

    @Test
    void registerAgent_AlreadyExists_ThrowsCustomException() {
        when(agentRepository.findByUserId(1L)).thenReturn(Optional.of(mockAgent));

        assertThrows(CustomException.class, () -> deliveryService.registerAgent(registrationRequest));
    }

    @Test
    void verifyAgent_Success_UpdatesStatus() {
        when(agentRepository.findById(10L)).thenReturn(Optional.of(mockAgent));
        when(agentRepository.save(any(DeliveryAgent.class))).thenReturn(mockAgent);

        AgentResponse response = deliveryService.verifyAgent(10L, true);

        assertTrue(response.getIsVerified());
    }

    @Test
    void toggleAvailability_Success_UpdatesStatus() {
        when(agentRepository.findById(10L)).thenReturn(Optional.of(mockAgent));
        when(agentRepository.save(any(DeliveryAgent.class))).thenReturn(mockAgent);

        AgentResponse response = deliveryService.toggleAvailability(10L, false, 1L);

        assertFalse(response.getIsAvailable());
    }

    @Test
    void updateLocation_Success_UpdatesCoords() {
        LocationUpdateRequest req = new LocationUpdateRequest();
        req.setAgentId(10L);
        req.setLatitude(12.34);
        req.setLongitude(56.78);
        when(agentRepository.findById(10L)).thenReturn(Optional.of(mockAgent));
        when(agentRepository.save(any(DeliveryAgent.class))).thenReturn(mockAgent);

        AgentResponse response = deliveryService.updateLocation(req, 1L);

        assertNotNull(response);
    }

    @Test
    void assignOrder_Success_ReturnsAgentId() {
        AssignmentRequest req = new AssignmentRequest();
        req.setRestaurantLat(12.0);
        req.setRestaurantLng(56.0);
        when(agentRepository.findNearbyAvailableAgents(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(Collections.singletonList(mockAgent));

        Long agentId = deliveryService.assignOrder(req);

        assertEquals(10L, agentId);
    }

    @Test
    void assignOrder_NoAgentsNearby_ThrowsCustomException() {
        AssignmentRequest req = new AssignmentRequest();
        req.setRestaurantLat(12.0);
        req.setRestaurantLng(56.0);
        
        when(agentRepository.findNearbyAvailableAgents(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(Collections.emptyList());

        assertThrows(CustomException.class, () -> deliveryService.assignOrder(req));
    }

    @Test
    void markOrderDelivered_Success_UpdatesEarnings() {
        when(agentRepository.findById(10L)).thenReturn(Optional.of(mockAgent));
        when(agentRepository.save(any(DeliveryAgent.class))).thenReturn(mockAgent);

        AgentResponse response = deliveryService.markOrderDelivered(10L, 100L, 1L);

        assertEquals(550.0, mockAgent.getTotalEarnings());
    }

    @Test
    void updateAgentRating_Success_UpdatesRating() {
        when(agentRepository.findById(10L)).thenReturn(Optional.of(mockAgent));

        deliveryService.updateAgentRating(10L, 4.8);

        assertEquals(4.8, mockAgent.getAvgRating());
        verify(agentRepository).save(mockAgent);
    }

    @Test
    void getEarnings_Success_ReturnsValue() {
        when(agentRepository.findById(10L)).thenReturn(Optional.of(mockAgent));

        EarningsResponse response = deliveryService.getEarnings(10L, 1L);

        assertEquals(500.0, response.getTotalEarnings());
    }

    @Test
    void getAgentById_Success_ReturnsResponse() {
        when(agentRepository.findById(10L)).thenReturn(Optional.of(mockAgent));

        AgentResponse response = deliveryService.getAgentById(10L);

        assertEquals(10L, response.getAgentId());
    }

    @Test
    void getAgentByUserId_Success_ReturnsResponse() {
        when(agentRepository.findByUserId(1L)).thenReturn(Optional.of(mockAgent));

        AgentResponse response = deliveryService.getAgentByUserId(1L);

        assertEquals(1L, response.getUserId());
    }

    @Test
    void listAllAgents_Success_ReturnsList() {
        when(agentRepository.findAll()).thenReturn(Collections.singletonList(mockAgent));

        List<AgentResponse> results = deliveryService.listAllAgents();

        assertEquals(1, results.size());
    }
}
