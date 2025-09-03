package com.crmplatform.sales.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineStageRequest {
    
    @NotBlank(message = "Stage name is required")
    @Size(max = 100, message = "Stage name must not exceed 100 characters")
    private String stageName;
    
    @NotNull(message = "Stage order is required")
    private Integer stageOrder;
    
    @NotNull(message = "Stage type is required")
    private String stageType; // OPEN, WON, LOST
    
    @DecimalMin(value = "0.0", message = "Win probability must be between 0 and 100")
    @DecimalMax(value = "100.0", message = "Win probability must be between 0 and 100")
    private BigDecimal winProbability;
}
