package com.crmplatform.email.dto;

import com.crmplatform.email.entity.EmailLog;
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
public class SendEmailRequest {
    
    @NotBlank(message = "Tenant ID is required")
    private String tenantId;
    
    @NotBlank(message = "To email is required")
    @Email(message = "Invalid email format")
    private String toEmail;
    
    @Email(message = "Invalid CC email format")
    private String ccEmail;
    
    @Email(message = "Invalid BCC email format")
    private String bccEmail;
    
    @NotBlank(message = "Subject is required")
    private String subject;
    
    private String content;
    
    @NotNull(message = "Email type is required")
    private EmailLog.EmailType emailType;
    
    // Template-based email
    private String templateName;
    private Map<String, Object> templateVariables;
    
    // CRM associations
    private Long contactId;
    private Long dealId;
    private Long accountId;
    private Long userId;
    
    // Auto-log to activity service
    private Boolean logAsActivity = true;
}
