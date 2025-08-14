package com.crmplatform.sales.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "deals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Deal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deal_id")
    private Long dealId;
    
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    
    @Column(name = "deal_name", nullable = false, length = 255)
    private String dealName;
    
    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "expected_close_date")
    private LocalDate expectedCloseDate;
    
    @Column(name = "stage_id", nullable = false)
    private Long stageId;
    
    @Column(name = "contact_id", nullable = false)
    private Long contactId;
    
    @Column(name = "account_id", nullable = false)
    private Long accountId;
    
    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
} 