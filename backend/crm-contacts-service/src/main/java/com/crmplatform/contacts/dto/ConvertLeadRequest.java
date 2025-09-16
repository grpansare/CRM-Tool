package com.crmplatform.contacts.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConvertLeadRequest {
    
    @NotNull(message = "Lead ID is required")
    private Long leadId;
    
    private boolean createAccount;
    
    private String accountName;
    
    private String accountWebsite;
    
    private String accountIndustry;
    
    private String notes;
}
