package com.crmplatform.contacts.dto;

import com.crmplatform.contacts.entity.Lead;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadResponse {
    
    private Long leadId;
    private Long tenantId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String company;
    private String jobTitle;
    private Lead.LeadSource leadSource;
    private Lead.LeadStatus leadStatus;
    private Integer leadScore;
    private String industry;
    private String website;
    private Long annualRevenue;
    private Integer employeeCount;
    private String notes;
    private Long ownerUserId;
    private String ownerName;
    private Long convertedContactId;
    private Long convertedAccountId;
    private LocalDateTime convertedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    
    public String getFullName() {
        if (firstName != null && !firstName.trim().isEmpty()) {
            return firstName + " " + lastName;
        }
        return lastName;
    }
    
    public boolean isConverted() {
        return leadStatus == Lead.LeadStatus.CONVERTED && convertedContactId != null;
    }
}
