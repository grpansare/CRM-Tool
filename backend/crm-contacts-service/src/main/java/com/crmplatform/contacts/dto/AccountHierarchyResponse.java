package com.crmplatform.contacts.dto;

import com.crmplatform.contacts.entity.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountHierarchyResponse {
    
    private Long accountId;
    private String accountName;
    private String website;
    private String industry;
    private Long ownerUserId;
    private Long parentAccountId;
    private AccountType accountType;
    private BigDecimal annualRevenue;
    private Integer employeeCount;
    private String description;
    private LocalDateTime createdAt;
    
    // Hierarchy information
    private List<AccountHierarchyResponse> childAccounts;
    private Long childAccountCount;
    private BigDecimal totalChildRevenue;
    
    // Contact information
    private Long contactCount;
    
    // Deal aggregation
    private Long totalDeals;
    private BigDecimal totalDealValue;
    private Long wonDeals;
    private BigDecimal wonDealValue;
}
