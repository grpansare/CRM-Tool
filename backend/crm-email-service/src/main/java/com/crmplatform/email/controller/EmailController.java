package com.crmplatform.email.controller;

import com.crmplatform.email.dto.EmailRequest;
import com.crmplatform.email.dto.EmailTemplateRequest;
import com.crmplatform.email.dto.SendEmailRequest;
import com.crmplatform.email.entity.EmailTemplate;
import com.crmplatform.email.service.EmailService;
import com.crmplatform.email.service.EmailTemplateService;
import com.crmplatform.email.service.EmailWorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
@Slf4j
public class EmailController {

    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;
    private final EmailWorkflowService emailWorkflowService;

    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendEmail(@Valid @RequestBody EmailRequest emailRequest) {
        log.info("Received email request for tenant: {} to: {}", emailRequest.getTenantId(), emailRequest.getToEmail());
        
        boolean success = emailService.sendEmail(emailRequest);
        
        return ResponseEntity.ok(Map.of(
                "success", success,
                "message", success ? "Email sent successfully" : "Failed to send email"
        ));
    }

    @PostMapping("/send-async")
    public ResponseEntity<Map<String, Object>> sendEmailAsync(@Valid @RequestBody EmailRequest emailRequest) {
        log.info("Received async email request for tenant: {} to: {}", emailRequest.getTenantId(), emailRequest.getToEmail());
        
        emailService.sendEmailAsync(emailRequest);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Email queued for sending"
        ));
    }

    @PostMapping("/welcome")
    public ResponseEntity<Map<String, Object>> sendWelcomeEmail(
            @RequestParam String tenantId,
            @RequestParam String toEmail,
            @RequestParam String userName,
            @RequestParam String tenantName) {
        
        log.info("Sending welcome email for tenant: {} to: {}", tenantId, toEmail);
        
        emailService.sendWelcomeEmailAsync(tenantId, toEmail, userName, tenantName);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Welcome email queued for sending"
        ));
    }

    @PostMapping("/invitation")
    public ResponseEntity<Map<String, Object>> sendInvitationEmail(
            @RequestParam String tenantId,
            @RequestParam String toEmail,
            @RequestParam String inviteUrl,
            @RequestParam String role) {

        log.info("Sending invitation email for tenant: {} to: {}", tenantId, toEmail);

        emailService.sendInvitationEmailAsync(tenantId, toEmail, inviteUrl, role);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Invitation email queued for sending"
        ));
    }

    // Email Template Management
    @PostMapping("/templates")
    public ResponseEntity<EmailTemplate> createTemplate(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @Valid @RequestBody EmailTemplateRequest request) {
        
        log.info("Creating email template: {} for tenant: {}", request.getTemplateName(), tenantId);
        
        EmailTemplate template = emailTemplateService.createTemplate(tenantId, request);
        return ResponseEntity.ok(template);
    }

    @GetMapping("/templates")
    public ResponseEntity<List<EmailTemplate>> getTemplates(@RequestHeader("X-Tenant-Id") String tenantId) {
        log.info("Getting email templates for tenant: {}", tenantId);
        
        List<EmailTemplate> templates = emailTemplateService.getTemplatesByTenant(tenantId);
        return ResponseEntity.ok(templates);
    }

    @PutMapping("/templates/{templateId}")
    public ResponseEntity<EmailTemplate> updateTemplate(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @PathVariable Long templateId,
            @Valid @RequestBody EmailTemplateRequest request) {
        
        log.info("Updating email template: {} for tenant: {}", templateId, tenantId);
        
        EmailTemplate template = emailTemplateService.updateTemplate(tenantId, templateId, request);
        return ResponseEntity.ok(template);
    }

    @DeleteMapping("/templates/{templateId}")
    public ResponseEntity<Map<String, Object>> deleteTemplate(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @PathVariable Long templateId) {
        
        log.info("Deleting email template: {} for tenant: {}", templateId, tenantId);
        
        emailTemplateService.deleteTemplate(tenantId, templateId);
        return ResponseEntity.ok(Map.of("success", true, "message", "Template deleted successfully"));
    }

    // Workflow Email Endpoints
    @PostMapping("/send-templated")
    public ResponseEntity<Map<String, Object>> sendTemplatedEmail(@Valid @RequestBody SendEmailRequest request) {
        log.info("Sending templated email using template: {} to: {}", request.getTemplateName(), request.getToEmail());
        
        boolean success = emailWorkflowService.sendTemplatedEmail(request);
        
        return ResponseEntity.ok(Map.of(
                "success", success,
                "message", success ? "Templated email sent successfully" : "Failed to send templated email"
        ));
    }

    @PostMapping("/follow-up")
    public ResponseEntity<Map<String, Object>> sendFollowUpEmail(
            @RequestParam String tenantId,
            @RequestParam Long contactId,
            @RequestParam Long dealId,
            @RequestParam Long userId,
            @RequestParam String contactName,
            @RequestParam String dealName,
            @RequestParam String customMessage) {
        
        log.info("Sending follow-up email for deal: {} to contact: {}", dealId, contactId);
        
        boolean success = emailWorkflowService.sendFollowUpEmail(
                tenantId, contactId, dealId, userId, contactName, dealName, customMessage);
        
        return ResponseEntity.ok(Map.of(
                "success", success,
                "message", success ? "Follow-up email sent successfully" : "Failed to send follow-up email"
        ));
    }

    @PostMapping("/proposal")
    public ResponseEntity<Map<String, Object>> sendProposalEmail(
            @RequestParam String tenantId,
            @RequestParam Long contactId,
            @RequestParam Long dealId,
            @RequestParam Long userId,
            @RequestParam String contactName,
            @RequestParam String dealName,
            @RequestParam String dealAmount,
            @RequestParam String proposalDetails) {
        
        log.info("Sending proposal email for deal: {} to contact: {}", dealId, contactId);
        
        boolean success = emailWorkflowService.sendProposalEmail(
                tenantId, contactId, dealId, userId, contactName, dealName, dealAmount, proposalDetails);
        
        return ResponseEntity.ok(Map.of(
                "success", success,
                "message", success ? "Proposal email sent successfully" : "Failed to send proposal email"
        ));
    }

    @PostMapping("/meeting-reminder")
    public ResponseEntity<Map<String, Object>> sendMeetingReminder(
            @RequestParam String tenantId,
            @RequestParam Long contactId,
            @RequestParam Long dealId,
            @RequestParam Long userId,
            @RequestParam String contactName,
            @RequestParam String dealName,
            @RequestParam String meetingDate,
            @RequestParam String meetingTime,
            @RequestParam String meetingLocation) {
        
        log.info("Sending meeting reminder for deal: {} to contact: {}", dealId, contactId);
        
        boolean success = emailWorkflowService.sendMeetingReminder(
                tenantId, contactId, dealId, userId, contactName, dealName, 
                meetingDate, meetingTime, meetingLocation);
        
        return ResponseEntity.ok(Map.of(
                "success", success,
                "message", success ? "Meeting reminder sent successfully" : "Failed to send meeting reminder"
        ));
    }
}
