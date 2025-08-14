package com.crmplatform.contacts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactResponse {
    
    private Long contactId;
    private String firstName;
    private String lastName;
    private String primaryEmail;
    private String phoneNumber;
    private String jobTitle;
    private Long ownerUserId;
    private LocalDateTime createdAt;
    
    private AccountResponse account;
    private Map<String, String> customFields;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountResponse {
        private Long accountId;
        private String accountName;
        private String website;
        private String industry;
        private Long ownerUserId;
        private LocalDateTime createdAt;
    }
} 