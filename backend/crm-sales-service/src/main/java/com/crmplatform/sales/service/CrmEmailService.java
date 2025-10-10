//package com.crmplatform.sales.service;
//
//
//import com.crmplatform.sales.entity.*;
//
//import com.crmplatform.sales.repository.*;
//import com.crmplatform.common.security.UserContext;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import jakarta.mail.MessagingException;
//import jakarta.mail.internet.MimeMessage;
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import com.crmplatform.sales.dto.*;
//
//@Service
//@Transactional
//public class CrmEmailService {
//    
//    private static final Logger log = LoggerFactory.getLogger(CrmEmailService.class);
//    
//    @Autowired
//    private JavaMailSender mailSender;
//    
//    @Autowired
//    private EmailTemplateRepository emailTemplateRepository;
//    
//    @Autowired
//    private EmailSentHistoryRepository emailHistoryRepository;
//    
//    @Autowired
//    private LeadRepository leadRepository;
//    
//    @Value("${spring.mail.username:noreply@crmplatform.com}")
//    private String defaultSenderEmail;
//    
//    @Value("${crm.company.name:CRM Platform}")
//    private String companyName;
//    
//    /**
//     * Send email using template to a lead
//     */
//    public Long sendEmailToLead(Long leadId, Long templateId, Map<String, String> customVariables) {
//        try {
//            // Get lead details
//            Lead lead = leadRepository.findById(leadId)
//                .orElseThrow(() -> new RuntimeException("Lead not found"));
//            
//            // Get email template
//            EmailTemplate template = emailTemplateRepository.findById(templateId)
//                .orElseThrow(() -> new RuntimeException("Email template not found"));
//            
//            // Prepare template variables
//            Map<String, String> variables = prepareLeadVariables(lead);
//            if (customVariables != null) {
//                variables.putAll(customVariables);
//            }
//            
//            // Process template
//            String processedSubject = template.processSubject(variables);
//            String processedBody = template.processTemplate(variables);
//            
//            // Send email
//            return sendEmail(
//                lead.getEmail(),
//                processedSubject,
//                processedBody,
//                template.getHtmlBody() != null ? processTemplate(template.getHtmlBody(), variables) : null,
//                lead.getLeadId(),
//                null,
//                templateId
//            );
//            
//        } catch (Exception e) {
//            log.error("Error sending email to lead {}: {}", leadId, e.getMessage(), e);
//            throw new RuntimeException("Failed to send email: " + e.getMessage());
//        }
//    }
//    
//    /**
//     * Send custom email to a lead
//     */
//    public Long sendCustomEmailToLead(Long leadId, String subject, String body, String htmlBody) {
//        try {
//            Lead lead = leadRepository.findById(leadId)
//                .orElseThrow(() -> new RuntimeException("Lead not found"));
//            
//            return sendEmail(
//                lead.getEmail(),
//                subject,
//                body,
//                htmlBody,
//                leadId,
//                null,
//                null
//            );
//            
//        } catch (Exception e) {
//            log.error("Error sending custom email to lead {}: {}", leadId, e.getMessage(), e);
//            throw new RuntimeException("Failed to send email: " + e.getMessage());
//        }
//    }
//    
//    /**
//     * Send email to any recipient
//     */
//    public Long sendEmail(String recipientEmail, String subject, String body, String htmlBody,
//                         Long leadId, Long contactId, Long templateId) {
//        try {
//            // Create email history record first
//            EmailSentHistory emailHistory = new EmailSentHistory();
//            emailHistory.setTenantId(UserContext.getCurrentTenantId());
//            emailHistory.setLeadId(leadId);
//            emailHistory.setContactId(contactId);
//            emailHistory.setTemplateId(templateId);
//            emailHistory.setRecipientEmail(recipientEmail);
//            emailHistory.setSenderEmail(defaultSenderEmail);
//            emailHistory.setSubjectLine(subject);
//            emailHistory.setEmailBody(body);
//            emailHistory.setSendStatus(EmailSendStatus.PENDING);
//            emailHistory.setSentBy(UserContext.getCurrentUserId());
//            
//            emailHistory = emailHistoryRepository.save(emailHistory);
//            
//            // Send the actual email
//            if (htmlBody != null && !htmlBody.trim().isEmpty()) {
//                sendHtmlEmail(recipientEmail, subject, body, htmlBody);
//            } else {
//                sendPlainTextEmail(recipientEmail, subject, body);
//            }
//            
//            // Update status to sent
//            emailHistory.setSendStatus(EmailSendStatus.SENT);
//            emailHistory.setSentAt(LocalDateTime.now());
//            emailHistoryRepository.save(emailHistory);
//            
//            log.info("Email sent successfully to {} with subject: {}", recipientEmail, subject);
//            return emailHistory.getEmailId();
//            
//        } catch (Exception e) {
//            log.error("Error sending email to {}: {}", recipientEmail, e.getMessage(), e);
//            
//            // Update status to failed if we have the history record
//            try {
//                EmailSentHistory history = emailHistoryRepository.findByRecipientEmailAndSubjectLineAndSendStatus(
//                    recipientEmail, subject, EmailSendStatus.PENDING);
//                if (history != null) {
//                    history.setSendStatus(EmailSendStatus.FAILED);
//                    history.setFailedReason(e.getMessage());
//                    emailHistoryRepository.save(history);
//                }
//            } catch (Exception historyException) {
//                log.error("Error updating email history: {}", historyException.getMessage());
//            }
//            
//            throw new RuntimeException("Failed to send email: " + e.getMessage());
//        }
//    }
//    
//    /**
//     * Send plain text email
//     */
//    private void sendPlainTextEmail(String to, String subject, String body) {
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setFrom(defaultSenderEmail);
//        message.setTo(to);
//        message.setSubject(subject);
//        message.setText(body);
//        
//        mailSender.send(message);
//    }
//    
//    /**
//     * Send HTML email
//     */
//    private void sendHtmlEmail(String to, String subject, String textBody, String htmlBody) throws MessagingException {
//        MimeMessage message = mailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//        
//        helper.setFrom(defaultSenderEmail);
//        helper.setTo(to);
//        helper.setSubject(subject);
//        helper.setText(textBody, htmlBody);
//        
//        mailSender.send(message);
//    }
//    
//    /**
//     * Prepare template variables for a lead
//     */
//    private Map<String, String> prepareLeadVariables(Lead lead) {
//        Map<String, String> variables = new HashMap<>();
//        
//        variables.put("firstName", lead.getFirstName() != null ? lead.getFirstName() : "");
//        variables.put("lastName", lead.getLastName() != null ? lead.getLastName() : "");
//        variables.put("email", lead.getEmail() != null ? lead.getEmail() : "");
//        variables.put("phone", lead.getPhoneNumber() != null ? lead.getPhoneNumber() : "");
//        variables.put("company", lead.getCompany() != null ? lead.getCompany() : "");
//        variables.put("jobTitle", lead.getJobTitle() != null ? lead.getJobTitle() : "");
//        variables.put("leadScore", lead.getLeadScore() != null ? lead.getLeadScore().toString() : "0");
//        
//        // Add sender/company variables
//        variables.put("senderName", getCurrentUserName());
//        variables.put("companyName", companyName);
//        
//        return variables;
//    }
//    
//    /**
//     * Process template with variables
//     */
//    private String processTemplate(String template, Map<String, String> variables) {
//        String processed = template;
//        
//        if (variables != null) {
//            for (Map.Entry<String, String> entry : variables.entrySet()) {
//                String placeholder = "{{" + entry.getKey() + "}}";
//                String value = entry.getValue() != null ? entry.getValue() : "";
//                processed = processed.replace(placeholder, value);
//            }
//        }
//        
//        return processed;
//    }
//    
//    /**
//     * Get current user name (placeholder - implement based on your user service)
//     */
//    private String getCurrentUserName() {
//        // In production, fetch from user service
//        return "Sales Representative";
//    }
//    
//    /**
//     * Get email templates by type
//     */
//    public List<EmailTemplate> getTemplatesByType(EmailTemplateType type) {
//        return emailTemplateRepository.findByTenantIdAndTemplateTypeAndIsActiveOrderByTemplateName(
//            UserContext.getCurrentTenantId(), type, true);
//    }
//    
//    /**
//     * Get all active email templates
//     */
//    public List<EmailTemplate> getAllActiveTemplates() {
//        return emailTemplateRepository.findByTenantIdAndIsActiveOrderByTemplateTypeAscTemplateNameAsc(
//            UserContext.getCurrentTenantId(), true);
//    }
//    
//    /**
//     * Get default template for a type
//     */
//    public Optional<EmailTemplate> getDefaultTemplate(EmailTemplateType type) {
//        return emailTemplateRepository.findByTenantIdAndTemplateTypeAndIsDefaultAndIsActive(
//            UserContext.getCurrentTenantId(), type, true, true);
//    }
//    
//    /**
//     * Create new email template
//     */
//    public EmailTemplate createTemplate(String templateName, EmailTemplateType type, 
//                                      String subject, String body, String htmlBody, 
//                                      List<String> availableVariables) {
//        EmailTemplate template = new EmailTemplate();
//        template.setTenantId(UserContext.getCurrentTenantId());
//        template.setTemplateName(templateName);
//        template.setTemplateType(type);
//        template.setSubjectLine(subject);
//        template.setEmailBody(body);
//        template.setHtmlBody(htmlBody);
//        template.setAvailableVariablesList(availableVariables);
//        template.setCreatedBy(UserContext.getCurrentUserId());
//        
//        return emailTemplateRepository.save(template);
//    }
//    
//    /**
//     * Update email template
//     */
//    public EmailTemplate updateTemplate(Long templateId, String templateName, String subject, 
//                                      String body, String htmlBody, List<String> availableVariables) {
//        EmailTemplate template = emailTemplateRepository.findById(templateId)
//            .orElseThrow(() -> new RuntimeException("Template not found"));
//        
//        // Check tenant access
//        if (!template.getTenantId().equals(UserContext.getCurrentTenantId())) {
//            throw new RuntimeException("Access denied");
//        }
//        
//        template.setTemplateName(templateName);
//        template.setSubjectLine(subject);
//        template.setEmailBody(body);
//        template.setHtmlBody(htmlBody);
//        template.setAvailableVariablesList(availableVariables);
//        template.setUpdatedBy(UserContext.getCurrentUserId());
//        
//        return emailTemplateRepository.save(template);
//    }
//    
//    /**
//     * Get email history for a lead
//     */
//    public List<EmailSentHistory> getEmailHistoryForLead(Long leadId) {
//        return emailHistoryRepository.findByTenantIdAndLeadIdOrderBySentAtDesc(
//            UserContext.getCurrentTenantId(), leadId);
//    }
//    
//    /**
//     * Get email sending statistics
//     */
//    public Map<String, Object> getEmailStatistics() {
//        Long tenantId = UserContext.getCurrentTenantId();
//        
//        Map<String, Object> stats = new HashMap<>();
//        stats.put("totalEmailsSent", emailHistoryRepository.countByTenantIdAndSendStatus(tenantId, EmailSendStatus.SENT));
//        stats.put("totalEmailsFailed", emailHistoryRepository.countByTenantIdAndSendStatus(tenantId, EmailSendStatus.FAILED));
//        stats.put("totalEmailsPending", emailHistoryRepository.countByTenantIdAndSendStatus(tenantId, EmailSendStatus.PENDING));
//        
//        // Get emails sent in last 30 days
//        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
//        stats.put("emailsLast30Days", emailHistoryRepository.countByTenantIdAndSentAtAfter(tenantId, thirtyDaysAgo));
//        
//        return stats;
//    }
//    
//    /**
//     * Send welcome email to new lead
//     */
//    public void sendWelcomeEmail(Lead lead) {
//        try {
//            Optional<EmailTemplate> welcomeTemplate = getDefaultTemplate(EmailTemplateType.LEAD_WELCOME);
//            
//            if (welcomeTemplate.isPresent()) {
//                sendEmailToLead(lead.getLeadId(), welcomeTemplate.get().getTemplateId(), null);
//                log.info("Welcome email sent to lead: {}", lead.getLeadId());
//            } else {
//                log.warn("No welcome email template found for tenant: {}", lead.getTenantId());
//            }
//        } catch (Exception e) {
//            log.error("Error sending welcome email to lead {}: {}", lead.getLeadId(), e.getMessage());
//            // Don't throw exception as this is a background operation
//        }
//    }
//    
//    /**
//     * Send follow-up email
//     */
//    public void sendFollowUpEmail(Lead lead, String customMessage) {
//        try {
//            Optional<EmailTemplate> followUpTemplate = getDefaultTemplate(EmailTemplateType.FOLLOW_UP);
//            
//            if (followUpTemplate.isPresent()) {
//                Map<String, String> customVars = new HashMap<>();
//                if (customMessage != null && !customMessage.trim().isEmpty()) {
//                    customVars.put("customMessage", customMessage);
//                }
//                
//                sendEmailToLead(lead.getLeadId(), followUpTemplate.get().getTemplateId(), customVars);
//                log.info("Follow-up email sent to lead: {}", lead.getLeadId());
//            } else {
//                log.warn("No follow-up email template found for tenant: {}", lead.getTenantId());
//            }
//        } catch (Exception e) {
//            log.error("Error sending follow-up email to lead {}: {}", lead.getLeadId(), e.getMessage());
//        }
//    }
//}
//
//enum EmailSendStatus {
//    PENDING,
//    SENT,
//    DELIVERED,
//    OPENED,
//    CLICKED,
//    FAILED,
//    BOUNCED
//}
