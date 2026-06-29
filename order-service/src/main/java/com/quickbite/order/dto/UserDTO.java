package com.quickbite.order.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long userId;
    private String email;
    private String phone;
    private String fullName;
    private String role;
}