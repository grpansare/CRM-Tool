package com.crmplatform.sales.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDealRequest {
    
    @NotBlank(message = "Deal name is required")
    @Size(max = 255, message = "Deal name must not exceed 255 characters")
    private String dealName;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    private LocalDate expectedCloseDate;
    
    @NotNull(message = "Stage ID is required")
    private Long stageId;
    
    @NotNull(message = "Contact ID is required")
    private Long contactId;
    
    @NotNull(message = "Account ID is required")
    private Long accountId;
} 