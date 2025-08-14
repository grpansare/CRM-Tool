package com.crmplatform.contacts.dto;

import jakarta.validation.constraints.Email;
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
public class CreateContactRequest {
    
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;
    
    @Email(message = "Primary email must be a valid email address")
    @Size(max = 255, message = "Primary email must not exceed 255 characters")
    private String primaryEmail;
    
    @Size(max = 50, message = "Phone number must not exceed 50 characters")
    private String phoneNumber;
    
    @Size(max = 100, message = "Job title must not exceed 100 characters")
    private String jobTitle;
    
    @Size(max = 255, message = "Account name must not exceed 255 characters")
    private String accountName;
    
    private Map<String, String> customFields;
} 