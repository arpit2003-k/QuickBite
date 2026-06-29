package com.quickbite.auth.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserDTO {
    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private String profilePicUrl;
    private Boolean isActive;
    private LocalDateTime createdAt;
}