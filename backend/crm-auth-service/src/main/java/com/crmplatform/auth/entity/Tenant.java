package com.crmplatform.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tenants")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tenant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tenant_id")
    private Long tenantId;
    
    @Column(name = "tenant_name", nullable = false, length = 255)
    private String tenantName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_plan", length = 50)
    private SubscriptionPlan subscriptionPlan;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
    
    @Column(name = "max_users")
    private Integer maxUsers;
    
    @Column(name = "current_users")
    private Integer currentUsers;
    
    @Column(name = "trial_ends_at")
    private LocalDateTime trialEndsAt;
    
    @Column(name = "subscription_ends_at")
    private LocalDateTime subscriptionEndsAt;
    
    @Column(name = "company_name", length = 255)
    private String companyName;
    
    @Column(name = "company_address", length = 500)
    private String companyAddress;
    
    @Column(name = "company_phone", length = 50)
    private String companyPhone;
    
    @Column(name = "company_email", length = 255)
    private String companyEmail;
    
    @Column(name = "industry", length = 100)
    private String industry;
    
    @Column(name = "timezone", length = 50)
    private String timezone;
    
    @Column(name = "locale", length = 10)
    private String locale;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    public enum SubscriptionPlan {
        FREE,
        STARTER,
        PROFESSIONAL,
        ENTERPRISE,
        CUSTOM
    }
} 