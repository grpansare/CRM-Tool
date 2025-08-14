package com.crmplatform.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantStatsResponse {
    private int totalUsers;
    private int activeUsers;
    private int pendingInvitations;
    private int totalContacts;
    private int totalAccounts;
    private int totalDeals;
    private BigDecimal totalRevenue;
    private BigDecimal monthlyRevenue;
    private int dealsThisMonth;
    private int contactsThisMonth;
    private String subscriptionPlan;
    private int maxUsers;
    private boolean isTrialActive;
    private String trialEndsAt;
    private String subscriptionEndsAt;
}
