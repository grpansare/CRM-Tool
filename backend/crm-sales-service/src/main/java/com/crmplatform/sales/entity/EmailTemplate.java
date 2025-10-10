package com.crmplatform.sales.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "email_templates")
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmailTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Long templateId;
    
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    
    @Column(name = "template_name", nullable = false)
    private String templateName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "template_type", nullable = false)
    private EmailTemplateType templateType;
    
    @Column(name = "subject_line", nullable = false, length = 500)
    private String subjectLine;
    
    @Column(name = "email_body", nullable = false, columnDefinition = "TEXT")
    private String emailBody;
    
    @Column(name = "html_body", columnDefinition = "TEXT")
    private String htmlBody;
    
    @Column(name = "available_variables", columnDefinition = "JSON")
    private String availableVariables;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "is_default")
    private Boolean isDefault = false;
    
    @Column(name = "created_by", nullable = false)
    private Long createdBy;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Helper methods for JSON fields
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public List<String> getAvailableVariablesList() {
        if (availableVariables == null || availableVariables.isEmpty()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(availableVariables, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
    
    public void setAvailableVariablesList(List<String> variables) {
        try {
            this.availableVariables = objectMapper.writeValueAsString(variables);
        } catch (Exception e) {
            this.availableVariables = "[]";
        }
    }
    
    // Template processing method
    public String processTemplate(Map<String, String> variables) {
        String processedBody = emailBody;
        String processedSubject = subjectLine;
        
        if (variables != null) {
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                String placeholder = "{{" + entry.getKey() + "}}";
                String value = entry.getValue() != null ? entry.getValue() : "";
                
                processedBody = processedBody.replace(placeholder, value);
                processedSubject = processedSubject.replace(placeholder, value);
            }
        }
        
        return processedBody;
    }
    
    public String processSubject(Map<String, String> variables) {
        String processedSubject = subjectLine;
        
        if (variables != null) {
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                String placeholder = "{{" + entry.getKey() + "}}";
                String value = entry.getValue() != null ? entry.getValue() : "";
                processedSubject = processedSubject.replace(placeholder, value);
            }
        }
        
        return processedSubject;
    }
    
    // Constructors
    public EmailTemplate() {}
    
    public EmailTemplate(String templateName, EmailTemplateType templateType, String subjectLine, 
                        String emailBody, Long tenantId, Long createdBy) {
        this.templateName = templateName;
        this.templateType = templateType;
        this.subjectLine = subjectLine;
        this.emailBody = emailBody;
        this.tenantId = tenantId;
        this.createdBy = createdBy;
    }
    
    // Getters and Setters
    public Long getTemplateId() {
        return templateId;
    }
    
    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }
    
    public Long getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
    
    public String getTemplateName() {
        return templateName;
    }
    
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }
    
    public EmailTemplateType getTemplateType() {
        return templateType;
    }
    
    public void setTemplateType(EmailTemplateType templateType) {
        this.templateType = templateType;
    }
    
    public String getSubjectLine() {
        return subjectLine;
    }
    
    public void setSubjectLine(String subjectLine) {
        this.subjectLine = subjectLine;
    }
    
    public String getEmailBody() {
        return emailBody;
    }
    
    public void setEmailBody(String emailBody) {
        this.emailBody = emailBody;
    }
    
    public String getHtmlBody() {
        return htmlBody;
    }
    
    public void setHtmlBody(String htmlBody) {
        this.htmlBody = htmlBody;
    }
    
    public String getAvailableVariables() {
        return availableVariables;
    }
    
    public void setAvailableVariables(String availableVariables) {
        this.availableVariables = availableVariables;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public Boolean getIsDefault() {
        return isDefault;
    }
    
    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
    
    public Long getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public Long getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

enum EmailTemplateType {
    LEAD_WELCOME,
    FOLLOW_UP,
    NURTURE,
    MEETING_INVITE,
    PROPOSAL,
    CUSTOM
}
