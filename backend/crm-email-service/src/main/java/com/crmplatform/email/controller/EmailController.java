package com.crmplatform.email.controller;

import com.crmplatform.email.dto.EmailRequest;
import com.crmplatform.email.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
@Slf4j
public class EmailController {

    private final EmailService emailService;

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
}
