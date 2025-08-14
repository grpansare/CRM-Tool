package com.crmplatform.sales.service;

import com.crmplatform.common.dto.ApiResponse;
import com.crmplatform.common.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactIntegrationService {
    
    private final RestTemplate restTemplate;
    
    @Value("${crm.contacts.service.url:http://localhost:8081}")
    private String contactsServiceUrl;
    
    public boolean validateContact(Long contactId) {
        Long tenantId = UserContext.getCurrentTenantId();
        
        try {
            String url = String.format("%s/api/v1/contacts/%d", contactsServiceUrl, contactId);
            ResponseEntity<ApiResponse> response = restTemplate.getForEntity(url, ApiResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ApiResponse apiResponse = response.getBody();
                return apiResponse.isSuccess();
            }
            
            log.warn("Contact validation failed for contact ID: {} in tenant: {}", contactId, tenantId);
            return false;
            
        } catch (Exception e) {
            log.error("Error validating contact ID: {} in tenant: {}", contactId, tenantId, e);
            return false;
        }
    }
    
    public boolean validateAccount(Long accountId) {
        Long tenantId = UserContext.getCurrentTenantId();
        
        try {
            String url = String.format("%s/api/v1/accounts/%d", contactsServiceUrl, accountId);
            ResponseEntity<ApiResponse> response = restTemplate.getForEntity(url, ApiResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ApiResponse apiResponse = response.getBody();
                return apiResponse.isSuccess();
            }
            
            log.warn("Account validation failed for account ID: {} in tenant: {}", accountId, tenantId);
            return false;
            
        } catch (Exception e) {
            log.error("Error validating account ID: {} in tenant: {}", accountId, tenantId, e);
            return false;
        }
    }
    
    public Map<String, Object> getContactDetails(Long contactId) {
        Long tenantId = UserContext.getCurrentTenantId();
        
        try {
            String url = String.format("%s/api/v1/contacts/%d", contactsServiceUrl, contactId);
            ResponseEntity<ApiResponse> response = restTemplate.getForEntity(url, ApiResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ApiResponse apiResponse = response.getBody();
                if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                    return (Map<String, Object>) apiResponse.getData();
                }
            }
            
            log.warn("Failed to get contact details for contact ID: {} in tenant: {}", contactId, tenantId);
            return null;
            
        } catch (Exception e) {
            log.error("Error getting contact details for contact ID: {} in tenant: {}", contactId, tenantId, e);
            return null;
        }
    }
    
    public Map<String, Object> getAccountDetails(Long accountId) {
        Long tenantId = UserContext.getCurrentTenantId();
        
        try {
            String url = String.format("%s/api/v1/accounts/%d", contactsServiceUrl, accountId);
            ResponseEntity<ApiResponse> response = restTemplate.getForEntity(url, ApiResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ApiResponse apiResponse = response.getBody();
                if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                    return (Map<String, Object>) apiResponse.getData();
                }
            }
            
            log.warn("Failed to get account details for account ID: {} in tenant: {}", accountId, tenantId);
            return null;
            
        } catch (Exception e) {
            log.error("Error getting account details for account ID: {} in tenant: {}", accountId, tenantId, e);
            return null;
        }
    }
} 