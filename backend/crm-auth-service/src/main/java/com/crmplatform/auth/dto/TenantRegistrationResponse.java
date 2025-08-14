package com.crmplatform.auth.dto;

import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@Builder
public class TenantRegistrationResponse {
    
    private Long tenantId;
    private String tenantName;
    private String companyName;
    private String subscriptionPlan;
    private LocalDateTime trialEndsAt;
    private LocalDateTime createdAt;
    private Long adminUserId;
    private String adminEmail;
    private String adminUsername;
    private String welcomeMessage;
    private String nextSteps;
} 