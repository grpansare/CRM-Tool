package com.crmplatform.auth.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class TenantRegistrationRequest {
    
    @NotBlank(message = "Tenant name is required")
    @Size(min = 2, max = 255, message = "Tenant name must be between 2 and 255 characters")
    private String tenantName;
    
    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 255, message = "Company name must be between 2 and 255 characters")
    private String companyName;
    
    @NotBlank(message = "Admin first name is required")
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    private String adminFirstName;
    
    @NotBlank(message = "Admin last name is required")
    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    private String adminLastName;
    
    @NotBlank(message = "Admin email is required")
    @Email(message = "Invalid email format")
    private String adminEmail;
    
    @NotBlank(message = "Admin username is required")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String adminUsername;
    
    @NotBlank(message = "Admin password is required")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", 
            message = "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one number and one special character")
    private String adminPassword;
    
    @Size(max = 500, message = "Company address must not exceed 500 characters")
    private String companyAddress;
    
    @Pattern(regexp = "^[+]?[0-9\\s\\-()]+$", message = "Invalid phone number format")
    @Size(max = 50, message = "Phone number must not exceed 50 characters")
    private String companyPhone;
    
    @Email(message = "Invalid company email format")
    private String companyEmail;
    
    @Size(max = 100, message = "Industry must not exceed 100 characters")
    private String industry;
    
    @NotNull(message = "You must accept the terms and conditions")
    private Boolean acceptTerms;
    
    @Size(max = 50, message = "Timezone must not exceed 50 characters")
    private String timezone;
    
    @Pattern(regexp = "^[a-z]{2}(-[A-Z]{2})?$", message = "Invalid locale format (e.g., en, en-US)")
    @Size(max = 10, message = "Locale must not exceed 10 characters")
    private String locale;
    
    private String subscriptionPlan;
    
    private Boolean acceptMarketing;
} 