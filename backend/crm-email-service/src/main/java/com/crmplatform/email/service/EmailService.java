package com.crmplatform.email.service;

import com.crmplatform.email.dto.EmailRequest;
import com.crmplatform.email.entity.EmailLog;
import com.crmplatform.email.entity.EmailTemplate;
import com.crmplatform.email.repository.EmailLogRepository;
import com.crmplatform.email.repository.EmailTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailLogRepository emailLogRepository;
    private final EmailTemplateRepository emailTemplateRepository;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String defaultFromEmail;

    @Async
    public CompletableFuture<Boolean> sendEmailAsync(EmailRequest emailRequest) {
        return CompletableFuture.completedFuture(sendEmail(emailRequest));
    }

    public boolean sendEmail(EmailRequest emailRequest) {
        EmailLog emailLog = createEmailLog(emailRequest);
        
        try {
            String content = prepareEmailContent(emailRequest);
            
            if (emailRequest.getIsHtml()) {
                sendHtmlEmail(emailRequest, content);
            } else {
                sendTextEmail(emailRequest, content);
            }
            
            emailLog.setStatus(EmailLog.EmailStatus.SENT);
            emailLog.setContent(content);
            log.info("Email sent successfully to: {} for tenant: {}", emailRequest.getToEmail(), emailRequest.getTenantId());
            
        } catch (Exception e) {
            emailLog.setStatus(EmailLog.EmailStatus.FAILED);
            emailLog.setErrorMessage(e.getMessage());
            log.error("Failed to send email to: {} for tenant: {}. Error: {}", 
                    emailRequest.getToEmail(), emailRequest.getTenantId(), e.getMessage(), e);
        } finally {
            emailLogRepository.save(emailLog);
        }
        
        return emailLog.getStatus() == EmailLog.EmailStatus.SENT;
    }

    @Async
    public CompletableFuture<Boolean> sendWelcomeEmailAsync(String tenantId, String toEmail, String userName, String tenantName) {
        EmailRequest emailRequest = EmailRequest.builder()
                .tenantId(tenantId)
                .toEmail(toEmail)
                .fromEmail(defaultFromEmail)
                .subject("Welcome to " + tenantName + " CRM Platform!")
                .templateName("WELCOME")
                .emailType(EmailRequest.EmailType.WELCOME)
                .templateVariables(Map.of(
                        "userName", userName,
                        "tenantName", tenantName,
                        "loginUrl", "https://" + tenantId + ".crm-platform.com/login"
                ))
                .isHtml(true)
                .build();
        
        return sendEmailAsync(emailRequest);
    }

    @Async
    public CompletableFuture<Boolean> sendInvitationEmailAsync(String tenantId, String toEmail, String inviteUrl, String role) {
        EmailRequest emailRequest = EmailRequest.builder()
                .tenantId(tenantId)
                .toEmail(toEmail)
                .fromEmail(defaultFromEmail)
                .subject("You're invited to join the CRM Platform")
                .templateName("INVITATION")
                .emailType(EmailRequest.EmailType.WELCOME)
                .templateVariables(Map.of(
                        "inviteUrl", inviteUrl,
                        "role", role
                ))
                .isHtml(true)
                .build();

        return sendEmailAsync(emailRequest);
    }

    private String prepareEmailContent(EmailRequest emailRequest) {
        if (emailRequest.getTemplateName() != null) {
            return processTemplate(emailRequest);
        }
        return emailRequest.getContent();
    }

    private String processTemplate(EmailRequest emailRequest) {
        Optional<EmailTemplate> templateOpt = emailTemplateRepository
                .findByTenantIdAndTemplateNameAndIsActiveTrue(emailRequest.getTenantId(), emailRequest.getTemplateName());
        
        Context context = new Context();
        if (emailRequest.getTemplateVariables() != null) {
            emailRequest.getTemplateVariables().forEach(context::setVariable);
        }
        
        if (templateOpt.isEmpty()) {
            // Fallback to default Thymeleaf template file
            if ("WELCOME".equals(emailRequest.getTemplateName())) {
                return templateEngine.process("welcome-email", context);
            }
            if ("INVITATION".equals(emailRequest.getTemplateName())) {
                return getDefaultInvitationTemplate(emailRequest.getTemplateVariables());
            }
            return getDefaultWelcomeTemplate(emailRequest.getTemplateVariables());
        }
        
        EmailTemplate template = templateOpt.get();
        return templateEngine.process(template.getHtmlContent(), context);
    }

    private String getDefaultWelcomeTemplate(Map<String, Object> variables) {
        String userName = (String) variables.getOrDefault("userName", "User");
        String tenantName = (String) variables.getOrDefault("tenantName", "CRM Platform");
        String loginUrl = (String) variables.getOrDefault("loginUrl", "#");
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Welcome to %s</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #007bff; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f8f9fa; }
                    .button { display: inline-block; padding: 12px 24px; background-color: #007bff; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Welcome to %s!</h1>
                    </div>
                    <div class="content">
                        <h2>Hello %s,</h2>
                        <p>Welcome to your new CRM platform! We're excited to have you on board.</p>
                        <p>Your account has been successfully created and you can now start managing your contacts, accounts, and sales activities.</p>
                        <p>To get started, click the button below to access your dashboard:</p>
                        <a href="%s" class="button">Access Your Dashboard</a>
                        <p>If you have any questions or need assistance, please don't hesitate to contact our support team.</p>
                        <p>Best regards,<br>The %s Team</p>
                    </div>
                    <div class="footer">
                        <p>This email was sent from %s CRM Platform. Please do not reply to this email.</p>
                    </div>
                </div>
            </body>
            </html>
            """, tenantName, tenantName, userName, loginUrl, tenantName, tenantName);
    }

    private String getDefaultInvitationTemplate(Map<String, Object> variables) {
        String inviteUrl = (String) variables.getOrDefault("inviteUrl", "#");
        String role = (String) variables.getOrDefault("role", "User");

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>You're invited</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #10b981; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f8f9fa; }
                    .button { display: inline-block; padding: 12px 24px; background-color: #10b981; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>You're invited!</h1>
                    </div>
                    <div class="content">
                        <p>You have been invited to join our CRM platform as <strong>%s</strong>.</p>
                        <p>Click the button below to accept the invitation and set up your account.</p>
                        <a href="%s" class="button">Accept Invitation</a>
                        <p>If you were not expecting this invitation, you can ignore this email.</p>
                    </div>
                    <div class="footer">
                        <p>This email was sent automatically. Please do not reply.</p>
                    </div>
                </div>
            </body>
            </html>
            """, role, inviteUrl);
    }

    private void sendHtmlEmail(EmailRequest emailRequest, String content) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(emailRequest.getFromEmail() != null ? emailRequest.getFromEmail() : defaultFromEmail);
        helper.setTo(emailRequest.getToEmail());
        helper.setSubject(emailRequest.getSubject());
        helper.setText(content, true);
        
        if (emailRequest.getCcEmail() != null) {
            helper.setCc(emailRequest.getCcEmail());
        }
        
        if (emailRequest.getBccEmail() != null) {
            helper.setBcc(emailRequest.getBccEmail());
        }
        
        mailSender.send(message);
    }

    private void sendTextEmail(EmailRequest emailRequest, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailRequest.getFromEmail() != null ? emailRequest.getFromEmail() : defaultFromEmail);
        message.setTo(emailRequest.getToEmail());
        message.setSubject(emailRequest.getSubject());
        message.setText(content);
        
        if (emailRequest.getCcEmail() != null) {
            message.setCc(emailRequest.getCcEmail());
        }
        
        if (emailRequest.getBccEmail() != null) {
            message.setBcc(emailRequest.getBccEmail());
        }
        
        mailSender.send(message);
    }

    private EmailLog createEmailLog(EmailRequest emailRequest) {
        return EmailLog.builder()
                .tenantId(emailRequest.getTenantId())
                .fromEmail(emailRequest.getFromEmail() != null ? emailRequest.getFromEmail() : defaultFromEmail)
                .toEmail(emailRequest.getToEmail())
                .ccEmail(emailRequest.getCcEmail())
                .bccEmail(emailRequest.getBccEmail())
                .subject(emailRequest.getSubject())
                .status(EmailLog.EmailStatus.PENDING)
                .emailType(EmailLog.EmailType.valueOf(emailRequest.getEmailType().name()))
                .templateName(emailRequest.getTemplateName())
                .retryCount(0)
                .build();
    }
}
