package com.crmplatform.sales.service;

import com.crmplatform.sales.dto.CreateLeadRequest;
import com.crmplatform.sales.dto.LeadResponse;
import com.crmplatform.sales.dto.UpdateLeadRequest;
import com.crmplatform.sales.dto.SetDispositionRequest;
import com.crmplatform.sales.dto.DispositionHistoryResponse;
import com.crmplatform.sales.entity.Lead;
import com.crmplatform.sales.entity.LeadStatus;
import com.crmplatform.sales.entity.LeadSource;
import com.crmplatform.sales.entity.LeadDisposition;
import com.crmplatform.sales.repository.LeadRepository;
import com.crmplatform.common.security.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LeadService {
    
    @Autowired
    private LeadRepository leadRepository;
    
    @Autowired
    private LeadActivityService leadActivityService;
    
    @Autowired
    private LeadScoringService leadScoringService;
    
    public Page<LeadResponse> getAllLeads(int page, int size, String searchTerm) {
        Long tenantId = UserContext.getCurrentTenantId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<Lead> leads;
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            leads = leadRepository.searchLeads(tenantId, searchTerm.trim(), pageable);
        } else {
            leads = leadRepository.findByTenantId(tenantId, pageable);
        }
        
        return leads.map(LeadResponse::new);
    }
    
    public LeadResponse getLeadById(Long leadId) {
        Long tenantId = UserContext.getCurrentTenantId();
        Lead lead = leadRepository.findByLeadIdAndTenantId(leadId, tenantId)
                .orElseThrow(() -> new RuntimeException("Lead not found"));
        return new LeadResponse(lead);
    }
    
    public LeadResponse createLead(CreateLeadRequest request) {
        Long tenantId = UserContext.getCurrentTenantId();
        Long userId = UserContext.getCurrentUserId();
        
        // Check for duplicate email if provided
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (leadRepository.existsByEmailAndTenantId(request.getEmail().trim(), tenantId)) {
                throw new RuntimeException("A lead with this email already exists");
            }
        }
        
        Lead lead = new Lead();
        lead.setTenantId(tenantId);
        lead.setFirstName(request.getFirstName());
        lead.setLastName(request.getLastName());
        lead.setEmail(request.getEmail());
        lead.setPhoneNumber(request.getPhoneNumber());
        lead.setCompany(request.getCompany());
        lead.setJobTitle(request.getJobTitle());
        lead.setLeadSource(request.getLeadSource());
        lead.setNotes(request.getNotes());
        lead.setEmployeeCount(request.getEmployeeCount());
        lead.setAnnualRevenue(request.getAnnualRevenue());
        lead.setIndustry(request.getIndustry());
        lead.setOwnerUserId(userId);
        lead.setLeadStatus(LeadStatus.NEW);
        
        // Calculate initial lead score
        int calculatedScore = leadScoringService.calculateLeadScore(lead);
        lead.setLeadScore(calculatedScore);
        
        Lead savedLead = leadRepository.save(lead);
        
        // Log lead creation activity
        String leadName = (savedLead.getFirstName() != null ? savedLead.getFirstName() + " " : "") + savedLead.getLastName();
        String source = savedLead.getLeadSource() != null ? savedLead.getLeadSource().toString() : "UNKNOWN";
        leadActivityService.logLeadCreated(savedLead.getLeadId(), leadName, source);
        
        return new LeadResponse(savedLead);
    }
    
    public LeadResponse updateLead(Long leadId, UpdateLeadRequest request) {
        Long tenantId = UserContext.getCurrentTenantId();
        Lead lead = leadRepository.findByLeadIdAndTenantId(leadId, tenantId)
                .orElseThrow(() -> new RuntimeException("Lead not found"));
        
        // Check for duplicate email if email is being updated
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty() && 
            !request.getEmail().equals(lead.getEmail())) {
            if (leadRepository.existsByEmailAndTenantId(request.getEmail().trim(), tenantId)) {
                throw new RuntimeException("A lead with this email already exists");
            }
        }
        
        // Track status change for activity logging
        LeadStatus oldStatus = lead.getLeadStatus();
        String leadName = (lead.getFirstName() != null ? lead.getFirstName() + " " : "") + lead.getLastName();
        
        // Update fields if they are not null in the request
        if (request.getFirstName() != null) lead.setFirstName(request.getFirstName());
        if (request.getLastName() != null) lead.setLastName(request.getLastName());
        if (request.getEmail() != null) lead.setEmail(request.getEmail());
        if (request.getPhoneNumber() != null) lead.setPhoneNumber(request.getPhoneNumber());
        if (request.getCompany() != null) lead.setCompany(request.getCompany());
        if (request.getJobTitle() != null) lead.setJobTitle(request.getJobTitle());
        if (request.getLeadSource() != null) lead.setLeadSource(request.getLeadSource());
        if (request.getLeadStatus() != null) lead.setLeadStatus(request.getLeadStatus());
        if (request.getNotes() != null) lead.setNotes(request.getNotes());
        if (request.getEmployeeCount() != null) lead.setEmployeeCount(request.getEmployeeCount());
        if (request.getAnnualRevenue() != null) lead.setAnnualRevenue(request.getAnnualRevenue());
        if (request.getIndustry() != null) lead.setIndustry(request.getIndustry());
        if (request.getLeadScore() != null) {
            lead.setLeadScore(request.getLeadScore()); // Manual override
        } else {
            // Auto-calculate score based on updated lead data
            int calculatedScore = leadScoringService.calculateLeadScore(lead);
            lead.setLeadScore(calculatedScore);
        }
        if (request.getNotes() != null) {
            lead.setNotes(request.getNotes());
        }
        
        Lead updatedLead = leadRepository.save(lead);
        
        // Log status change activity if status was updated
        if (request.getLeadStatus() != null && !oldStatus.equals(request.getLeadStatus())) {
            leadActivityService.logLeadStatusChange(updatedLead.getLeadId(), leadName, 
                oldStatus.toString(), request.getLeadStatus().toString());
        } else {
            // Log general update activity
            leadActivityService.logLeadUpdated(updatedLead.getLeadId(), leadName, "Lead information updated");
        }
        
        return new LeadResponse(updatedLead);
    }
    
    public void deleteLead(Long leadId) {
        Long tenantId = UserContext.getCurrentTenantId();
        Lead lead = leadRepository.findByLeadIdAndTenantId(leadId, tenantId)
                .orElseThrow(() -> new RuntimeException("Lead not found"));
        
        // Log deletion activity before deleting
        String leadName = (lead.getFirstName() != null ? lead.getFirstName() + " " : "") + lead.getLastName();
        leadActivityService.logLeadDeleted(leadId, leadName);
        
        leadRepository.delete(lead);
    }
    
    public Page<LeadResponse> getLeadsByStatus(LeadStatus status, int page, int size) {
        Long tenantId = UserContext.getCurrentTenantId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Lead> leads = leadRepository.findByLeadStatusAndTenantId(status, tenantId, pageable);
        return leads.map(LeadResponse::new);
    }
    
    public Page<LeadResponse> getLeadsBySource(LeadSource source, int page, int size) {
        Long tenantId = UserContext.getCurrentTenantId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Lead> leads = leadRepository.findByLeadSourceAndTenantId(source, tenantId, pageable);
        return leads.map(LeadResponse::new);
    }
    
    public Page<LeadResponse> getMyLeads(int page, int size) {
        Long tenantId = UserContext.getCurrentTenantId();
        Long userId = UserContext.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Lead> leads = leadRepository.findByOwnerUserIdAndTenantId(userId, tenantId, pageable);
        return leads.map(LeadResponse::new);
    }
    
    public long getLeadCountByStatus(LeadStatus status) {
        Long tenantId = UserContext.getCurrentTenantId();
        Pageable pageable = PageRequest.of(0, 1);
        return leadRepository.findByTenantIdAndLeadStatus(tenantId, status, pageable).getTotalElements();
    }
    
    public LeadResponse recalculateLeadScore(Long leadId) {
        Long tenantId = UserContext.getCurrentTenantId();
        
        Lead lead = leadRepository.findByLeadIdAndTenantId(leadId, tenantId)
                .orElseThrow(() -> new RuntimeException("Lead not found"));
        
        // Recalculate score
        int newScore = leadScoringService.calculateLeadScore(lead);
        lead.setLeadScore(newScore);
        
        Lead updatedLead = leadRepository.save(lead);
        
        // Log score update activity
        String leadName = (lead.getFirstName() != null ? lead.getFirstName() + " " : "") + lead.getLastName();
        leadActivityService.logLeadScoreUpdate(leadId, leadName, newScore);
        
        return new LeadResponse(updatedLead);
    }
    
    public long getTotalLeadCount() {
        Long tenantId = UserContext.getCurrentTenantId();
        return leadRepository.countByTenantId(tenantId);
    }
    
    public List<LeadResponse> getRecentLeads(int days) {
        Long tenantId = UserContext.getCurrentTenantId();
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<Lead> leads = leadRepository.findRecentLeads(tenantId, startDate);
        return leads.stream().map(LeadResponse::new).collect(Collectors.toList());
    }
    
    // Lead Disposition Management
    public LeadResponse setLeadDisposition(Long leadId, SetDispositionRequest request) {
        Long tenantId = UserContext.getCurrentTenantId();
        Long userId = UserContext.getCurrentUserId();
        
        Lead lead = leadRepository.findByLeadIdAndTenantId(leadId, tenantId)
                .orElseThrow(() -> new RuntimeException("Lead not found"));
        
        // Store old disposition for activity logging
        LeadDisposition oldDisposition = lead.getCurrentDisposition();
        String leadName = (lead.getFirstName() != null ? lead.getFirstName() + " " : "") + lead.getLastName();
        
        // Update disposition fields
        lead.setCurrentDisposition(request.getDisposition());
        lead.setDispositionNotes(request.getNotes());
        lead.setNextFollowUpDate(request.getNextFollowUpDate());
        lead.setDispositionUpdatedAt(LocalDateTime.now());
        lead.setDispositionUpdatedBy(userId);
        lead.setLastContactDate(LocalDateTime.now());
        
        // Update lead status based on disposition
        updateLeadStatusBasedOnDisposition(lead, request.getDisposition());
        
        Lead updatedLead = leadRepository.save(lead);
        
        // Log disposition change activity
        leadActivityService.logDispositionUpdate(leadId, leadName, request.getDisposition(), request.getNotes());
        
        return new LeadResponse(updatedLead);
    }
    
    private void updateLeadStatusBasedOnDisposition(Lead lead, LeadDisposition disposition) {
        // Auto-update lead status based on disposition
        switch (disposition) {
            case CONVERTED:
                lead.setLeadStatus(LeadStatus.CONVERTED);
                break;
            case NOT_INTERESTED:
            case NOT_QUALIFIED:
            case DO_NOT_CALL:
            case LOST:
                lead.setLeadStatus(LeadStatus.LOST);
                break;
            case INTERESTED:
            case MEETING_SCHEDULED:
            case DEMO_REQUESTED:
            case PROPOSAL_SENT:
                lead.setLeadStatus(LeadStatus.QUALIFIED);
                break;
            case CALL_BACK_LATER:
            case NO_ANSWER:
            case VOICEMAIL_LEFT:
            case EMAIL_SENT:
            case BUSY:
                lead.setLeadStatus(LeadStatus.CONTACTED);
                break;
            default:
                // Keep current status for other dispositions
                break;
        }
    }
    
    // Get current disposition for a lead
    public DispositionHistoryResponse getCurrentDisposition(Long leadId) {
        Long tenantId = UserContext.getCurrentTenantId();
        
        Lead lead = leadRepository.findByLeadIdAndTenantId(leadId, tenantId)
                .orElseThrow(() -> new RuntimeException("Lead not found"));
        
        if (lead.getCurrentDisposition() == null) {
            return null;
        }
        
        return new DispositionHistoryResponse(
            lead.getCurrentDisposition(),
            lead.getDispositionNotes(),
            lead.getLastContactDate(),
            lead.getNextFollowUpDate(),
            lead.getDispositionUpdatedAt(),
            lead.getDispositionUpdatedBy(),
            null // updatedByName - would need user service lookup
        );
    }
    
    // Get leads requiring follow-up
    public List<LeadResponse> getLeadsRequiringFollowUp() {
        Long tenantId = UserContext.getCurrentTenantId();
        LocalDateTime currentDate = LocalDateTime.now();
        
        List<Lead> leads = leadRepository.findLeadsRequiringFollowUp(tenantId, currentDate);
        return leads.stream().map(LeadResponse::new).collect(Collectors.toList());
    }
    
    // Get leads by disposition
    public List<LeadResponse> getLeadsByDisposition(LeadDisposition disposition) {
        Long tenantId = UserContext.getCurrentTenantId();
        
        List<Lead> leads = leadRepository.findByTenantIdAndCurrentDisposition(tenantId, disposition);
        return leads.stream().map(LeadResponse::new).collect(Collectors.toList());
    }
}
