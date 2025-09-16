package com.crmplatform.sales.dto;

import com.crmplatform.sales.entity.LeadSource;
import com.crmplatform.sales.entity.LeadStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class UpdateLeadRequest {
    
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;
    
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;
    
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;
    
    @Size(max = 50, message = "Phone number must not exceed 50 characters")
    private String phoneNumber;
    
    @Size(max = 255, message = "Company must not exceed 255 characters")
    private String company;
    
    @Size(max = 100, message = "Job title must not exceed 100 characters")
    private String jobTitle;
    
    private LeadSource leadSource;
    
    private LeadStatus leadStatus;
    
    @Min(value = 0, message = "Lead score must be between 0 and 100")
    @Max(value = 100, message = "Lead score must be between 0 and 100")
    private Integer leadScore;
    
    private String notes;
    
    @Min(value = 0, message = "Employee count must be 0 or greater")
    private Integer employeeCount;
    
    @Min(value = 0, message = "Annual revenue must be 0 or greater")
    private Long annualRevenue;
    
    @Size(max = 100, message = "Industry must not exceed 100 characters")
    private String industry;
    
    // Constructors
    public UpdateLeadRequest() {}
    
    // Getters and Setters
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
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
}
