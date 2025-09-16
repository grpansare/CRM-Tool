package com.crmplatform.contacts.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;
    
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    
    @Column(name = "account_name", nullable = false, length = 255)
    private String accountName;
    
    @Column(name = "website", length = 255)
    private String website;
    
    @Column(name = "industry", length = 100)
    private String industry;
    
    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId;
    
    // Account hierarchy support
    @Column(name = "parent_account_id")
    private Long parentAccountId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", length = 50)
    private AccountType accountType;
    
    // Revenue tracking fields
    @Column(name = "annual_revenue")
    private java.math.BigDecimal annualRevenue;
    
    @Column(name = "employee_count")
    private Integer employeeCount;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}