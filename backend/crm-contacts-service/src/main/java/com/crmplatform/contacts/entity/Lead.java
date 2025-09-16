package com.crmplatform.contacts.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "leads")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lead {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lead_id")
    private Long leadId;
    
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    
    @Column(name = "first_name", length = 100)
    private String firstName;
    
    @Column(name = "last_name", nullable = false, length = 100)
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
    @Column(name = "lead_status", nullable = false, length = 50)
    private LeadStatus leadStatus;
    
    @Column(name = "lead_score")
    private Integer leadScore;
    
    @Column(name = "industry", length = 100)
    private String industry;
    
    @Column(name = "website", length = 255)
    private String website;
    
    @Column(name = "annual_revenue")
    private Long annualRevenue;
    
    @Column(name = "employee_count")
    private Integer employeeCount;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId;
    
    @Column(name = "converted_contact_id")
    private Long convertedContactId;
    
    @Column(name = "converted_account_id")
    private Long convertedAccountId;
    
    @Column(name = "converted_at")
    private LocalDateTime convertedAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    public enum LeadSource {
        WEBSITE,
        SOCIAL_MEDIA,
        EMAIL_CAMPAIGN,
        COLD_CALL,
        REFERRAL,
        TRADE_SHOW,
        WEBINAR,
        CONTENT_DOWNLOAD,
        ADVERTISEMENT,
        PARTNER,
        OTHER
    }
    
    public enum LeadStatus {
        NEW,
        CONTACTED,
        QUALIFIED,
        UNQUALIFIED,
        NURTURING,
        CONVERTED,
        LOST
    }
    
    public boolean isConverted() {
        return leadStatus == LeadStatus.CONVERTED && convertedContactId != null;
    }
}
