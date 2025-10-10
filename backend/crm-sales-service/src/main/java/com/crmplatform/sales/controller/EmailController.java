//package com.crmplatform.sales.controller;
//
//import com.crmplatform.sales.entity.EmailTemplate;
//import com.crmplatform.sales.entity.EmailTemplateType;
//import com.crmplatform.sales.entity.EmailSentHistory;
//import com.crmplatform.sales.service.CrmEmailService;
//import com.crmplatform.common.dto.ApiResponse;
//import jakarta.validation.Valid;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/v1/emails")
//public class EmailController {
//    
//    @Autowired
//    private CrmEmailService emailService;
//    
//    /**
//     * Send email using template to a lead
//     */
//    @PostMapping("/send/lead/{leadId}")
//    public ResponseEntity<ApiResponse<Long>> sendEmailToLead(
//            @PathVariable Long leadId,
//            @RequestBody SendEmailRequest request) {
//        try {
//            Long emailId = emailService.sendEmailToLead(
//                leadId, 
//                request.getTemplateId(), 
//                request.getCustomVariables()
//            );
//            
//            return ResponseEntity.ok(ApiResponse.success(emailId, "Email sent successfully"));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(ApiResponse.error("Failed to send email: " + e.getMessage()));
//        }
//    }
//    
//    /**
//     * Send custom email to a lead
//     */
//    @PostMapping("/send/lead/{leadId}/custom")
//    public ResponseEntity<ApiResponse<Long>> sendCustomEmailToLead(
//            @PathVariable Long leadId,
//            @RequestBody SendCustomEmailRequest request) {
//        try {
//            Long emailId = emailService.sendCustomEmailToLead(
//                leadId,
//                request.getSubject(),
//                request.getBody(),
//                request.getHtmlBody()
//            );
//            
//            return ResponseEntity.ok(ApiResponse.success(emailId, "Email sent successfully"));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(ApiResponse.error("Failed to send email: " + e.getMessage()));
//        }
//    }
//    
//    /**
//     * Send email to any recipient
//     */
//    @PostMapping("/send")
//    public ResponseEntity<ApiResponse<Long>> sendEmail(@RequestBody SendEmailToRecipientRequest request) {
//        try {
//            Long emailId = emailService.sendEmail(
//                request.getRecipientEmail(),
//                request.getSubject(),
//                request.getBody(),
//                request.getHtmlBody(),
//                request.getLeadId(),
//                request.getContactId(),
//                request.getTemplateId()
//            );
//            
//            return ResponseEntity.ok(ApiResponse.success(emailId, "Email sent successfully"));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(ApiResponse.error("Failed to send email: " + e.getMessage()));
//        }
//    }
//    
//    /**
//     * Get all email templates
//     */
//    @GetMapping("/templates")
//    public ResponseEntity<ApiResponse<List<EmailTemplate>>> getAllTemplates() {
//        try {
//            List<EmailTemplate> templates = emailService.getAllActiveTemplates();
//            return ResponseEntity.ok(ApiResponse.success(templates));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(ApiResponse.error("Failed to fetch templates: " + e.getMessage()));
//        }
//    }
//    
//    /**
//     * Get templates by type
//     */
//    @GetMapping("/templates/type/{type}")
//    public ResponseEntity<ApiResponse<List<EmailTemplate>>> getTemplatesByType(@PathVariable EmailTemplateType type) {
//        try {
//            List<EmailTemplate> templates = emailService.getTemplatesByType(type);
//            return ResponseEntity.ok(ApiResponse.success(templates));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(ApiResponse.error("Failed to fetch templates: " + e.getMessage()));
//        }
//    }
//    
//    /**
//     * Get default template for a type
//     */
//    @GetMapping("/templates/default/{type}")
//    public ResponseEntity<ApiResponse<EmailTemplate>> getDefaultTemplate(@PathVariable EmailTemplateType type) {
//        try {
//            return emailService.getDefaultTemplate(type)
//                .map(template -> ResponseEntity.ok(ApiResponse.success(template)))
//                .orElse(ResponseEntity.ok(ApiResponse.success(null, "No default template found")));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(ApiResponse.error("Failed to fetch default template: " + e.getMessage()));
//        }
//    }
//    
//    /**
//     * Create new email template
//     */
//    @PostMapping("/templates")
//    public ResponseEntity<ApiResponse<EmailTemplate>> createTemplate(@Valid @RequestBody CreateEmailTemplateRequest request) {
//        try {
//            EmailTemplate template = emailService.createTemplate(
//                request.getTemplateName(),
//                request.getTemplateType(),
//                request.getSubjectLine(),
//                request.getEmailBody(),
//                request.getHtmlBody(),
//                request.getAvailableVariables()
//            );
//            
//            return ResponseEntity.status(HttpStatus.CREATED)
//                .body(ApiResponse.success(template));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(ApiResponse.error("Failed to create template: " + e.getMessage()));
//        }
//    }
//    
//    /**
//     * Update email template
//     */
//    @PutMapping("/templates/{templateId}")
//    public ResponseEntity<ApiResponse<EmailTemplate>> updateTemplate(
//            @PathVariable Long templateId,
//            @Valid @RequestBody UpdateEmailTemplateRequest request) {
//        try {
//            EmailTemplate template = emailService.updateTemplate(
//                templateId,
//                request.getTemplateName(),
//                request.getSubjectLine(),
//                request.getEmailBody(),
//                request.getHtmlBody(),
//                request.getAvailableVariables()
//            );
//            
//            return ResponseEntity.ok(ApiResponse.success(template));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(ApiResponse.error("Failed to update template: " + e.getMessage()));
//        }
//    }
//    
//    /**
//     * Get email history for a lead
//     */
//    @GetMapping("/history/lead/{leadId}")
//    public ResponseEntity<ApiResponse<List<EmailSentHistory>>> getEmailHistoryForLead(@PathVariable Long leadId) {
//        try {
//            List<EmailSentHistory> history = emailService.getEmailHistoryForLead(leadId);
//            return ResponseEntity.ok(ApiResponse.success(history));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(ApiResponse.error("Failed to fetch email history: " + e.getMessage()));
//        }
//    }
//    
//    /**
//     * Get email sending statistics
//     */
//    @GetMapping("/statistics")
//    public ResponseEntity<ApiResponse<Map<String, Object>>> getEmailStatistics() {
//        try {
//            Map<String, Object> stats = emailService.getEmailStatistics();
//            return ResponseEntity.ok(ApiResponse.success(stats));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(ApiResponse.error("Failed to fetch email statistics: " + e.getMessage()));
//        }
//    }
//    
//    /**
//     * Send welcome email to lead
//     */
//    @PostMapping("/send/welcome/lead/{leadId}")
//    public ResponseEntity<ApiResponse<Void>> sendWelcomeEmail(@PathVariable Long leadId) {
//        try {
//            // This is async, so we don't wait for completion
//            emailService.sendWelcomeEmail(
//                // Need to fetch lead - this is a simplified version
//                // In production, you'd inject LeadService here
//                null
//            );
//            
//            return ResponseEntity.ok(ApiResponse.success(null, "Welcome email queued for sending"));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(ApiResponse.error("Failed to queue welcome email: " + e.getMessage()));
//        }
//    }
//    
//    /**
//     * Get available template types
//     */
//    @GetMapping("/template-types")
//    public ResponseEntity<ApiResponse<EmailTemplateType[]>> getTemplateTypes() {
//        try {
//            EmailTemplateType[] types = EmailTemplateType.values();
//            return ResponseEntity.ok(ApiResponse.success(types));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(ApiResponse.error("Failed to fetch template types: " + e.getMessage()));
//        }
//    }
//}
//
//// Request DTOs
//class SendEmailRequest {
//    private Long templateId;
//    private Map<String, String> customVariables;
//    
//    // Getters and setters
//    public Long getTemplateId() { return templateId; }
//    public void setTemplateId(Long templateId) { this.templateId = templateId; }
//    public Map<String, String> getCustomVariables() { return customVariables; }
//    public void setCustomVariables(Map<String, String> customVariables) { this.customVariables = customVariables; }
//}
//
//class SendCustomEmailRequest {
//    private String subject;
//    private String body;
//    private String htmlBody;
//    
//    // Getters and setters
//    public String getSubject() { return subject; }
//    public void setSubject(String subject) { this.subject = subject; }
//    public String getBody() { return body; }
//    public void setBody(String body) { this.body = body; }
//    public String getHtmlBody() { return htmlBody; }
//    public void setHtmlBody(String htmlBody) { this.htmlBody = htmlBody; }
//}
//
//class SendEmailToRecipientRequest {
//    private String recipientEmail;
//    private String subject;
//    private String body;
//    private String htmlBody;
//    private Long leadId;
//    private Long contactId;
//    private Long templateId;
//    
//    // Getters and setters
//    public String getRecipientEmail() { return recipientEmail; }
//    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }
//    public String getSubject() { return subject; }
//    public void setSubject(String subject) { this.subject = subject; }
//    public String getBody() { return body; }
//    public void setBody(String body) { this.body = body; }
//    public String getHtmlBody() { return htmlBody; }
//    public void setHtmlBody(String htmlBody) { this.htmlBody = htmlBody; }
//    public Long getLeadId() { return leadId; }
//    public void setLeadId(Long leadId) { this.leadId = leadId; }
//    public Long getContactId() { return contactId; }
//    public void setContactId(Long contactId) { this.contactId = contactId; }
//    public Long getTemplateId() { return templateId; }
//    public void setTemplateId(Long templateId) { this.templateId = templateId; }
//}
//
//class CreateEmailTemplateRequest {
//    private String templateName;
//    private EmailTemplateType templateType;
//    private String subjectLine;
//    private String emailBody;
//    private String htmlBody;
//    private List<String> availableVariables;
//    
//    // Getters and setters
//    public String getTemplateName() { return templateName; }
//    public void setTemplateName(String templateName) { this.templateName = templateName; }
//    public EmailTemplateType getTemplateType() { return templateType; }
//    public void setTemplateType(EmailTemplateType templateType) { this.templateType = templateType; }
//    public String getSubjectLine() { return subjectLine; }
//    public void setSubjectLine(String subjectLine) { this.subjectLine = subjectLine; }
//    public String getEmailBody() { return emailBody; }
//    public void setEmailBody(String emailBody) { this.emailBody = emailBody; }
//    public String getHtmlBody() { return htmlBody; }
//    public void setHtmlBody(String htmlBody) { this.htmlBody = htmlBody; }
//    public List<String> getAvailableVariables() { return availableVariables; }
//    public void setAvailableVariables(List<String> availableVariables) { this.availableVariables = availableVariables; }
//}
//
//class UpdateEmailTemplateRequest {
//    private String templateName;
//    private String subjectLine;
//    private String emailBody;
//    private String htmlBody;
//    private List<String> availableVariables;
//    
//    // Getters and setters
//    public String getTemplateName() { return templateName; }
//    public void setTemplateName(String templateName) { this.templateName = templateName; }
//    public String getSubjectLine() { return subjectLine; }
//    public void setSubjectLine(String subjectLine) { this.subjectLine = subjectLine; }
//    public String getEmailBody() { return emailBody; }
//    public void setEmailBody(String emailBody) { this.emailBody = emailBody; }
//    public String getHtmlBody() { return htmlBody; }
//    public void setHtmlBody(String htmlBody) { this.htmlBody = htmlBody; }
//    public List<String> getAvailableVariables() { return availableVariables; }
//    public void setAvailableVariables(List<String> availableVariables) { this.availableVariables = availableVariables; }
//}
