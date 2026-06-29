package com.quickbite.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 4, message = "New password must be at least 4 characters long")
    private String newPassword;
}
