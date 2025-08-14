package com.crmplatform.sales.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDealStageRequest {
    
    @NotNull(message = "New stage ID is required")
    private Long newStageId;
} 