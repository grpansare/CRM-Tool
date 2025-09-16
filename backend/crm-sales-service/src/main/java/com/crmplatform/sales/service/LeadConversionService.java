package com.crmplatform.sales.service;

import com.crmplatform.common.dto.ApiResponse;
import com.crmplatform.common.security.UserContext;
import com.crmplatform.sales.dto.ConvertLeadRequest;
import com.crmplatform.sales.dto.ConvertLeadResponse;
import com.crmplatform.sales.entity.Lead;
import com.crmplatform.sales.entity.LeadStatus;
import com.crmplatform.sales.entity.Deal;
import com.crmplatform.sales.entity.PipelineStage;
import com.crmplatform.sales.repository.LeadRepository;
import com.crmplatform.sales.repository.DealRepository;
import com.crmplatform.sales.repository.PipelineStageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class LeadConversionService {
    
    private static final Logger log = LoggerFactory.getLogger(LeadConversionService.class);
    
    @Autowired
    private LeadRepository leadRepository;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private LeadActivityService leadActivityService;
    
    @Autowired
    private DealRepository dealRepository;
    
    @Autowired
    private PipelineStageRepository pipelineStageRepository;
    
    @Value("${crm.contacts.service.url:http://localhost:8081}")
    private String contactsServiceUrl;
    
    public ConvertLeadResponse convertLead(ConvertLeadRequest request) {
        try {
            Long tenantId = UserContext.getCurrentTenantId();
            Long userId = UserContext.getCurrentUserId();
            
            log.info("Converting lead: leadId={}, tenantId={}, userId={}, createAccount={}", 
                request.getLeadId(), tenantId, userId, request.isCreateAccount());
            
            // Find and validate lead
            Lead lead = leadRepository.findByLeadIdAndTenantId(request.getLeadId(), tenantId)
                    .orElseThrow(() -> new RuntimeException("Lead not found"));
            
            if (lead.getLeadStatus() == LeadStatus.CONVERTED) {
                throw new RuntimeException("Lead is already converted");
            }
            
            // Validate lead status - must be QUALIFIED or higher for conversion
            if (!isLeadReadyForConversion(lead.getLeadStatus())) {
                throw new RuntimeException("Lead must be QUALIFIED or higher status to be converted. Current status: " + lead.getLeadStatus());
            }
            
            log.info("Lead found: firstName={}, lastName={}, email={}, status={}", 
                lead.getFirstName(), lead.getLastName(), lead.getEmail(), lead.getLeadStatus());
            
            Long contactId = null;
            Long accountId = null;
            
            // Create contact first
            contactId = createContact(lead);
            
            // Create account if requested
            if (request.isCreateAccount() && request.getAccountName() != null && !request.getAccountName().trim().isEmpty()) {
                accountId = createAccount(request, contactId);
                
                // Link contact to account after account creation
                if (accountId != null) {
                    linkContactToAccount(contactId, accountId);
                }
            }
            
            // Auto-create deal after successful contact/account creation
            Long dealId = createDealFromLead(lead, contactId, accountId, request);
            
            // Update lead status to converted
            lead.setLeadStatus(LeadStatus.CONVERTED);
            leadRepository.save(lead);
            
            // Log conversion activity
            String leadName = (lead.getFirstName() != null ? lead.getFirstName() + " " : "") + lead.getLastName();
            leadActivityService.logLeadConverted(lead.getLeadId(), leadName, contactId, accountId);
            
            return new ConvertLeadResponse(
                lead.getLeadId(),
                contactId,
                accountId,
                dealId,
                "Lead converted successfully with deal created"
            );
            
        } catch (Exception e) {
            log.error("Failed to convert lead: leadId={}, error={}", request.getLeadId(), e.getMessage(), e);
            throw new RuntimeException("Failed to convert lead: " + e.getMessage());
        }
    }
    
    private Long createContact(Lead lead) {
        try {
            log.info("Creating contact for lead: email={}, firstName={}, lastName={}", 
                lead.getEmail(), lead.getFirstName(), lead.getLastName());
            
            Map<String, Object> contactData = new HashMap<>();
            contactData.put("firstName", lead.getFirstName());
            contactData.put("lastName", lead.getLastName());
            contactData.put("email", lead.getEmail());
            
            // Only add optional fields if they have values
            if (lead.getPhoneNumber() != null && !lead.getPhoneNumber().trim().isEmpty()) {
                contactData.put("phoneNumber", lead.getPhoneNumber());
            }
            if (lead.getCompany() != null && !lead.getCompany().trim().isEmpty()) {
                contactData.put("company", lead.getCompany());
            }
            if (lead.getJobTitle() != null && !lead.getJobTitle().trim().isEmpty()) {
                contactData.put("jobTitle", lead.getJobTitle());
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-User-Id", UserContext.getCurrentUserId().toString());
            headers.set("X-Tenant-Id", UserContext.getCurrentTenantId().toString());
            headers.set("X-Username", UserContext.getCurrentUsername());
            headers.set("X-User-Role", UserContext.getCurrentUserRole());
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(contactData, headers);
            
            String url = contactsServiceUrl + "/api/v1/contacts";
            log.info("Calling contacts service: url={}, data={}", url, contactData);
            
            ResponseEntity<ApiResponse> response = restTemplate.postForEntity(url, entity, ApiResponse.class);
            
            log.info("Contacts service response: status={}, body={}", 
                response.getStatusCode(), response.getBody());
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().isSuccess()) {
                Map<String, Object> responseData = (Map<String, Object>) response.getBody().getData();
                Long contactId = Long.valueOf(responseData.get("contactId").toString());
                log.info("Contact created successfully: contactId={}", contactId);
                return contactId;
            } else {
                String errorMsg = response.getBody() != null ? response.getBody().getMessage() : "Unknown error";
                log.error("Failed to create contact: status={}, error={}", response.getStatusCode(), errorMsg);
                throw new RuntimeException("Failed to create contact: " + errorMsg);
            }
            
        } catch (Exception e) {
            log.error("Error creating contact for lead: leadId={}, error={}", lead.getLeadId(), e.getMessage(), e);
            throw new RuntimeException("Error creating contact: " + e.getMessage());
        }
    }
    
    private Long createAccount(ConvertLeadRequest request, Long contactId) {
        try {
            log.info("Creating account: name={}, website={}, industry={}, contactId={}", 
                request.getAccountName(), request.getAccountWebsite(), request.getAccountIndustry(), contactId);
            
            // First, try to find existing account by name
            Long existingAccountId = findExistingAccount(request.getAccountName());
            if (existingAccountId != null) {
                log.info("Found existing account with name '{}': accountId={}", request.getAccountName(), existingAccountId);
                return existingAccountId;
            }
            
            Map<String, Object> accountData = new HashMap<>();
            accountData.put("accountName", request.getAccountName());
            if (request.getAccountWebsite() != null && !request.getAccountWebsite().trim().isEmpty()) {
                accountData.put("website", request.getAccountWebsite());
            }
            if (request.getAccountIndustry() != null && !request.getAccountIndustry().trim().isEmpty()) {
                accountData.put("industry", request.getAccountIndustry());
            }
            accountData.put("primaryContactId", contactId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-User-Id", UserContext.getCurrentUserId().toString());
            headers.set("X-Tenant-Id", UserContext.getCurrentTenantId().toString());
            headers.set("X-Username", UserContext.getCurrentUsername());
            headers.set("X-User-Role", UserContext.getCurrentUserRole());
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(accountData, headers);
            
            String url = contactsServiceUrl + "/api/v1/accounts";
            log.info("Calling contacts service for account: url={}, data={}", url, accountData);
            
            ResponseEntity<ApiResponse> response = restTemplate.postForEntity(url, entity, ApiResponse.class);
            
            log.info("Account creation response: status={}, body={}", 
                response.getStatusCode(), response.getBody());
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().isSuccess()) {
                Map<String, Object> responseData = (Map<String, Object>) response.getBody().getData();
                Long accountId = Long.valueOf(responseData.get("accountId").toString());
                log.info("Account created successfully: accountId={}", accountId);
                return accountId;
            } else {
                String errorMsg = response.getBody() != null ? response.getBody().getMessage() : "Unknown error";
                log.error("Failed to create account: status={}, error={}", response.getStatusCode(), errorMsg);
                throw new RuntimeException("Failed to create account: " + errorMsg);
            }
            
        } catch (org.springframework.web.client.HttpClientErrorException.Conflict e) {
            // Handle 409 Conflict - account already exists
            log.warn("Account '{}' already exists, attempting to find existing account", request.getAccountName());
            Long existingAccountId = findExistingAccount(request.getAccountName());
            if (existingAccountId != null) {
                log.info("Using existing account: accountId={}", existingAccountId);
                return existingAccountId;
            } else {
                // If we can't find the existing account, proceed without account creation
                // This allows the lead conversion to continue with just the contact
                log.warn("Could not find existing account '{}' after conflict error. Proceeding without account creation.", request.getAccountName());
                return null; // Return null to indicate no account was created/found
            }
        } catch (Exception e) {
            log.error("Error creating account: accountName={}, error={}", request.getAccountName(), e.getMessage(), e);
            throw new RuntimeException("Error creating account: " + e.getMessage());
        }
    }
    
    private void linkContactToAccount(Long contactId, Long accountId) {
        try {
            log.info("Linking contact to account: contactId={}, accountId={}", contactId, accountId);
            
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("accountId", accountId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-User-Id", UserContext.getCurrentUserId().toString());
            headers.set("X-Tenant-Id", UserContext.getCurrentTenantId().toString());
            headers.set("X-Username", UserContext.getCurrentUsername());
            headers.set("X-User-Role", UserContext.getCurrentUserRole());
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(updateData, headers);
            
            String url = contactsServiceUrl + "/api/v1/contacts/" + contactId + "/account/" + accountId;
            log.info("Linking contact to account: url={}", url);
            
            ResponseEntity<ApiResponse> response = restTemplate.exchange(
                url, HttpMethod.PUT, entity, ApiResponse.class);
            
            log.info("Contact update response: status={}, body={}", 
                response.getStatusCode(), response.getBody());
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().isSuccess()) {
                log.info("Contact successfully linked to account: contactId={}, accountId={}", contactId, accountId);
            } else {
                String errorMsg = response.getBody() != null ? response.getBody().getMessage() : "Unknown error";
                log.warn("Failed to link contact to account: status={}, error={}", response.getStatusCode(), errorMsg);
                // Don't throw exception here - contact and account are created, just association failed
            }
            
        } catch (Exception e) {
            log.warn("Error linking contact to account: contactId={}, accountId={}, error={}", 
                contactId, accountId, e.getMessage());
            // Don't throw exception - this is not critical for conversion success
        }
    }
    
    private Long createDealFromLead(Lead lead, Long contactId, Long accountId, ConvertLeadRequest request) {
        try {
            Long tenantId = UserContext.getCurrentTenantId();
            Long userId = UserContext.getCurrentUserId();
            
            log.info("Creating deal from lead: leadId={}, contactId={}, accountId={}", 
                lead.getLeadId(), contactId, accountId);
            
            // Find the first stage of the default pipeline (usually "Prospecting")
            Optional<PipelineStage> firstStage = pipelineStageRepository
                .findFirstByTenantIdOrderByStageOrder(tenantId);
            
            if (firstStage.isEmpty()) {
                log.warn("No pipeline stages found for tenant: {}", tenantId);
                throw new RuntimeException("No pipeline stages configured. Please set up a sales pipeline first.");
            }
            
            // Create deal name from lead information
            String dealName = generateDealName(lead);
            
            // Use lead's estimated value if available, otherwise use provided deal amount, default to 0.00
            BigDecimal dealAmount = lead.getEstimatedValue() != null ? 
                lead.getEstimatedValue() : 
                (request.getDealAmount() != null ? request.getDealAmount() : new BigDecimal("0.00"));
            
            // Set expected close date (30 days from now by default)
            LocalDate expectedCloseDate = LocalDate.now().plusDays(30);
            
            // Create the deal
            Deal deal = Deal.builder()
                .tenantId(tenantId)
                .dealName(dealName)
                .amount(dealAmount)
                .expectedCloseDate(expectedCloseDate)
                .stageId(firstStage.get().getStageId())
                .contactId(contactId)
                .accountId(accountId != null ? accountId : contactId) // Use contactId if no account
                .ownerUserId(userId)
                .build();
            
            deal = dealRepository.save(deal);
            
            log.info("Deal created successfully: dealId={}, dealName={}, stageId={}", 
                deal.getDealId(), deal.getDealName(), deal.getStageId());
            
            return deal.getDealId();
            
        } catch (Exception e) {
            log.error("Error creating deal from lead: leadId={}, error={}", lead.getLeadId(), e.getMessage(), e);
            throw new RuntimeException("Error creating deal: " + e.getMessage());
        }
    }
    
    private Long findExistingAccount(String accountName) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-User-Id", UserContext.getCurrentUserId().toString());
            headers.set("X-Tenant-Id", UserContext.getCurrentTenantId().toString());
            headers.set("X-Username", UserContext.getCurrentUsername());
            headers.set("X-User-Role", UserContext.getCurrentUserRole());
            
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            // Try multiple search approaches
            String[] searchUrls = {
                contactsServiceUrl + "/api/v1/accounts?search=" + URLEncoder.encode(accountName, StandardCharsets.UTF_8),
                contactsServiceUrl + "/api/v1/accounts?accountName=" + URLEncoder.encode(accountName, StandardCharsets.UTF_8),
                contactsServiceUrl + "/api/v1/accounts"
            };
            
            for (String url : searchUrls) {
                try {
                    log.info("Searching for existing account: url={}", url);
                    
                    ResponseEntity<ApiResponse> response = restTemplate.exchange(url, 
                        org.springframework.http.HttpMethod.GET, entity, ApiResponse.class);
                    
                    if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().isSuccess()) {
                        Map<String, Object> responseData = (Map<String, Object>) response.getBody().getData();
                        log.info("Search response data: {}", responseData);
                        
                        java.util.List<Map<String, Object>> accounts = null;
                        
                        // Try different response structures
                        if (responseData != null) {
                            if (responseData.containsKey("accounts")) {
                                accounts = (java.util.List<Map<String, Object>>) responseData.get("accounts");
                            } else if (responseData.containsKey("content")) {
                                accounts = (java.util.List<Map<String, Object>>) responseData.get("content");
                            } else if (responseData instanceof java.util.List) {
                                accounts = (java.util.List<Map<String, Object>>) responseData;
                            }
                        }
                        
                        if (accounts != null && !accounts.isEmpty()) {
                            // Find exact match by name
                            for (Map<String, Object> account : accounts) {
                                String existingName = (String) account.get("accountName");
                                if (existingName != null && accountName.equalsIgnoreCase(existingName.trim())) {
                                    Object accountIdObj = account.get("accountId");
                                    if (accountIdObj != null) {
                                        Long accountId = Long.valueOf(accountIdObj.toString());
                                        log.info("Found existing account: name={}, accountId={}", existingName, accountId);
                                        return accountId;
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("Search attempt failed for url: {}, error: {}", url, e.getMessage());
                    continue; // Try next URL
                }
            }
            
            log.info("No existing account found with name: {}", accountName);
            return null;
            
        } catch (Exception e) {
            log.warn("Error searching for existing account: {}, error: {}", accountName, e.getMessage());
            return null;
        }
    }
    
    private String generateDealName(Lead lead) {
        StringBuilder dealName = new StringBuilder();
        
        // Add lead name
        if (lead.getFirstName() != null) {
            dealName.append(lead.getFirstName()).append(" ");
        }
        if (lead.getLastName() != null) {
            dealName.append(lead.getLastName());
        }
        
        // Add company if available
        if (lead.getCompany() != null && !lead.getCompany().trim().isEmpty()) {
            if (dealName.length() > 0) {
                dealName.append(" - ");
            }
            dealName.append(lead.getCompany());
        }
        
        // Add "Opportunity" suffix
        dealName.append(" - Opportunity");
        
        return dealName.toString();
    }
    
    private boolean isLeadReadyForConversion(LeadStatus status) {
        // Lead must be QUALIFIED or higher status to be converted
        // Status hierarchy: NEW < CONTACTED < QUALIFIED < CONVERTED
        // UNQUALIFIED, NURTURING, LOST are not eligible for conversion
        return status == LeadStatus.QUALIFIED || status == LeadStatus.CONVERTED;
    }
}
