package com.quickbite.auth.service;

import com.quickbite.auth.dto.AuthResponse;
import com.quickbite.auth.dto.ChangePasswordRequest;
import com.quickbite.auth.dto.LoginRequest;
import com.quickbite.auth.dto.UpdateProfileRequest;
import com.quickbite.auth.dto.RegisterRequest;
import com.quickbite.auth.dto.UserDTO;
import com.quickbite.auth.entity.User;
import com.quickbite.auth.exception.CustomException;
import com.quickbite.auth.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Override
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            log.error("Email already registered: {}", request.getEmail());
            throw new CustomException("Email already registered");
        }

        // Convert role string to enum
        User.Role role;
        try {
            role = User.Role.valueOf(request.getRole().toUpperCase());
            // Prevent registration as ADMIN via public endpoint
            if (role == User.Role.ADMIN) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            throw new CustomException("Invalid role. Allowed: CUSTOMER, RESTAURANT_OWNER, DELIVERY_AGENT");
        }

        // Create user entity
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(role);
        user.setProvider("LOCAL");
        user.setIsActive(true);

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getUserId());

        // Generate JWT token
        String token = jwtService.generateToken(savedUser.getEmail(), savedUser.getRole().name(), savedUser.getUserId());

        return new AuthResponse(token, "Bearer", savedUser.getUserId(), savedUser.getEmail(), savedUser.getRole().name());
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Invalid password for email: {}", request.getEmail());
            throw new CustomException("Invalid email or password");
        }

        if (!user.getIsActive()) {
            log.warn("Account disabled for email: {}", request.getEmail());
            throw new CustomException("Account is disabled. Contact admin.");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name(), user.getUserId());
        log.info("Login successful for user: {}", user.getEmail());

        return new AuthResponse(token, "Bearer", user.getUserId(), user.getEmail(), user.getRole().name());
    }

    @Override
    public UserDTO getUserProfile(Long userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException("User not found"));
        return toUserDto(user);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public UserDTO updateUserProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException("User not found"));
        user.setFullName(request.getFullName().trim());
        user.setPhone(request.getPhone().trim());
        return toUserDto(userRepository.save(user));
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException("User not found"));

        boolean hasLocalPassword = user.getPasswordHash() != null && !user.getPasswordHash().isBlank();
        boolean localProvider = user.getProvider() == null || "LOCAL".equalsIgnoreCase(user.getProvider());

        if (hasLocalPassword && localProvider) {
            if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
                throw new CustomException("Current password is required");
            }
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
                throw new CustomException("Current password is incorrect");
            }
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        if (!localProvider) {
            user.setProvider("LOCAL");
        }
        userRepository.save(user);
    }

    private UserDTO toUserDto(User user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole().name());
        dto.setProfilePicUrl(user.getProfilePicUrl());
        dto.setIsActive(user.getIsActive());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    @Override
    public java.util.List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(user -> {
            UserDTO dto = new UserDTO();
            dto.setUserId(user.getUserId());
            dto.setFullName(user.getFullName());
            dto.setEmail(user.getEmail());
            dto.setPhone(user.getPhone());
            dto.setRole(user.getRole().name());
            dto.setIsActive(user.getIsActive());
            dto.setCreatedAt(user.getCreatedAt());
            return dto;
        }).collect(java.util.stream.Collectors.toList());
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void updateUserStatus(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User not found"));
        user.setIsActive(active);
        userRepository.save(user);
        log.info("User {} status updated to {}", userId, active ? "ACTIVE" : "SUSPENDED");
    }

    @Override
    public com.quickbite.auth.dto.PlatformStatsDTO getPlatformStats() {
        long total = userRepository.count();
        long customers = userRepository.countByRole(User.Role.CUSTOMER);
        long owners = userRepository.countByRole(User.Role.RESTAURANT_OWNER);
        long agents = userRepository.countByRole(User.Role.DELIVERY_AGENT);
        long active = userRepository.countByIsActiveTrue();

        return new com.quickbite.auth.dto.PlatformStatsDTO(total, customers, owners, agents, active);
    }
}
