package com.crmplatform.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    
    @Column(name = "username", nullable = false, length = 100, unique = true)
    private String username;
    
    @Column(name = "email", nullable = false, length = 255, unique = true)
    private String email;
    
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    
    @Column(name = "first_name", length = 100)
    private String firstName;
    
    @Column(name = "last_name", length = 100)
    private String lastName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    private UserRole role;
    
    @Column(name = "manager_id")
    private Long managerId; // For hierarchy-based access control
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    public enum UserRole {
        SUPER_ADMIN,    // Can access all tenants
        TENANT_ADMIN,   // Can manage tenant settings
        SALES_MANAGER,  // Can see team's deals
        SALES_REP,      // Can only see own deals
        SUPPORT_AGENT,  // Can access contacts and activities
        READ_ONLY       // Can only view data
    }
} 