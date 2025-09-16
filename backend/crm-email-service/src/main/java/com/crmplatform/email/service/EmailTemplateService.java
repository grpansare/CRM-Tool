package com.crmplatform.email.service;

import com.crmplatform.email.dto.EmailTemplateRequest;
import com.crmplatform.email.entity.EmailTemplate;
import com.crmplatform.email.repository.EmailTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailTemplateService {

    private final EmailTemplateRepository emailTemplateRepository;
    
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");

    public EmailTemplate createTemplate(String tenantId, EmailTemplateRequest request) {
        log.info("Creating email template: {} for tenant: {}", request.getTemplateName(), tenantId);
        
        EmailTemplate template = EmailTemplate.builder()
                .tenantId(tenantId)
                .templateName(request.getTemplateName())
                .subject(request.getSubject())
                .htmlContent(request.getHtmlContent())
                .textContent(request.getTextContent())
                .templateType(request.getTemplateType())
                .isActive(request.getIsActive())
                .build();
        
        return emailTemplateRepository.save(template);
    }

    public List<EmailTemplate> getTemplatesByTenant(String tenantId) {
        return emailTemplateRepository.findByTenantIdAndIsActiveTrue(tenantId);
    }

    public Optional<EmailTemplate> getTemplateByName(String tenantId, String templateName) {
        return emailTemplateRepository.findByTenantIdAndTemplateNameAndIsActiveTrue(tenantId, templateName);
    }

    public EmailTemplate updateTemplate(String tenantId, Long templateId, EmailTemplateRequest request) {
        log.info("Updating email template: {} for tenant: {}", templateId, tenantId);
        
        EmailTemplate template = emailTemplateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        
        if (!template.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Unauthorized access to template");
        }
        
        template.setTemplateName(request.getTemplateName());
        template.setSubject(request.getSubject());
        template.setHtmlContent(request.getHtmlContent());
        template.setTextContent(request.getTextContent());
        template.setTemplateType(request.getTemplateType());
        template.setIsActive(request.getIsActive());
        
        return emailTemplateRepository.save(template);
    }

    public void deleteTemplate(String tenantId, Long templateId) {
        log.info("Deleting email template: {} for tenant: {}", templateId, tenantId);
        
        EmailTemplate template = emailTemplateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        
        if (!template.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Unauthorized access to template");
        }
        
        template.setIsActive(false);
        emailTemplateRepository.save(template);
    }

    public String processTemplate(EmailTemplate template, Map<String, Object> variables) {
        if (variables == null || variables.isEmpty()) {
            return template.getHtmlContent();
        }
        
        String content = template.getHtmlContent();
        Matcher matcher = VARIABLE_PATTERN.matcher(content);
        
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String variableName = matcher.group(1);
            Object value = variables.get(variableName);
            String replacement = value != null ? value.toString() : "";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }

    public String processSubject(EmailTemplate template, Map<String, Object> variables) {
        if (variables == null || variables.isEmpty()) {
            return template.getSubject();
        }
        
        String subject = template.getSubject();
        Matcher matcher = VARIABLE_PATTERN.matcher(subject);
        
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String variableName = matcher.group(1);
            Object value = variables.get(variableName);
            String replacement = value != null ? value.toString() : "";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }

    public void createDefaultTemplates(String tenantId) {
        log.info("Creating default email templates for tenant: {}", tenantId);
        
        // Follow-up email template
        createDefaultTemplate(tenantId, "follow-up", "Follow up on {{dealName}}", 
                "<html><body>" +
                "<h2>Hi {{contactName}},</h2>" +
                "<p>I wanted to follow up on our discussion about {{dealName}}.</p>" +
                "<p>{{customMessage}}</p>" +
                "<p>Please let me know if you have any questions.</p>" +
                "<p>Best regards,<br/>{{senderName}}</p>" +
                "</body></html>", 
                EmailTemplate.TemplateType.FOLLOW_UP);

        // Proposal email template
        createDefaultTemplate(tenantId, "proposal", "Proposal for {{dealName}}", 
                "<html><body>" +
                "<h2>Hi {{contactName}},</h2>" +
                "<p>Thank you for your interest in our services. Please find attached our proposal for {{dealName}}.</p>" +
                "<p>The proposed solution includes:</p>" +
                "<ul>" +
                "<li>{{proposalDetails}}</li>" +
                "</ul>" +
                "<p>Total investment: {{dealAmount}}</p>" +
                "<p>I'm available to discuss this proposal at your convenience.</p>" +
                "<p>Best regards,<br/>{{senderName}}</p>" +
                "</body></html>", 
                EmailTemplate.TemplateType.PROPOSAL);

        // Meeting reminder template
        createDefaultTemplate(tenantId, "meeting-reminder", "Reminder: Meeting about {{dealName}}", 
                "<html><body>" +
                "<h2>Hi {{contactName}},</h2>" +
                "<p>This is a friendly reminder about our upcoming meeting:</p>" +
                "<p><strong>Topic:</strong> {{dealName}}</p>" +
                "<p><strong>Date:</strong> {{meetingDate}}</p>" +
                "<p><strong>Time:</strong> {{meetingTime}}</p>" +
                "<p><strong>Location:</strong> {{meetingLocation}}</p>" +
                "<p>Looking forward to speaking with you.</p>" +
                "<p>Best regards,<br/>{{senderName}}</p>" +
                "</body></html>", 
                EmailTemplate.TemplateType.MEETING_REMINDER);
    }

    private void createDefaultTemplate(String tenantId, String name, String subject, String content, EmailTemplate.TemplateType type) {
        if (!emailTemplateRepository.existsByTenantIdAndTemplateName(tenantId, name)) {
            EmailTemplate template = EmailTemplate.builder()
                    .tenantId(tenantId)
                    .templateName(name)
                    .subject(subject)
                    .htmlContent(content)
                    .templateType(type)
                    .isActive(true)
                    .build();
            emailTemplateRepository.save(template);
        }
    }
}
