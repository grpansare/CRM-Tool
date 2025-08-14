package com.crmplatform.sales.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealResponse {
    
    private Long dealId;
    private String dealName;
    private BigDecimal amount;
    private LocalDate expectedCloseDate;
    private Long stageId;
    private String stageName;
    private String stageType;
    private BigDecimal winProbability;
    private Long contactId;
    private Long accountId;
    private Long ownerUserId;
    private LocalDateTime createdAt;
    
    private PipelineStageResponse currentStage;
    private ContactSummary contact;
    private AccountSummary account;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PipelineStageResponse {
        private Long stageId;
        private String stageName;
        private Integer stageOrder;
        private String stageType;
        private BigDecimal winProbability;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContactSummary {
        private Long contactId;
        private String firstName;
        private String lastName;
        private String primaryEmail;
        private String jobTitle;
        private String phoneNumber;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountSummary {
        private Long accountId;
        private String accountName;
        private String website;
        private String industry;
    }
} 