package com.quickbite.notification.client;

import com.quickbite.notification.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "auth-service", url = "http://localhost:8081")
public interface AuthClient {

    @GetMapping("/api/auth/internal/users/{userId}")
    UserDTO getUserById(@PathVariable("userId") Long userId);

    @GetMapping("/api/auth/internal/users")
    List<UserDTO> getAllUsers();
}
