package com.crmplatform.contacts.dto;

import com.crmplatform.contacts.entity.Lead;
import jakarta.validation.constraints.Email;
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
public class CreateLeadRequest {
    
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;
    
    @Email(message = "Email should be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;
    
    @Size(max = 50, message = "Phone number must not exceed 50 characters")
    private String phoneNumber;
    
    @Size(max = 255, message = "Company must not exceed 255 characters")
    private String company;
    
    @Size(max = 100, message = "Job title must not exceed 100 characters")
    private String jobTitle;
    
    private Lead.LeadSource leadSource;
    
    private Lead.LeadStatus leadStatus;
    
    private Integer leadScore;
    
    @Size(max = 100, message = "Industry must not exceed 100 characters")
    private String industry;
    
    @Size(max = 255, message = "Website must not exceed 255 characters")
    private String website;
    
    private Long annualRevenue;
    
    private Integer employeeCount;
    
    private String notes;
}
