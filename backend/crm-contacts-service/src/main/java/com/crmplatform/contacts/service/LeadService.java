package com.crmplatform.contacts.service;

import com.crmplatform.contacts.dto.ConvertLeadRequest;

import com.crmplatform.contacts.dto.CreateLeadRequest;
import com.crmplatform.contacts.dto.LeadResponse;
import com.crmplatform.contacts.entity.Account;
import com.crmplatform.contacts.entity.Contact;
import com.crmplatform.contacts.entity.Lead;
import com.crmplatform.contacts.repository.AccountRepository;
import com.crmplatform.contacts.repository.ContactRepository;
import com.crmplatform.contacts.repository.LeadRepository;

import com.crmplatform.common.dto.ApiResponse;
import com.crmplatform.common.security.UserContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeadService {
    
    private final LeadRepository leadRepository;
    private final ContactRepository contactRepository;
    private final AccountRepository accountRepository;
    
    public ApiResponse<Page<LeadResponse>> getLeads(String searchTerm, int page, int size) {
        try {
            Long tenantId = UserContext.getCurrentTenantId();
            if (tenantId == null) {
                return ApiResponse.error("UNAUTHORIZED", "Tenant context not found");
            }
            
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Lead> leads;
            
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                leads = leadRepository.findByTenantIdAndSearchTerm(tenantId, searchTerm.trim(), pageable);
            } else {
                leads = leadRepository.findByTenantId(tenantId, pageable);
            }
            
            Page<LeadResponse> leadResponses = leads.map(this::convertToResponse);
            return ApiResponse.success(leadResponses);
            
        } catch (Exception e) {
            log.error("Error fetching leads", e);
            return ApiResponse.error("FETCH_ERROR", "Failed to fetch leads");
        }
    }
    
    public ApiResponse<LeadResponse> getLeadById(Long leadId) {
        try {
            Long tenantId = UserContext.getCurrentTenantId();
            if (tenantId == null) {
                return ApiResponse.error("UNAUTHORIZED", "Tenant context not found");
            }
            
            Optional<Lead> leadOpt = leadRepository.findByLeadIdAndTenantId(leadId, tenantId);
            if (leadOpt.isEmpty()) {
                return ApiResponse.error("NOT_FOUND", "Lead not found");
            }
            
            LeadResponse response = convertToResponse(leadOpt.get());
            return ApiResponse.success(response);
            
        } catch (Exception e) {
            log.error("Error fetching lead by ID: {}", leadId, e);
            return ApiResponse.error("FETCH_ERROR", "Failed to fetch lead");
        }
    }
    
    public ApiResponse<LeadResponse> createLead(CreateLeadRequest request) {
        try {
            Long tenantId = UserContext.getCurrentTenantId();
            Long userId = UserContext.getCurrentUserId();
            
            if (tenantId == null || userId == null) {
                return ApiResponse.error("UNAUTHORIZED", "User context not found");
            }
            
            // Check for duplicate email
            if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                if (leadRepository.existsByEmailAndTenantId(request.getEmail(), tenantId)) {
                    return ApiResponse.error("LEAD_DUPLICATE", "Lead with this email already exists");
                }
            }
            
            Lead lead = Lead.builder()
                    .tenantId(tenantId)
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .email(request.getEmail())
                    .phoneNumber(request.getPhoneNumber())
                    .company(request.getCompany())
                    .jobTitle(request.getJobTitle())
                    .leadSource(request.getLeadSource() != null ? request.getLeadSource() : Lead.LeadSource.OTHER)
                    .leadStatus(request.getLeadStatus() != null ? request.getLeadStatus() : Lead.LeadStatus.NEW)
                    .leadScore(request.getLeadScore() != null ? request.getLeadScore() : calculateInitialScore(request))
                    .industry(request.getIndustry())
                    .website(request.getWebsite())
                    .annualRevenue(request.getAnnualRevenue())
                    .employeeCount(request.getEmployeeCount())
                    .notes(request.getNotes())
                    .ownerUserId(userId)
                    .createdBy(userId)
                    .build();
            
            Lead savedLead = leadRepository.save(lead);
            LeadResponse response = convertToResponse(savedLead);
            
            log.info("Lead created successfully with ID: {}", savedLead.getLeadId());
            return ApiResponse.success(response);
            
        } catch (Exception e) {
            log.error("Error creating lead", e);
            return ApiResponse.error("CREATE_ERROR", "Failed to create lead");
        }
    }
    
    public ApiResponse<LeadResponse> updateLead(Long leadId, CreateLeadRequest request) {
        try {
            Long tenantId = UserContext.getCurrentTenantId();
            if (tenantId == null) {
                return ApiResponse.error("UNAUTHORIZED", "Tenant context not found");
            }
            
            Optional<Lead> leadOpt = leadRepository.findByLeadIdAndTenantId(leadId, tenantId);
            if (leadOpt.isEmpty()) {
                return ApiResponse.error("NOT_FOUND", "Lead not found");
            }
            
            Lead lead = leadOpt.get();
            
            // Check for duplicate email if email is being changed
            if (request.getEmail() != null && !request.getEmail().equals(lead.getEmail())) {
                if (leadRepository.existsByEmailAndTenantId(request.getEmail(), tenantId)) {
                    return ApiResponse.error("LEAD_DUPLICATE", "Lead with this email already exists");
                }
            }
            
            // Update lead fields
            lead.setFirstName(request.getFirstName());
            lead.setLastName(request.getLastName());
            lead.setEmail(request.getEmail());
            lead.setPhoneNumber(request.getPhoneNumber());
            lead.setCompany(request.getCompany());
            lead.setJobTitle(request.getJobTitle());
            lead.setLeadSource(request.getLeadSource());
            lead.setLeadStatus(request.getLeadStatus());
            lead.setLeadScore(request.getLeadScore() != null ? request.getLeadScore() : calculateInitialScore(request));
            lead.setIndustry(request.getIndustry());
            lead.setWebsite(request.getWebsite());
            lead.setAnnualRevenue(request.getAnnualRevenue());
            lead.setEmployeeCount(request.getEmployeeCount());
            lead.setNotes(request.getNotes());
            
            Lead savedLead = leadRepository.save(lead);
            LeadResponse response = convertToResponse(savedLead);
            
            log.info("Lead updated successfully with ID: {}", leadId);
            return ApiResponse.success(response);
            
        } catch (Exception e) {
            log.error("Error updating lead with ID: {}", leadId, e);
            return ApiResponse.error("UPDATE_ERROR", "Failed to update lead");
        }
    }
    
    @Transactional
    public ApiResponse<LeadResponse> convertLead(ConvertLeadRequest request) {
        try {
            Long tenantId = UserContext.getCurrentTenantId();
            Long userId = UserContext.getCurrentUserId();
            
            if (tenantId == null || userId == null) {
                return ApiResponse.error("UNAUTHORIZED", "User context not found");
            }
            
            Optional<Lead> leadOpt = leadRepository.findByLeadIdAndTenantId(request.getLeadId(), tenantId);
            if (leadOpt.isEmpty()) {
                return ApiResponse.error("NOT_FOUND", "Lead not found");
            }
            
            Lead lead = leadOpt.get();
            
            if (lead.isConverted()) {
                return ApiResponse.error("ALREADY_CONVERTED", "Lead is already converted");
            }
            
            Long accountId = null;
            
            // Create account if requested
            if (request.isCreateAccount() && request.getAccountName() != null && !request.getAccountName().trim().isEmpty()) {
                Account account = Account.builder()
                        .tenantId(tenantId)
                        .accountName(request.getAccountName())
                        .website(request.getAccountWebsite() != null ? request.getAccountWebsite() : lead.getWebsite())
                        .industry(request.getAccountIndustry() != null ? request.getAccountIndustry() : lead.getIndustry())
                        .ownerUserId(userId)
                        .build();
                
                Account savedAccount = accountRepository.save(account);
                accountId = savedAccount.getAccountId();
                log.info("Account created during lead conversion with ID: {}", accountId);
            }
            
            // Create contact from lead
            Contact contact = Contact.builder()
                    .tenantId(tenantId)
                    .firstName(lead.getFirstName())
                    .lastName(lead.getLastName())
                    .primaryEmail(lead.getEmail())
                    .phoneNumber(lead.getPhoneNumber())
                    .jobTitle(lead.getJobTitle())
                    .ownerUserId(userId)
                    .build();
            
            Contact savedContact = contactRepository.save(contact);
            log.info("Contact created during lead conversion with ID: {}", savedContact.getContactId());
            
            // Update lead as converted
            lead.setLeadStatus(Lead.LeadStatus.CONVERTED);
            lead.setConvertedContactId(savedContact.getContactId());
            lead.setConvertedAccountId(accountId);
            lead.setConvertedAt(LocalDateTime.now());
            
            Lead savedLead = leadRepository.save(lead);
            LeadResponse response = convertToResponse(savedLead);
            
            log.info("Lead converted successfully with ID: {}", lead.getLeadId());
            return ApiResponse.success(response);
            
        } catch (Exception e) {
            log.error("Error converting lead with ID: {}", request.getLeadId(), e);
            return ApiResponse.error("CONVERT_ERROR", "Failed to convert lead");
        }
    }
    
    public ApiResponse<Void> deleteLead(Long leadId) {
        try {
            Long tenantId = UserContext.getCurrentTenantId();
            if (tenantId == null) {
                return ApiResponse.error("UNAUTHORIZED", "Tenant context not found");
            }
            
            Optional<Lead> leadOpt = leadRepository.findByLeadIdAndTenantId(leadId, tenantId);
            if (leadOpt.isEmpty()) {
                return ApiResponse.error("NOT_FOUND", "Lead not found");
            }
            
            leadRepository.deleteById(leadId);
            log.info("Lead deleted successfully with ID: {}", leadId);
            return ApiResponse.success(null);
            
        } catch (Exception e) {
            log.error("Error deleting lead with ID: {}", leadId, e);
            return ApiResponse.error("DELETE_ERROR", "Failed to delete lead");
        }
    }
    
    public ApiResponse<List<LeadResponse>> getMyLeads() {
        try {
            Long tenantId = UserContext.getCurrentTenantId();
            Long userId = UserContext.getCurrentUserId();
            
            if (tenantId == null || userId == null) {
                return ApiResponse.error("UNAUTHORIZED", "User context not found");
            }
            
            List<Lead> leads = leadRepository.findByTenantIdAndOwnerUserId(tenantId, userId);
            List<LeadResponse> responses = leads.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(responses);
            
        } catch (Exception e) {
            log.error("Error fetching user's leads", e);
            return ApiResponse.error("FETCH_ERROR", "Failed to fetch your leads");
        }
    }
    
    private LeadResponse convertToResponse(Lead lead) {
        return LeadResponse.builder()
                .leadId(lead.getLeadId())
                .tenantId(lead.getTenantId())
                .firstName(lead.getFirstName())
                .lastName(lead.getLastName())
                .email(lead.getEmail())
                .phoneNumber(lead.getPhoneNumber())
                .company(lead.getCompany())
                .jobTitle(lead.getJobTitle())
                .leadSource(lead.getLeadSource())
                .leadStatus(lead.getLeadStatus())
                .leadScore(lead.getLeadScore())
                .industry(lead.getIndustry())
                .website(lead.getWebsite())
                .annualRevenue(lead.getAnnualRevenue())
                .employeeCount(lead.getEmployeeCount())
                .notes(lead.getNotes())
                .ownerUserId(lead.getOwnerUserId())
                .convertedContactId(lead.getConvertedContactId())
                .convertedAccountId(lead.getConvertedAccountId())
                .convertedAt(lead.getConvertedAt())
                .createdAt(lead.getCreatedAt())
                .updatedAt(lead.getUpdatedAt())
                .createdBy(lead.getCreatedBy())
                .build();
    }
    
    private Integer calculateInitialScore(CreateLeadRequest request) {
        int score = 0;
        
        // Email provided
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            score += 10;
        }
        
        // Phone provided
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            score += 10;
        }
        
        // Company provided
        if (request.getCompany() != null && !request.getCompany().trim().isEmpty()) {
            score += 15;
        }
        
        // Job title provided
        if (request.getJobTitle() != null && !request.getJobTitle().trim().isEmpty()) {
            score += 10;
        }
        
        // Website provided
        if (request.getWebsite() != null && !request.getWebsite().trim().isEmpty()) {
            score += 5;
        }
        
        // Annual revenue provided
        if (request.getAnnualRevenue() != null && request.getAnnualRevenue() > 0) {
            score += 20;
        }
        
        // Employee count provided
        if (request.getEmployeeCount() != null && request.getEmployeeCount() > 0) {
            score += 10;
        }
        
        // Lead source bonus
        if (request.getLeadSource() != null) {
            switch (request.getLeadSource()) {
                case REFERRAL:
                    score += 20;
                    break;
                case WEBSITE:
                case CONTENT_DOWNLOAD:
                    score += 15;
                    break;
                case WEBINAR:
                case TRADE_SHOW:
                    score += 10;
                    break;
                default:
                    score += 5;
            }
        }
        
        return Math.min(score, 100); // Cap at 100
    }
}
