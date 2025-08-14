package com.crmplatform.auth.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class EmailServiceClientFallback implements EmailServiceClient {
    
    @Override
    public ResponseEntity<Map<String, Object>> sendWelcomeEmail(String tenantId, String toEmail, String userName, String tenantName) {
        log.warn("Email service is unavailable. Fallback triggered for welcome email to: {} for tenant: {}", toEmail, tenantId);
        
        return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Email service temporarily unavailable"
        ));
    }
}
