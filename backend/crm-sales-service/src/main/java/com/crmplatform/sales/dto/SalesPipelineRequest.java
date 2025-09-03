package com.crmplatform.sales.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesPipelineRequest {
    
    @NotBlank(message = "Pipeline name is required")
    @Size(max = 100, message = "Pipeline name must not exceed 100 characters")
    private String pipelineName;
}
