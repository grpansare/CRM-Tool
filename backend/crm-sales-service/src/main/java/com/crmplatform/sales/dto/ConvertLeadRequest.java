package com.crmplatform.sales.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;

import java.math.BigDecimal;

public class ConvertLeadRequest {
    
    @NotNull(message = "Lead ID is required")
    private Long leadId;
    
    private boolean createAccount;
    
    @Size(max = 255, message = "Account name must not exceed 255 characters")
    private String accountName;
    
    @Size(max = 255, message = "Account website must not exceed 255 characters")
    private String accountWebsite;
    
    @Size(max = 100, message = "Account industry must not exceed 100 characters")
    private String accountIndustry;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Deal amount must be greater than 0")
    @Digits(integer = 12, fraction = 2, message = "Deal amount must have up to 2 decimal places")
    private BigDecimal dealAmount;
    
    // Constructors
    public ConvertLeadRequest() {}
    
    // Getters and Setters
    public Long getLeadId() {
        return leadId;
    }
    
    public void setLeadId(Long leadId) {
        this.leadId = leadId;
    }
    
    public boolean isCreateAccount() {
        return createAccount;
    }
    
    public void setCreateAccount(boolean createAccount) {
        this.createAccount = createAccount;
    }
    
    public String getAccountName() {
        return accountName;
    }
    
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
    
    public String getAccountWebsite() {
        return accountWebsite;
    }
    
    public void setAccountWebsite(String accountWebsite) {
        this.accountWebsite = accountWebsite;
    }
    
    public String getAccountIndustry() {
        return accountIndustry;
    }
    
    public void setAccountIndustry(String accountIndustry) {
        this.accountIndustry = accountIndustry;
    }
    
    public BigDecimal getDealAmount() {
        return dealAmount;
    }

    public void setDealAmount(BigDecimal dealAmount) {
        this.dealAmount = dealAmount;
    }
}
