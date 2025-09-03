package com.crmplatform.contacts.service;

import com.crmplatform.common.dto.ApiResponse;
import com.crmplatform.common.security.UserContext;
import com.crmplatform.contacts.dto.ContactResponse;
import com.crmplatform.contacts.dto.CreateContactRequest;
import com.crmplatform.contacts.entity.Account;
import com.crmplatform.contacts.entity.AccountContact;
import com.crmplatform.contacts.entity.Contact;
import com.crmplatform.contacts.repository.AccountRepository;
import com.crmplatform.contacts.repository.AccountContactRepository;
import com.crmplatform.contacts.repository.ContactRepository;
import com.crmplatform.contacts.entity.CustomFieldData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactService {
    
    private final ContactRepository contactRepository;
    private final AccountRepository accountRepository;
    private final AccountContactRepository accountContactRepository;
    private final CustomFieldService customFieldService;
    
    private static final Pattern EMAIL_DOMAIN_PATTERN = Pattern.compile("@(.+)$");
    
    @Transactional
    public ApiResponse<ContactResponse> createContact(CreateContactRequest request) {
        Long tenantId = UserContext.getCurrentTenantId();
        Long ownerUserId = UserContext.getCurrentUserId();
        
        // Check for existing contact with same email
        if (request.getPrimaryEmail() != null) {
            Optional<Contact> existingContact = contactRepository
                    .findByTenantIdAndPrimaryEmail(tenantId, request.getPrimaryEmail());
            
            if (existingContact.isPresent()) {
                log.warn("Contact with email {} already exists in tenant {}", 
                        request.getPrimaryEmail(), tenantId);
                return ApiResponse.error("Contact with this email already exists", "CONTACT_DUPLICATE");
            }
        }
        
        // Find or create account
        Account account = findOrCreateAccount(request.getAccountName(), tenantId, ownerUserId);
        
        // Create contact
        Contact contact = Contact.builder()
                .tenantId(tenantId)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .primaryEmail(request.getPrimaryEmail())
                .phoneNumber(request.getPhoneNumber())
                .jobTitle(request.getJobTitle())
                .ownerUserId(ownerUserId)
                .build();
        
        contact = contactRepository.save(contact);
        
        // Save custom fields
        if (request.getCustomFields() != null && !request.getCustomFields().isEmpty()) {
            customFieldService.saveCustomFields(tenantId, contact.getContactId(), 
                    CustomFieldData.EntityType.CONTACT, request.getCustomFields());
        }
        
        // Save account contact relationship
        if (account != null) {
            AccountContact accountContact = AccountContact.builder()
                    .id(AccountContact.AccountContactId.builder()
                            .accountId(account.getAccountId())
                            .contactId(contact.getContactId())
                            .tenantId(tenantId)
                            .build())
                    .build();
            accountContactRepository.save(accountContact);
        }
        
        // Events disabled for now
        log.info("Contact created successfully: {}", contact.getContactId());
        
        // Build response
        ContactResponse response = buildContactResponse(contact, account, request.getCustomFields());
        
        return ApiResponse.success(response, "Contact created successfully");
    }
    
    public ApiResponse<ContactResponse> getContact(Long contactId) {
        Long tenantId = UserContext.getCurrentTenantId();
        
        Optional<Contact> contactOpt = contactRepository.findByTenantIdAndContactId(tenantId, contactId);
        if (contactOpt.isEmpty()) {
            return ApiResponse.error("Contact not found", "CONTACT_NOT_FOUND");
        }
        
        Contact contact = contactOpt.get();
        
        // Get custom fields
        Map<String, String> customFields = customFieldService.getCustomFields(
                tenantId, contact.getContactId(), CustomFieldData.EntityType.CONTACT);
        
        // Get associated account details
        Account account = getAccountForContact(contact.getContactId(), tenantId);
        
        ContactResponse response = buildContactResponse(contact, account, customFields);
        return ApiResponse.success(response);
    }
    
    public ApiResponse<List<ContactResponse>> searchContacts(String searchTerm, int page, int size) {
        Long tenantId = UserContext.getCurrentTenantId();
        
        List<Contact> contacts;
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            contacts = contactRepository.searchContacts(tenantId, searchTerm.trim());
        } else {
            Pageable pageable = PageRequest.of(page, size);
            Page<Contact> contactPage = contactRepository.findByTenantId(tenantId, pageable);
            contacts = contactPage.getContent();
        }
        
        List<ContactResponse> responses = contacts.stream()
            .map(contact -> {
                // Get account for this contact
                Account account = getAccountForContact(contact.getContactId(), tenantId);
                // Get custom fields
                Map<String, String> customFields = customFieldService.getCustomFields(
                        tenantId, contact.getContactId(), CustomFieldData.EntityType.CONTACT);
                return buildContactResponse(contact, account, customFields);
            })
            .toList();
        
        return ApiResponse.success(responses);
    }
    
    @Transactional
    public ApiResponse<ContactResponse> updateContact(Long contactId, CreateContactRequest request) {
        Long tenantId = UserContext.getCurrentTenantId();
        Long ownerUserId = UserContext.getCurrentUserId();
        
        // Find existing contact
        Optional<Contact> contactOpt = contactRepository.findByTenantIdAndContactId(tenantId, contactId);
        if (contactOpt.isEmpty()) {
            return ApiResponse.error("Contact not found", "CONTACT_NOT_FOUND");
        }
        
        Contact contact = contactOpt.get();
        
        // Check for email conflicts (excluding current contact)
        if (request.getPrimaryEmail() != null && !request.getPrimaryEmail().equals(contact.getPrimaryEmail())) {
            Optional<Contact> existingContact = contactRepository
                    .findByTenantIdAndPrimaryEmail(tenantId, request.getPrimaryEmail());
            
            if (existingContact.isPresent()) {
                log.warn("Contact with email {} already exists in tenant {}", 
                        request.getPrimaryEmail(), tenantId);
                return ApiResponse.error("Contact with this email already exists", "CONTACT_DUPLICATE");
            }
        }
        
        // Update contact fields
        contact.setFirstName(request.getFirstName());
        contact.setLastName(request.getLastName());
        contact.setPrimaryEmail(request.getPrimaryEmail());
        contact.setPhoneNumber(request.getPhoneNumber());
        contact.setJobTitle(request.getJobTitle());
        
        contact = contactRepository.save(contact);
        
        // Update custom fields
        if (request.getCustomFields() != null) {
            customFieldService.saveCustomFields(tenantId, contact.getContactId(), 
                    CustomFieldData.EntityType.CONTACT, request.getCustomFields());
        }
        
        // Handle account association update
        Account account = null;
        if (request.getAccountName() != null && !request.getAccountName().trim().isEmpty()) {
            account = findOrCreateAccount(request.getAccountName(), tenantId, ownerUserId);
            
            // Remove existing account association
            accountContactRepository.deleteByContactIdAndTenantId(contact.getContactId(), tenantId);
            
            // Create new account association
            if (account != null) {
                AccountContact accountContact = AccountContact.builder()
                        .id(AccountContact.AccountContactId.builder()
                                .accountId(account.getAccountId())
                                .contactId(contact.getContactId())
                                .tenantId(tenantId)
                                .build())
                        .build();
                accountContactRepository.save(accountContact);
            }
        }
        
        // Get updated custom fields for response
        Map<String, String> customFields = customFieldService.getCustomFields(
                tenantId, contact.getContactId(), CustomFieldData.EntityType.CONTACT);
        
        // Build response
        ContactResponse response = buildContactResponse(contact, account, customFields);
        
        log.info("Contact updated successfully: {}", contact.getContactId());
        return ApiResponse.success(response, "Contact updated successfully");
    }
    
    private Account getAccountForContact(Long contactId, Long tenantId) {
        Optional<AccountContact> accountContactOpt = accountContactRepository
                .findByContactIdAndTenantId(contactId, tenantId);
        
        if (accountContactOpt.isPresent()) {
            Long accountId = accountContactOpt.get().getId().getAccountId();
            return accountRepository.findByTenantIdAndAccountId(tenantId, accountId).orElse(null);
        }
        
        return null;
    }
    
    private Account findOrCreateAccount(String accountName, Long tenantId, Long ownerUserId) {
        if (accountName == null || accountName.trim().isEmpty()) {
            return null;
        }
        
        // Try to find existing account by name
        Optional<Account> existingAccount = accountRepository
                .findByTenantIdAndAccountName(tenantId, accountName);
        
        if (existingAccount.isPresent()) {
            return existingAccount.get();
        }
        
        // Try to find by domain if email is provided
        String domain = extractDomainFromEmail(accountName);
        if (domain != null) {
            Optional<Account> accountByDomain = accountRepository
                    .findByTenantIdAndWebsiteOrAccountName(tenantId, domain, accountName);
            
            if (accountByDomain.isPresent()) {
                return accountByDomain.get();
            }
        }
        
        // Create new account
        Account newAccount = Account.builder()
                .tenantId(tenantId)
                .accountName(accountName)
                .ownerUserId(ownerUserId)
                .build();
        
        return accountRepository.save(newAccount);
    }
    
    private String extractDomainFromEmail(String email) {
        if (email == null || !email.contains("@")) {
            return null;
        }
        
        Matcher matcher = EMAIL_DOMAIN_PATTERN.matcher(email);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return null;
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
} 