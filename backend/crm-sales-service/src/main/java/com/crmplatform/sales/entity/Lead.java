package com.crmplatform.sales.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "leads")
public class Lead {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lead_id")
    private Long leadId;
    
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    
    @Column(name = "first_name", length = 100)
    private String firstName;
    
    @Column(name = "last_name", length = 100, nullable = false)
    private String lastName;
    
    @Column(name = "email", length = 255)
    private String email;
    
    @Column(name = "phone_number", length = 50)
    private String phoneNumber;
    
    @Column(name = "company", length = 255)
    private String company;
    
    @Column(name = "job_title", length = 100)
    private String jobTitle;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "lead_source", length = 50)
    private LeadSource leadSource;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "lead_status", length = 20)
    private LeadStatus leadStatus = LeadStatus.NEW;
    
    @Column(name = "lead_score")
    private Integer leadScore = 0;
    
    @Column(name = "estimated_value", precision = 15, scale = 2)
    private BigDecimal estimatedValue;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "employee_count")
    private Integer employeeCount;
    
    @Column(name = "annual_revenue")
    private Long annualRevenue;
    
    @Column(name = "industry", length = 100)
    private String industry;
    
    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "current_disposition", length = 50)
    private LeadDisposition currentDisposition;
    

     @Column(name = "website", length = 255)
    private String website;

    @Column(name = "disposition_notes", columnDefinition = "TEXT")
    private String dispositionNotes;
    
    @Column(name = "last_contact_date")
    private LocalDateTime lastContactDate;
    
    @Column(name = "next_follow_up_date")
    private LocalDateTime nextFollowUpDate;
    
    @Column(name = "disposition_updated_at")
    private LocalDateTime dispositionUpdatedAt;
    
    @Column(name = "disposition_updated_by")
    private Long dispositionUpdatedBy;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public Lead() {}
    
    public Lead(String firstName, String lastName, String email, String phoneNumber, 
                String company, String jobTitle, LeadSource leadSource, Long ownerUserId, 
                Long tenantId, BigDecimal estimatedValue) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.company = company;
        this.jobTitle = jobTitle;
        this.leadSource = leadSource;
        this.ownerUserId = ownerUserId;
        this.tenantId = tenantId;
        this.leadStatus = LeadStatus.NEW;
        this.leadScore = 0;
        this.estimatedValue = estimatedValue;
    }
    
    // Getters and Setters
    public Long getLeadId() {
        return leadId;
    }
    
    public void setLeadId(Long leadId) {
        this.leadId = leadId;
    }
    
    public Long getTenantId() {
        return tenantId;
    }
   
    public String getWebsite() {
        return website;
    }
    
    public void setWebsite(String website) {
        this.website = website;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
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
    
    public BigDecimal getEstimatedValue() {
        return estimatedValue;
    }

    public void setEstimatedValue(BigDecimal estimatedValue) {
        this.estimatedValue = estimatedValue;
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
    
    public LeadDisposition getCurrentDisposition() {
        return currentDisposition;
    }
    
    public void setCurrentDisposition(LeadDisposition currentDisposition) {
        this.currentDisposition = currentDisposition;
    }
    
    public String getDispositionNotes() {
        return dispositionNotes;
    }
    
    public void setDispositionNotes(String dispositionNotes) {
        this.dispositionNotes = dispositionNotes;
    }
    
    public LocalDateTime getLastContactDate() {
        return lastContactDate;
    }
    
    public void setLastContactDate(LocalDateTime lastContactDate) {
        this.lastContactDate = lastContactDate;
    }
    
    public LocalDateTime getNextFollowUpDate() {
        return nextFollowUpDate;
    }
    
    public void setNextFollowUpDate(LocalDateTime nextFollowUpDate) {
        this.nextFollowUpDate = nextFollowUpDate;
    }
    
    public LocalDateTime getDispositionUpdatedAt() {
        return dispositionUpdatedAt;
    }
    
    public void setDispositionUpdatedAt(LocalDateTime dispositionUpdatedAt) {
        this.dispositionUpdatedAt = dispositionUpdatedAt;
    }
    
    public Long getDispositionUpdatedBy() {
        return dispositionUpdatedBy;
    }
    
    public void setDispositionUpdatedBy(Long dispositionUpdatedBy) {
        this.dispositionUpdatedBy = dispositionUpdatedBy;
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
}
