package com.crmplatform.sales.dto;

public class ConvertLeadResponse {
    
    private Long leadId;
    private Long contactId;
    private Long accountId;
    private Long dealId;
    private String message;
    
    // Constructors
    public ConvertLeadResponse() {}
    
    public ConvertLeadResponse(Long leadId, Long contactId, Long accountId, String message) {
        this.leadId = leadId;
        this.contactId = contactId;
        this.accountId = accountId;
        this.message = message;
    }
    
    public ConvertLeadResponse(Long leadId, Long contactId, Long accountId, Long dealId, String message) {
        this.leadId = leadId;
        this.contactId = contactId;
        this.accountId = accountId;
        this.dealId = dealId;
        this.message = message;
    }
    
    // Getters and Setters
    public Long getLeadId() {
        return leadId;
    }
    
    public void setLeadId(Long leadId) {
        this.leadId = leadId;
    }
    
    public Long getContactId() {
        return contactId;
    }
    
    public void setContactId(Long contactId) {
        this.contactId = contactId;
    }
    
    public Long getAccountId() {
        return accountId;
    }
    
    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
    
    public Long getDealId() {
        return dealId;
    }
    
    public void setDealId(Long dealId) {
        this.dealId = dealId;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
