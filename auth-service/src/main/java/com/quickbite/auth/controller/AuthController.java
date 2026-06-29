package com.quickbite.auth.controller;

import com.quickbite.auth.dto.AuthResponse;
import com.quickbite.auth.dto.ChangePasswordRequest;
import com.quickbite.auth.dto.LoginRequest;
import com.quickbite.auth.dto.RegisterRequest;
import com.quickbite.auth.dto.UpdateProfileRequest;
import com.quickbite.auth.dto.UserDTO;
import com.quickbite.auth.exception.CustomException;
import com.quickbite.auth.service.AuthService;
import com.quickbite.auth.service.JwtService;
import com.quickbite.auth.util.RoleValidator;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Slf4j
@Tag(name = "Authentication", description = "Register, Login, and Profile APIs")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    // Public endpoint: Guest users can register (no auth required)
    @PostMapping("/register")
    @Operation(summary = "Register a new user (Customer, Owner, or Agent)")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request for email: {}", request.getEmail());
        return ResponseEntity.ok(authService.register(request));
    }

    // Public endpoint: Guest users can login
    @PostMapping("/login")
    @Operation(summary = "Login with email and password to receive JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for email: {}", request.getEmail());
        return ResponseEntity.ok(authService.login(request));
    }

    // Protected endpoint: Only authenticated users can view their profile
    @GetMapping("/profile")
    @Operation(summary = "Get current user profile (requires JWT)")
    public ResponseEntity<UserDTO> getProfile(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);
        log.info("Fetching profile for user ID: {}", userId);
        return ResponseEntity.ok(authService.getUserProfile(userId));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update current user profile name and phone (requires JWT)")
    public ResponseEntity<UserDTO> updateProfile(@RequestHeader("Authorization") String authHeader,
                                                 @Valid @RequestBody UpdateProfileRequest request) {
        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);
        log.info("Updating profile for user ID: {}", userId);
        return ResponseEntity.ok(authService.updateUserProfile(userId, request));
    }

    @PutMapping("/profile/password")
    @Operation(summary = "Change current user password (requires JWT)")
    public ResponseEntity<Void> changePassword(@RequestHeader("Authorization") String authHeader,
                                               @Valid @RequestBody ChangePasswordRequest request) {
        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);
        log.info("Changing password for user ID: {}", userId);
        authService.changePassword(userId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/users")
    @Operation(summary = "Get all users (Admin only)")
    public ResponseEntity<java.util.List<UserDTO>> listUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }

    @GetMapping("/admin/stats")
    @Operation(summary = "Get platform-wide user statistics (Admin only)")
    public ResponseEntity<com.quickbite.auth.dto.PlatformStatsDTO> getPlatformStats() {
        return ResponseEntity.ok(authService.getPlatformStats());
    }

    @PatchMapping("/admin/users/{userId}/status")
    @Operation(summary = "Enable or disable user account (Admin only)")
    public ResponseEntity<Void> toggleUserStatus(@PathVariable Long userId, @RequestParam boolean active) {
        authService.updateUserStatus(userId, active);
        return ResponseEntity.ok().build();
    }
    
    // Internal endpoint for other microservices (Order Service, Notification Service)
    // This endpoint does NOT require JWT because it's called internally
    @GetMapping("/internal/users/{userId}")
    @Operation(summary = "Get user by ID (internal microservice call)")
    public ResponseEntity<UserDTO> getUserByIdInternal(@PathVariable Long userId) {
    	log.info("Internal call to fetch user details for userId: {}", userId);
    	return ResponseEntity.ok(authService.getUserProfile(userId));
    }

    @GetMapping("/internal/users")
    @Operation(summary = "Get all users (internal microservice call)")
    public ResponseEntity<java.util.List<UserDTO>> listUsersInternal() {
    	log.info("Internal call to fetch all users");
    	return ResponseEntity.ok(authService.getAllUsers());
    }

    // Admin-only endpoint example (role-based access controlled by SecurityConfig)
    @GetMapping("/admin/test")
    @Operation(summary = "Admin only test endpoint")
    public ResponseEntity<String> adminTest() {
        return ResponseEntity.ok("Admin access granted");
    }
}
