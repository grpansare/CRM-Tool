package com.crmplatform.contacts.service;

import com.crmplatform.common.dto.ApiResponse;
import com.crmplatform.common.security.UserContext;
import com.crmplatform.contacts.dto.AccountResponse;
import com.crmplatform.contacts.dto.ContactResponse;
import com.crmplatform.contacts.dto.CreateAccountRequest;
import com.crmplatform.contacts.entity.Account;
import com.crmplatform.contacts.entity.AccountContact;
import com.crmplatform.contacts.entity.Contact;
import com.crmplatform.contacts.entity.CustomFieldData;
import com.crmplatform.contacts.repository.AccountContactRepository;
import com.crmplatform.contacts.repository.AccountRepository;
import com.crmplatform.contacts.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {
    
    private final AccountRepository accountRepository;
    private final AccountContactRepository accountContactRepository;
    private final ContactRepository contactRepository;
    private final CustomFieldService customFieldService;
    
    public ApiResponse<AccountResponse> getAccount(Long accountId) {
        Long tenantId = UserContext.getCurrentTenantId();
        
        Optional<Account> accountOpt = accountRepository.findByTenantIdAndAccountId(tenantId, accountId);
        if (accountOpt.isEmpty()) {
            return ApiResponse.error("Account not found", "ACCOUNT_NOT_FOUND");
        }
        
        Account account = accountOpt.get();
        
        // Get custom fields
        var customFields = customFieldService.getCustomFields(
                tenantId, account.getAccountId(), CustomFieldData.EntityType.ACCOUNT);
        
        AccountResponse response = buildAccountResponse(account, customFields);
        return ApiResponse.success(response);
    }
    
    public ApiResponse<List<AccountResponse>> searchAccounts(String searchTerm, int page, int size) {
        Long tenantId = UserContext.getCurrentTenantId();
        
        List<Account> accounts;
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            accounts = accountRepository.searchAccounts(tenantId, searchTerm.trim());
        } else {
            Pageable pageable = PageRequest.of(page, size);
            Page<Account> accountPage = accountRepository.findByTenantId(tenantId, pageable);
            accounts = accountPage.getContent();
        }
        
        List<AccountResponse> responses = accounts.stream()
                .map(account -> buildAccountResponse(account, null))
                .toList();
        
        return ApiResponse.success(responses);
    }
    
    public ApiResponse<List<ContactResponse>> getAccountContacts(Long accountId) {
        Long tenantId = UserContext.getCurrentTenantId();
        
        // Verify account exists and belongs to tenant
        Optional<Account> accountOpt = accountRepository.findByTenantIdAndAccountId(tenantId, accountId);
        if (accountOpt.isEmpty()) {
            return ApiResponse.error("Account not found", "ACCOUNT_NOT_FOUND");
        }
        
        // Get all contacts for this account
        List<AccountContact> accountContacts = accountContactRepository.findByAccountIdAndTenantId(accountId, tenantId);
        
        List<ContactResponse> contactResponses = accountContacts.stream()
                .map(ac -> {
                    Long contactId = ac.getId().getContactId();
                    Optional<Contact> contactOpt = contactRepository.findByTenantIdAndContactId(tenantId, contactId);
                    if (contactOpt.isPresent()) {
                        Contact contact = contactOpt.get();
                        // Get custom fields
                        Map<String, String> customFields = customFieldService.getCustomFields(
                                tenantId, contact.getContactId(), CustomFieldData.EntityType.CONTACT);
                        return buildContactResponse(contact, accountOpt.get(), customFields);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
        
        return ApiResponse.success(contactResponses);
    }
    
    private ContactResponse buildContactResponse(Contact contact, Account account, Map<String, String> customFields) {
        ContactResponse.AccountResponse accountResponse = null;
        if (account != null) {
            accountResponse = ContactResponse.AccountResponse.builder()
                    .accountId(account.getAccountId())
                    .accountName(account.getAccountName())
                    .website(account.getWebsite())
                    .industry(account.getIndustry())
                    .ownerUserId(account.getOwnerUserId())
                    .createdAt(account.getCreatedAt())
                    .build();
        }
        
        return ContactResponse.builder()
                .contactId(contact.getContactId())
                .firstName(contact.getFirstName())
                .lastName(contact.getLastName())
                .primaryEmail(contact.getPrimaryEmail())
                .phoneNumber(contact.getPhoneNumber())
                .jobTitle(contact.getJobTitle())
                .ownerUserId(contact.getOwnerUserId())
                .createdAt(contact.getCreatedAt())
                .account(accountResponse)
                .customFields(customFields)
                .build();
    }
    
    @Transactional
    public ApiResponse<AccountResponse> createAccount(CreateAccountRequest request) {
        Long tenantId = UserContext.getCurrentTenantId();
        Long ownerUserId = UserContext.getCurrentUserId();
        
        // Check for existing account with same name
        Optional<Account> existingAccount = accountRepository
                .findByTenantIdAndAccountName(tenantId, request.getAccountName());
        
        if (existingAccount.isPresent()) {
            log.warn("Account with name {} already exists in tenant {}", 
                    request.getAccountName(), tenantId);
            return ApiResponse.error("Account with this name already exists", "ACCOUNT_DUPLICATE");
        }
        
        // Create account
        Account account = Account.builder()
                .tenantId(tenantId)
                .accountName(request.getAccountName())
                .website(request.getWebsite())
                .industry(request.getIndustry())
                .ownerUserId(ownerUserId)
                .build();
        
        account = accountRepository.save(account);
        
        // Save custom fields
        if (request.getCustomFields() != null && !request.getCustomFields().isEmpty()) {
            customFieldService.saveCustomFields(tenantId, account.getAccountId(), 
                    CustomFieldData.EntityType.ACCOUNT, request.getCustomFields());
        }
        
        // Build response
        AccountResponse response = buildAccountResponse(account, request.getCustomFields());
        
        return ApiResponse.success(response, "Account created successfully");
    }
    
    @Transactional
    public ApiResponse<AccountResponse> updateAccount(Long accountId, CreateAccountRequest request) {
        Long tenantId = UserContext.getCurrentTenantId();
        Long ownerUserId = UserContext.getCurrentUserId();
        
        // Find existing account
        Optional<Account> accountOpt = accountRepository.findByTenantIdAndAccountId(tenantId, accountId);
        if (accountOpt.isEmpty()) {
            return ApiResponse.error("Account not found", "ACCOUNT_NOT_FOUND");
        }
        
        Account account = accountOpt.get();
        
        // Check if user has permission to update (owner or admin)
        if (!account.getOwnerUserId().equals(ownerUserId)) {
            // TODO: Add role-based permission check for admins
            log.warn("User {} attempted to update account {} owned by {}", 
                    ownerUserId, accountId, account.getOwnerUserId());
            return ApiResponse.error("Insufficient permissions", "INSUFFICIENT_PERMISSIONS");
        }
        
        // Check for duplicate name (excluding current account)
        Optional<Account> existingAccount = accountRepository
                .findByTenantIdAndAccountName(tenantId, request.getAccountName());
        
        if (existingAccount.isPresent() && !existingAccount.get().getAccountId().equals(accountId)) {
            return ApiResponse.error("Account with this name already exists", "ACCOUNT_DUPLICATE");
        }
        
        // Update account fields
        account.setAccountName(request.getAccountName());
        account.setWebsite(request.getWebsite());
        account.setIndustry(request.getIndustry());
        
        account = accountRepository.save(account);
        
        // Update custom fields
        if (request.getCustomFields() != null) {
            customFieldService.saveCustomFields(tenantId, account.getAccountId(), 
                    CustomFieldData.EntityType.ACCOUNT, request.getCustomFields());
        }
        
        // Build response with updated custom fields
        Map<String, String> customFields = customFieldService.getCustomFields(
                tenantId, account.getAccountId(), CustomFieldData.EntityType.ACCOUNT);
        
        AccountResponse response = buildAccountResponse(account, customFields);
        
        return ApiResponse.success(response, "Account updated successfully");
    }
    
    private AccountResponse buildAccountResponse(Account account, java.util.Map<String, String> customFields) {
        return AccountResponse.builder()
                .accountId(account.getAccountId())
                .accountName(account.getAccountName())
                .website(account.getWebsite())
                .industry(account.getIndustry())
                .ownerUserId(account.getOwnerUserId())
                .createdAt(account.getCreatedAt())
                .customFields(customFields)
                .contacts(null) // TODO: Add contact relationships when implemented
                .build();
    }
} 