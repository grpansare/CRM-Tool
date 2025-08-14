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
public class PipelineResponse {
    
    private Long pipelineId;
    private String pipelineName;
    private List<PipelineStageResponse> stages;
    private BigDecimal totalPipelineValue;
    private Integer totalDeals;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PipelineStageResponse {
        private Long stageId;
        private String stageName;
        private Integer stageOrder;
        private String stageType;
        private BigDecimal winProbability;
        private List<DealSummary> deals;
        private BigDecimal stageValue;
        private Integer dealCount;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DealSummary {
        private Long dealId;
        private String dealName;
        private BigDecimal amount;
        private Long ownerUserId;
    }
} 