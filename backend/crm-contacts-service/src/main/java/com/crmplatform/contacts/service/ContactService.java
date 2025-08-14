package com.crmplatform.contacts.service;

import com.crmplatform.common.dto.ApiResponse;
import com.crmplatform.common.security.UserContext;
import com.crmplatform.contacts.dto.ContactResponse;
import com.crmplatform.contacts.dto.CreateContactRequest;
import com.crmplatform.contacts.entity.Account;
import com.crmplatform.contacts.entity.AccountContact;
import com.crmplatform.contacts.entity.Contact;
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
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactService {
    
    private final ContactRepository contactRepository;
    private final AccountRepository accountRepository;
    private final EventPublisher eventPublisher;
    
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
        
        // Create account-contact relationship
        AccountContact accountContact = AccountContact.builder()
                .id(AccountContact.AccountContactId.builder()
                        .accountId(account.getAccountId())
                        .contactId(contact.getContactId())
                        .tenantId(tenantId)
                        .build())
                .build();
        
        // TODO: Save account contact relationship when repository is created
        
        // Publish events (DISABLED - RabbitMQ not required)
        // eventPublisher.publishContactCreated(contact);
        // if (account.getCreatedAt().equals(contact.getCreatedAt())) {
        //     eventPublisher.publishAccountCreated(account);
        // }
        
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
        
        // TODO: Get associated account details
        Account account = null; // This would need to be fetched from account_contacts relationship
        
        ContactResponse response = buildContactResponse(contact, account, null);
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
                .map(contact -> buildContactResponse(contact, null, null))
                .toList();
        
        return ApiResponse.success(responses);
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