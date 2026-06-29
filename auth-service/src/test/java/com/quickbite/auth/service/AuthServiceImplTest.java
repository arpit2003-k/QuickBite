package com.quickbite.auth.service;

import com.quickbite.auth.dto.*;
import com.quickbite.auth.entity.User;
import com.quickbite.auth.exception.CustomException;
import com.quickbite.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User mockUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password");
        registerRequest.setFullName("Test User");
        registerRequest.setPhone("1234567890");
        registerRequest.setRole("CUSTOMER");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password");

        mockUser = new User();
        mockUser.setUserId(1L);
        mockUser.setEmail("test@example.com");
        mockUser.setPasswordHash("hashedPassword");
        mockUser.setRole(User.Role.CUSTOMER);
        mockUser.setIsActive(true);
    }

    @Test
    void register_Success_ReturnsAuthResponse() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(jwtService.generateToken(anyString(), anyString(), anyLong())).thenReturn("mockToken");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("test@example.com", response.getEmail());
        assertEquals("mockToken", response.getToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_DuplicateEmail_ThrowsCustomException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(CustomException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_InvalidRole_ThrowsCustomException() {
        registerRequest.setRole("INVALID_ROLE");

        assertThrows(CustomException.class, () -> authService.register(registerRequest));
    }

    @Test
    void register_AdminRoleAttempt_ThrowsCustomException() {
        registerRequest.setRole("ADMIN");

        assertThrows(CustomException.class, () -> authService.register(registerRequest));
    }

    @Test
    void login_Success_ReturnsAuthResponse() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateToken(anyString(), anyString(), anyLong())).thenReturn("mockToken");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("mockToken", response.getToken());
    }

    @Test
    void login_InvalidEmail_ThrowsCustomException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(CustomException.class, () -> authService.login(loginRequest));
    }

    @Test
    void login_InvalidPassword_ThrowsCustomException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(CustomException.class, () -> authService.login(loginRequest));
    }

    @Test
    void login_AccountDisabled_ThrowsCustomException() {
        mockUser.setIsActive(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        assertThrows(CustomException.class, () -> authService.login(loginRequest));
    }

    @Test
    void getUserProfile_Success_ReturnsUserDTO() {
        when(userRepository.findByUserId(anyLong())).thenReturn(Optional.of(mockUser));

        UserDTO result = authService.getUserProfile(1L);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void getUserProfile_UserNotFound_ThrowsCustomException() {
        when(userRepository.findByUserId(anyLong())).thenReturn(Optional.empty());

        assertThrows(CustomException.class, () -> authService.getUserProfile(1L));
    }

    @Test
    void getAllUsers_Success_ReturnsList() {
        when(userRepository.findAll()).thenReturn(Collections.singletonList(mockUser));

        var result = authService.getAllUsers();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void updateUserStatus_Success_UpdatesStatus() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(mockUser));

        authService.updateUserStatus(1L, false);

        assertFalse(mockUser.getIsActive());
        verify(userRepository).save(mockUser);
    }

    @Test
    void getPlatformStats_Success_ReturnsStats() {
        when(userRepository.count()).thenReturn(1L);
        when(userRepository.countByRole(User.Role.CUSTOMER)).thenReturn(1L);
        when(userRepository.countByRole(User.Role.RESTAURANT_OWNER)).thenReturn(0L);
        when(userRepository.countByRole(User.Role.DELIVERY_AGENT)).thenReturn(0L);
        when(userRepository.countByIsActiveTrue()).thenReturn(1L);

        PlatformStatsDTO stats = authService.getPlatformStats();

        assertNotNull(stats);
        assertEquals(1, stats.getTotalUsers());
        assertEquals(1, stats.getTotalCustomers());
        assertEquals(0, stats.getTotalOwners());
        assertEquals(0, stats.getTotalAgents());
        assertEquals(1, stats.getActiveUsers());
    }
}
