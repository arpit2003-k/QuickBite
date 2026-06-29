package com.quickbite.restaurant.controller;

import com.quickbite.restaurant.dto.*;
import com.quickbite.restaurant.service.RestaurantService;
import com.quickbite.restaurant.util.RoleValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.OPTIONS})
@Slf4j
@Tag(name = "Restaurant", description = "Manage restaurants, nearby search, approval")
public class RestaurantController {

    @Autowired
    private RestaurantService restaurantService;

    // Public endpoints (Guest can view)
    @GetMapping("/nearby")
    @Operation(summary = "Get nearby restaurants (guest accessible)")
    public ResponseEntity<List<RestaurantResponse>> getNearby(@RequestParam Double lat,
                                                               @RequestParam Double lng,
                                                               @RequestParam(required = false) Double radius) {
        NearbyRequest req = new NearbyRequest();
        req.setLatitude(lat);
        req.setLongitude(lng);
        req.setRadius(radius);
        return ResponseEntity.ok(restaurantService.getNearbyRestaurants(req));
    }

    @GetMapping("/cuisine/{cuisine}")
    @Operation(summary = "Search restaurants by cuisine")
    public ResponseEntity<List<RestaurantResponse>> searchByCuisine(@PathVariable String cuisine) {
        return ResponseEntity.ok(restaurantService.searchByCuisine(cuisine));
    }

    @GetMapping("/{restaurantId}")
    @Operation(summary = "Get restaurant details by ID")
    public ResponseEntity<RestaurantResponse> getRestaurant(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(restaurantService.getRestaurantById(restaurantId));
    }

    @GetMapping("/approved")
    @Operation(summary = "Get all approved restaurants")
    public ResponseEntity<List<RestaurantResponse>> getAllApproved() {
        return ResponseEntity.ok(restaurantService.getAllApprovedRestaurants());
    }

    // Restaurant owner endpoints (should be protected by JWT with role RESTAURANT_OWNER)
    @PostMapping("/register")
    @Operation(summary = "Register a new restaurant (owner only)")
    public ResponseEntity<RestaurantResponse> register(@Valid @RequestBody RestaurantRequest request,
                                                       @RequestHeader(value = "X-User-Role", required = false) String userRole,
                                                       @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        //RoleValidator.checkRole("RESTAURANT_OWNER", userRole);
    	
        request.setOwnerId(userId);
        return ResponseEntity.ok(restaurantService.registerRestaurant(request));
    }

    @PutMapping("/{restaurantId}")
    @Operation(summary = "Update restaurant details (owner only)")
    public ResponseEntity<RestaurantResponse> update(@PathVariable Long restaurantId,
                                                      @Valid @RequestBody RestaurantUpdateRequest request,
                                                      @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        //RoleValidator.checkRole("RESTAURANT_OWNER", userRole);
        return ResponseEntity.ok(restaurantService.updateRestaurant(restaurantId, request));
    }

    @PatchMapping("/{restaurantId}/open")
    @Operation(summary = "Toggle restaurant open/close status")
    public ResponseEntity<RestaurantResponse> toggleOpen(@PathVariable Long restaurantId,
                                                          @RequestParam boolean isOpen,
                                                          @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        //RoleValidator.checkRole("RESTAURANT_OWNER", userRole);
        return ResponseEntity.ok(restaurantService.toggleOpenStatus(restaurantId, isOpen));
    }

    @GetMapping("/owner/{ownerId}")
    @Operation(summary = "Get restaurants by owner ID")
    public ResponseEntity<List<RestaurantResponse>> getByOwner(@PathVariable Long ownerId) {
        return ResponseEntity.ok(restaurantService.getRestaurantsByOwner(ownerId));
    }

    // Admin endpoints (role ADMIN)
    @PatchMapping("/admin/approve/{restaurantId}")
    @Operation(summary = "Approve or reject restaurant (admin only)")
    public ResponseEntity<RestaurantResponse> approve(@PathVariable Long restaurantId,
                                                       @RequestParam boolean approved,
                                                       @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        //RoleValidator.checkRole("ADMIN", userRole);
        return ResponseEntity.ok(restaurantService.approveRestaurant(restaurantId, approved));
    }

    @GetMapping("/admin/pending")
    @Operation(summary = "Get all pending restaurants (admin only)")
    public ResponseEntity<List<RestaurantResponse>> getPending() {
        return ResponseEntity.ok(restaurantService.getPendingRestaurants());
    }

    // Internal endpoint – called by Review Service to push updated avg rating
    @PutMapping("/internal/rating/{restaurantId}")
    @Operation(summary = "Internal: update restaurant avg rating (called by Review Service)")
    public ResponseEntity<Void> updateRating(@PathVariable Long restaurantId,
                                             @RequestParam Double rating) {
        restaurantService.updateRating(restaurantId, rating);
        return ResponseEntity.ok().build();
    }
}
