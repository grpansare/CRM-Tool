package com.crmplatform.sales.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "deal_stage_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealStageHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;
    
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    
    @Column(name = "deal_id", nullable = false)
    private Long dealId;
    
    @Column(name = "from_stage_id")
    private Long fromStageId;
    
    @Column(name = "to_stage_id")
    private Long toStageId;
    
    @CreationTimestamp
    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;
    
    @Column(name = "time_in_previous_stage_days")
    private Integer timeInPreviousStageDays;
} 