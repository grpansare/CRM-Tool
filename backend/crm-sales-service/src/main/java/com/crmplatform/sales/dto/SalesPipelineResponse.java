package com.crmplatform.sales.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesPipelineResponse {
    
    private Long pipelineId;
    private String pipelineName;
    private List<PipelineStageResponse> stages;
    private Integer totalDeals;
    private Long totalValue;
}
