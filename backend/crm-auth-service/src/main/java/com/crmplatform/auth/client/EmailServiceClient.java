package com.crmplatform.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.crmplatform.auth.config.FeignAuthConfig;

import java.util.Map;

@FeignClient(name = "crm-email-service",configuration = FeignAuthConfig.class, fallback = EmailServiceClientFallback.class)
public interface EmailServiceClient {
    
    @PostMapping("/api/email/welcome")
    ResponseEntity<Map<String, Object>> sendWelcomeEmail(
            @RequestParam("tenantId") String tenantId,
            @RequestParam("toEmail") String toEmail,
            @RequestParam("userName") String userName,
            @RequestParam("tenantName") String tenantName
    );

    @PostMapping("/api/email/invitation")
    ResponseEntity<Map<String, Object>> sendInvitationEmail(
            @RequestParam("tenantId") String tenantId,
            @RequestParam("toEmail") String toEmail,
            @RequestParam("inviteUrl") String inviteUrl,
            @RequestParam("role") String role
    );
}
