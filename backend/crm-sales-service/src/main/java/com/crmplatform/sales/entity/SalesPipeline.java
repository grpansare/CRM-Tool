package com.crmplatform.sales.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sales_pipelines")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesPipeline {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pipeline_id")
    private Long pipelineId;
    
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    
    @Column(name = "pipeline_name", nullable = false, length = 100)
    private String pipelineName;
} 