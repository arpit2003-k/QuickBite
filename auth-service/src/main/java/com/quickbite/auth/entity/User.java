package com.quickbite.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false)
    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;   // BCrypt encoded

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;   // CUSTOMER, OWNER, AGENT, ADMIN

    private String provider;       // "LOCAL" or "GOOGLE"/"GITHUB"
    private String profilePicUrl;

    @Column(nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // Enum for roles
    public enum Role {
        CUSTOMER, RESTAURANT_OWNER, DELIVERY_AGENT, ADMIN
    }
}