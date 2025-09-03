package com.crmplatform.sales.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineStageResponse {
    
    private Long stageId;
    private String stageName;
    private Integer stageOrder;
    private String stageType;
    private BigDecimal winProbability;
    private List<DealResponse> deals;
    private Integer dealCount;
    private BigDecimal totalValue;
}
