package com.crmplatform.email.service;

import com.crmplatform.email.dto.SendEmailRequest;
import com.crmplatform.email.entity.EmailLog;
import com.crmplatform.email.entity.EmailTemplate;
import com.crmplatform.email.repository.EmailLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailWorkflowService {

    private final EmailTemplateService emailTemplateService;
    private final EmailLogRepository emailLogRepository;
    private final EmailService emailService;
    private final RestTemplate restTemplate;

    public boolean sendTemplatedEmail(SendEmailRequest request) {
        log.info("Sending templated email using template: {} to: {}", request.getTemplateName(), request.getToEmail());
        
        try {
            // Get template
            Optional<EmailTemplate> templateOpt = emailTemplateService.getTemplateByName(
                    request.getTenantId(), request.getTemplateName());
            
            if (templateOpt.isEmpty()) {
                log.error("Template not found: {}", request.getTemplateName());
                return false;
            }
            
            EmailTemplate template = templateOpt.get();
            
            // Process template with variables
            String processedContent = emailTemplateService.processTemplate(template, request.getTemplateVariables());
            String processedSubject = emailTemplateService.processSubject(template, request.getTemplateVariables());
            
            // Create email log
            EmailLog emailLog = EmailLog.builder()
                    .tenantId(request.getTenantId())
                    .fromEmail("noreply@crmplatform.com") // Configure as needed
                    .toEmail(request.getToEmail())
                    .ccEmail(request.getCcEmail())
                    .bccEmail(request.getBccEmail())
                    .subject(processedSubject)
                    .content(processedContent)
                    .status(EmailLog.EmailStatus.PENDING)
                    .emailType(request.getEmailType())
                    .templateName(request.getTemplateName())
                    .contactId(request.getContactId())
                    .dealId(request.getDealId())
                    .accountId(request.getAccountId())
                    .userId(request.getUserId())
                    .build();
            
            emailLog = emailLogRepository.save(emailLog);
            
            // Send email (simulate for now)
            boolean sent = simulateEmailSending(emailLog);
            
            // Update status
            emailLog.setStatus(sent ? EmailLog.EmailStatus.SENT : EmailLog.EmailStatus.FAILED);
            emailLogRepository.save(emailLog);
            
            // Log as activity if requested
            if (request.getLogAsActivity() && sent) {
                logEmailAsActivity(request, processedSubject, processedContent);
            }
            
            return sent;
            
        } catch (Exception e) {
            log.error("Error sending templated email", e);
            return false;
        }
    }

    public boolean sendFollowUpEmail(String tenantId, Long contactId, Long dealId, Long userId, 
                                   String contactName, String dealName, String customMessage) {
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("contactName", contactName);
        variables.put("dealName", dealName);
        variables.put("customMessage", customMessage);
        variables.put("senderName", getUserName(userId));
        
        SendEmailRequest request = SendEmailRequest.builder()
                .tenantId(tenantId)
                .toEmail(getContactEmail(contactId))
                .templateName("follow-up")
                .templateVariables(variables)
                .emailType(EmailLog.EmailType.FOLLOW_UP)
                .contactId(contactId)
                .dealId(dealId)
                .userId(userId)
                .logAsActivity(true)
                .build();
        
        return sendTemplatedEmail(request);
    }

    public boolean sendProposalEmail(String tenantId, Long contactId, Long dealId, Long userId,
                                   String contactName, String dealName, String dealAmount, String proposalDetails) {
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("contactName", contactName);
        variables.put("dealName", dealName);
        variables.put("dealAmount", dealAmount);
        variables.put("proposalDetails", proposalDetails);
        variables.put("senderName", getUserName(userId));
        
        SendEmailRequest request = SendEmailRequest.builder()
                .tenantId(tenantId)
                .toEmail(getContactEmail(contactId))
                .templateName("proposal")
                .templateVariables(variables)
                .emailType(EmailLog.EmailType.PROPOSAL)
                .contactId(contactId)
                .dealId(dealId)
                .userId(userId)
                .logAsActivity(true)
                .build();
        
        return sendTemplatedEmail(request);
    }

    public boolean sendMeetingReminder(String tenantId, Long contactId, Long dealId, Long userId,
                                     String contactName, String dealName, String meetingDate, 
                                     String meetingTime, String meetingLocation) {
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("contactName", contactName);
        variables.put("dealName", dealName);
        variables.put("meetingDate", meetingDate);
        variables.put("meetingTime", meetingTime);
        variables.put("meetingLocation", meetingLocation);
        variables.put("senderName", getUserName(userId));
        
        SendEmailRequest request = SendEmailRequest.builder()
                .tenantId(tenantId)
                .toEmail(getContactEmail(contactId))
                .templateName("meeting-reminder")
                .templateVariables(variables)
                .emailType(EmailLog.EmailType.MEETING_REMINDER)
                .contactId(contactId)
                .dealId(dealId)
                .userId(userId)
                .logAsActivity(true)
                .build();
        
        return sendTemplatedEmail(request);
    }

    private boolean simulateEmailSending(EmailLog emailLog) {
        // Simulate email sending - in production, integrate with actual email service
        log.info("Simulating email send to: {} with subject: {}", emailLog.getToEmail(), emailLog.getSubject());
        
        // Simulate 95% success rate
        return Math.random() > 0.05;
    }

    private void logEmailAsActivity(SendEmailRequest request, String subject, String content) {
        try {
            log.info("Logging email as activity for contact: {}, deal: {}", request.getContactId(), request.getDealId());
            
            Map<String, Object> activityRequest = new HashMap<>();
            activityRequest.put("type", "EMAIL");
            activityRequest.put("content", "Email sent: " + subject + "\n\n" + stripHtml(content));
            activityRequest.put("outcome", "Sent");
            
            Map<String, Object> associations = new HashMap<>();
            if (request.getContactId() != null) {
                associations.put("contacts", new Long[]{request.getContactId()});
            }
            if (request.getDealId() != null) {
                associations.put("deals", new Long[]{request.getDealId()});
            }
            if (request.getAccountId() != null) {
                associations.put("accounts", new Long[]{request.getAccountId()});
            }
            activityRequest.put("associations", associations);
            
            // Call activity service
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Tenant-Id", request.getTenantId());
            headers.set("X-User-Id", request.getUserId().toString());
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(activityRequest, headers);
            
            restTemplate.exchange(
                    "http://localhost:8084/api/v1/activities",
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            
            log.info("Email logged as activity successfully");
            
        } catch (Exception e) {
            log.error("Failed to log email as activity", e);
        }
    }

    private String stripHtml(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]*>", "").trim();
    }

    private String getContactEmail(Long contactId) {
        // In production, call contacts service to get email
        return "contact" + contactId + "@example.com";
    }

    private String getUserName(Long userId) {
        // In production, call auth service to get user name
        return "Sales Rep";
    }
}
