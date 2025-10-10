package com.crmplatform.sales.dto;

import com.crmplatform.sales.entity.Lead;
import com.crmplatform.sales.entity.LeadSource;
import com.crmplatform.sales.entity.LeadStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LeadResponse {
    
    private Long leadId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String company;
    private String jobTitle;
    private LeadSource leadSource;
    private LeadStatus leadStatus;
    private Integer leadScore;
    private String notes;
    private Integer employeeCount;
    private Long annualRevenue;
    private String industry;
    private String website;
    private Long ownerUserId;
    private String ownerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BigDecimal estimatedValue;
    private LocalDateTime nextFollowUpDate;

    
    // Constructors
    public LeadResponse() {}
    
    public LeadResponse(Lead lead) {
        this.leadId = lead.getLeadId();
        this.firstName = lead.getFirstName();
        this.lastName = lead.getLastName();
        this.email = lead.getEmail();
        this.phoneNumber = lead.getPhoneNumber();
        this.company = lead.getCompany();
        this.jobTitle = lead.getJobTitle();
        this.leadSource = lead.getLeadSource();
        this.leadStatus = lead.getLeadStatus();
        this.leadScore = lead.getLeadScore();
        this.notes = lead.getNotes();
        this.employeeCount = lead.getEmployeeCount();
        this.annualRevenue = lead.getAnnualRevenue();
        this.industry = lead.getIndustry();
        this.website = lead.getWebsite();
        this.ownerUserId = lead.getOwnerUserId();

        this.ownerName = null; // Owner name needs to be set separately via user service lookup
        this.createdAt = lead.getCreatedAt();
        this.updatedAt = lead.getUpdatedAt();
        this.estimatedValue = lead.getEstimatedValue();
        this.nextFollowUpDate = lead.getNextFollowUpDate(); // Assuming Lead entity has getNextFollowUpDate method
    }
    
    // Getters and Setters
    public Long getLeadId() {
        return leadId;
    }
    
    
    public void setLeadId(Long leadId) {
        this.leadId = leadId;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }

   
    public String getWebsite() {
        return website;
    }
    
    public void setWebsite(String website) {
        this.website = website;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getCompany() {
        return company;
    }
    
    public void setCompany(String company) {
        this.company = company;
    }
    
    public String getJobTitle() {
        return jobTitle;
    }
    
    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }
    
    public LeadSource getLeadSource() {
        return leadSource;
    }
    
    public void setLeadSource(LeadSource leadSource) {
        this.leadSource = leadSource;
    }
    
    public LeadStatus getLeadStatus() {
        return leadStatus;
    }
    
    public void setLeadStatus(LeadStatus leadStatus) {
        this.leadStatus = leadStatus;
    }
    
    public Integer getLeadScore() {
        return leadScore;
    }
    
    public void setLeadScore(Integer leadScore) {
        this.leadScore = leadScore;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public Integer getEmployeeCount() {
        return employeeCount;
    }
    
    public void setEmployeeCount(Integer employeeCount) {
        this.employeeCount = employeeCount;
    }
    
    public Long getAnnualRevenue() {
        return annualRevenue;
    }
    
    public void setAnnualRevenue(Long annualRevenue) {
        this.annualRevenue = annualRevenue;
    }
    
    public String getIndustry() {
        return industry;
    }
    
    public void setIndustry(String industry) {
        this.industry = industry;
    }
    
    public Long getOwnerUserId() {
        return ownerUserId;
    }
    
    public void setOwnerUserId(Long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }
    
    public String getOwnerName() {
        return ownerName;
    }
    
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public BigDecimal getEstimatedValue() {
        return estimatedValue;
    }
    
    public void setEstimatedValue(BigDecimal estimatedValue) {
        this.estimatedValue = estimatedValue;
    }
    
    public LocalDateTime getNextFollowUpDate() {
        return nextFollowUpDate;
    }
    
    public void setNextFollowUpDate(LocalDateTime nextFollowUpDate) {
        this.nextFollowUpDate = nextFollowUpDate;
    }
}
