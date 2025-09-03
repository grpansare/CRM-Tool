package com.crmplatform.contacts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {
    
    @NotBlank(message = "Account name is required")
    @Size(max = 255, message = "Account name must not exceed 255 characters")
    private String accountName;
    
    @Size(max = 255, message = "Website must not exceed 255 characters")
    private String website;
    
    @Size(max = 100, message = "Industry must not exceed 100 characters")
    private String industry;
    
    private Map<String, String> customFields;
}
