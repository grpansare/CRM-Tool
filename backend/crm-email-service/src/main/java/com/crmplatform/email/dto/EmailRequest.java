package com.crmplatform.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {
    
    @NotBlank(message = "Tenant ID is required")
    private String tenantId;
    
    @NotBlank(message = "To email is required")
    @Email(message = "Invalid email format")
    private String toEmail;
    
    @Email(message = "Invalid from email format")
    private String fromEmail;
    
    @Email(message = "Invalid CC email format")
    private String ccEmail;
    
    @Email(message = "Invalid BCC email format")
    private String bccEmail;
    
    @NotBlank(message = "Subject is required")
    private String subject;
    
    private String content;
    
    private String templateName;
    
    private Map<String, Object> templateVariables;
    
    @NotNull(message = "Email type is required")
    private EmailType emailType;
    
    private Boolean isHtml = true;
    
    public enum EmailType {
        WELCOME,
        NOTIFICATION,
        MARKETING,
        SYSTEM_ALERT,
        PASSWORD_RESET,
        VERIFICATION
    }
}
