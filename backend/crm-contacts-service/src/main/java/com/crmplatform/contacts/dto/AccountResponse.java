package com.crmplatform.contacts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
    
    private Long accountId;
    private String accountName;
    private String website;
    private String industry;
    private Long ownerUserId;
    private LocalDateTime createdAt;
    
    private List<ContactSummary> contacts;
    private Map<String, String> customFields;
    
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
    }
} 