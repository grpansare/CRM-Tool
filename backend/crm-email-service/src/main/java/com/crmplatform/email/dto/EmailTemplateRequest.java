package com.crmplatform.email.dto;

import com.crmplatform.email.entity.EmailTemplate;
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
public class EmailTemplateRequest {
    
    @NotBlank(message = "Template name is required")
    private String templateName;
    
    @NotBlank(message = "Subject is required")
    private String subject;
    
    @NotBlank(message = "HTML content is required")
    private String htmlContent;
    
    private String textContent;
    
    @NotNull(message = "Template type is required")
    private EmailTemplate.TemplateType templateType;
    
    private Boolean isActive = true;
    
    // Variables for template processing
    private Map<String, Object> variables;
}
