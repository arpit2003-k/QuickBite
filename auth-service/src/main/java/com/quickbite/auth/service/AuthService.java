package com.quickbite.auth.service;

import com.quickbite.auth.dto.AuthResponse;
import com.quickbite.auth.dto.ChangePasswordRequest;
import com.quickbite.auth.dto.LoginRequest;
import com.quickbite.auth.dto.UpdateProfileRequest;
import com.quickbite.auth.dto.RegisterRequest;
import com.quickbite.auth.dto.UserDTO;
import com.quickbite.auth.dto.PlatformStatsDTO;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserDTO getUserProfile(Long userId);
    UserDTO updateUserProfile(Long userId, UpdateProfileRequest request);
    void changePassword(Long userId, ChangePasswordRequest request);
    java.util.List<UserDTO> getAllUsers();
    void updateUserStatus(Long userId, boolean active);
    PlatformStatsDTO getPlatformStats();
}
