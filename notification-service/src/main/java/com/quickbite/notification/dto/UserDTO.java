package com.quickbite.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private Long userId;
    private String email;
    private String fullName;
    private String phone;
    private String role;
    private boolean isActive;
}
