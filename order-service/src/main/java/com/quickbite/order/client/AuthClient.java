package com.quickbite.order.client;

import com.quickbite.order.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service", url = "http://localhost:8081")
public interface AuthClient {

    @GetMapping("/api/auth/internal/users/{userId}")
    UserDTO getUserById(@PathVariable("userId") Long userId);
}