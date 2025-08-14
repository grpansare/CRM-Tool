package com.crmplatform.sales.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "pipeline_stages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineStage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stage_id")
    private Long stageId;
    
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    
    @Column(name = "pipeline_id", nullable = false)
    private Long pipelineId;
    
    @Column(name = "stage_name", nullable = false, length = 100)
    private String stageName;
    
    @Column(name = "stage_order", nullable = false)
    private Integer stageOrder;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "stage_type", length = 10)
    private StageType stageType;
    
    @Column(name = "win_probability", precision = 5, scale = 2)
    private BigDecimal winProbability;
    
    public enum StageType {
        OPEN, WON, LOST
    }
} 