package com.quickbite.delivery.controller;

import com.quickbite.delivery.dto.AgentRegistrationRequest;
import com.quickbite.delivery.dto.AgentResponse;
import com.quickbite.delivery.dto.AssignmentRequest;
import com.quickbite.delivery.dto.EarningsResponse;
import com.quickbite.delivery.dto.LocationUpdateRequest;
import com.quickbite.delivery.entity.DeliveryAgent;
import com.quickbite.delivery.exception.CustomException;
import com.quickbite.delivery.service.DeliveryService;
import com.quickbite.delivery.util.RoleValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delivery")
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.OPTIONS})
@Slf4j
@Tag(name = "Delivery", description = "Manage delivery agents, location, assignment")
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    // 1. Register agent
    @PostMapping("/register")
    @Operation(summary = "Register a new delivery agent")
    public ResponseEntity<AgentResponse> register(@Valid @RequestBody AgentRegistrationRequest request) {
        return ResponseEntity.ok(deliveryService.registerAgent(request));
    }

    // 2. Verify agent – only ADMIN
    @PatchMapping("/verify/{agentId}")
    @Operation(summary = "Verify a delivery agent (Admin only)")
    public ResponseEntity<AgentResponse> verify(@PathVariable Long agentId,
                                                @RequestParam boolean verified,
                                                @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        RoleValidator.checkRole("ADMIN", userRole);
        return ResponseEntity.ok(deliveryService.verifyAgent(agentId, verified));
    }

    // 3. Toggle availability – only the agent themselves
    @PatchMapping("/availability")
    @Operation(summary = "Toggle online/offline status (agent only)")
    public ResponseEntity<AgentResponse> toggleAvailability(@RequestParam Long agentId,
                                                            @RequestParam boolean isAvailable,
                                                            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ResponseEntity.ok(deliveryService.toggleAvailability(agentId, isAvailable, userId));
    }

    // 4. Update location – only the agent themselves
    @PutMapping("/location")
    @Operation(summary = "Update current GPS location (agent only)")
    public ResponseEntity<AgentResponse> updateLocation(@Valid @RequestBody LocationUpdateRequest request,
                                                        @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ResponseEntity.ok(deliveryService.updateLocation(request, userId));
    }

    // 5. Find nearby available agents – public
    @GetMapping("/nearby")
    @Operation(summary = "Find nearby available agents")
    public ResponseEntity<List<AgentResponse>> getNearby(@RequestParam double lat,
                                                         @RequestParam double lng,
                                                         @RequestParam(defaultValue = "10") double radius) {
        return ResponseEntity.ok(deliveryService.getNearbyAvailableAgents(lat, lng, radius));
    }

    // 6. Assign order – internal
    @PostMapping("/assign")
    @Operation(summary = "Assign order to nearest agent (internal)")
    public ResponseEntity<Long> assignOrder(@RequestBody AssignmentRequest request) {
        return ResponseEntity.ok(deliveryService.assignOrder(request));
    }

    // 7. Mark order delivered – agent only
    @PostMapping("/delivered")
    @Operation(summary = "Mark order as delivered (agent only)")
    public ResponseEntity<AgentResponse> markDelivered(@RequestParam Long agentId,
                                                       @RequestParam Long orderId,
                                                       @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ResponseEntity.ok(deliveryService.markOrderDelivered(agentId, orderId, userId));
    }

    // 8. Get earnings – agent only
    @GetMapping("/earnings/{agentId}")
    @Operation(summary = "Get earnings summary (agent only)")
    public ResponseEntity<EarningsResponse> getEarnings(@PathVariable Long agentId,
                                                        @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ResponseEntity.ok(deliveryService.getEarnings(agentId, userId));
    }

    // 9. Get agent by ID – admin can view any, agent can view own
    @GetMapping("/{agentId}")
    @Operation(summary = "Get agent details")
    public ResponseEntity<AgentResponse> getAgent(@PathVariable Long agentId,
                                                  @RequestHeader(value = "X-User-Role", required = false) String userRole,
                                                  @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userRole != null && userRole.equals("ADMIN")) {
            return ResponseEntity.ok(deliveryService.getAgentById(agentId));
        }
        AgentResponse agent = deliveryService.getAgentById(agentId);
        if (userId != null && agent.getUserId().equals(userId)) {
            return ResponseEntity.ok(agent);
        }
        throw new CustomException("Access denied");
    }

    @GetMapping("/internal/agents/{agentId}")
    @Operation(summary = "Internal: get agent details by agent ID")
    public ResponseEntity<AgentResponse> getAgentInternal(@PathVariable Long agentId) {
        return ResponseEntity.ok(deliveryService.getAgentById(agentId));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get agent details by User ID")
    public ResponseEntity<AgentResponse> getAgentByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(deliveryService.getAgentByUserId(userId));
    }

    @GetMapping("/admin/all")
    @Operation(summary = "Get all agents (Admin only)")
    public ResponseEntity<java.util.List<AgentResponse>> listAll() {
        return ResponseEntity.ok(deliveryService.listAllAgents());
    }

    // Internal endpoint – called by Review Service to push updated avg rating
    @PutMapping("/internal/rating/{agentId}")
    @Operation(summary = "Internal: update agent avg rating (called by Review Service)")
    public ResponseEntity<Void> updateRating(@PathVariable Long agentId,
                                             @RequestParam Double rating) {
        deliveryService.updateAgentRating(agentId, rating);
        return ResponseEntity.ok().build();
    }

    private AgentResponse toAgentResponse(DeliveryAgent agent) {
        return new AgentResponse(agent.getAgentId(), agent.getUserId(), agent.getFullName(),
                agent.getPhone(), agent.getVehicleType(), agent.getVehicleNumber(),
                agent.getIsAvailable(), agent.getIsVerified(), agent.getAvgRating(),
                agent.getTotalDeliveries(), agent.getTotalEarnings(),
                agent.getCurrentLatitude(), agent.getCurrentLongitude());
    }
}
