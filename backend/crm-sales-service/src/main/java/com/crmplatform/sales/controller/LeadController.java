package com.crmplatform.sales.controller;

import com.crmplatform.sales.dto.CreateLeadRequest;
import com.crmplatform.sales.dto.LeadResponse;
import com.crmplatform.sales.dto.UpdateLeadRequest;
import com.crmplatform.sales.dto.ConvertLeadRequest;
import com.crmplatform.sales.dto.ConvertLeadResponse;
import com.crmplatform.sales.entity.Lead;
import com.crmplatform.sales.entity.LeadSource;
import com.crmplatform.sales.entity.LeadStatus;
import com.crmplatform.sales.service.LeadService;
import com.crmplatform.sales.service.LeadConversionService;
import com.crmplatform.sales.service.LeadScoringService;
import com.crmplatform.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/leads")
public class LeadController {
    
    @Autowired
    private LeadService leadService;
    
    @Autowired
    private LeadConversionService leadConversionService;
    
    @Autowired
    private LeadScoringService leadScoringService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<Page<LeadResponse>>> getAllLeads(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String searchTerm) {
        try {
            Page<LeadResponse> leads = leadService.getAllLeads(page, size, searchTerm);
            return ResponseEntity.ok(ApiResponse.success(leads));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch leads: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{leadId}")
    public ResponseEntity<ApiResponse<LeadResponse>> getLeadById(@PathVariable Long leadId) {
        try {
            LeadResponse lead = leadService.getLeadById(leadId);
            return ResponseEntity.ok(ApiResponse.success(lead));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch lead: " + e.getMessage()));
        }
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<LeadResponse>> createLead(@Valid @RequestBody CreateLeadRequest request) {
        try {
            LeadResponse lead = leadService.createLead(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(lead));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create lead: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{leadId}")
    public ResponseEntity<ApiResponse<LeadResponse>> updateLead(
            @PathVariable Long leadId, 
            @Valid @RequestBody UpdateLeadRequest request) {
        try {
            LeadResponse lead = leadService.updateLead(leadId, request);
            return ResponseEntity.ok(ApiResponse.success(lead));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update lead: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{leadId}")
    public ResponseEntity<ApiResponse<Void>> deleteLead(@PathVariable Long leadId) {
        try {
            leadService.deleteLead(leadId);
            return ResponseEntity.ok(ApiResponse.success(null, "Lead deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete lead: " + e.getMessage()));
        }
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<Page<LeadResponse>>> getLeadsByStatus(
            @PathVariable LeadStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<LeadResponse> leads = leadService.getLeadsByStatus(status, page, size);
            return ResponseEntity.ok(ApiResponse.success(leads));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch leads by status: " + e.getMessage()));
        }
    }
    
    @GetMapping("/source/{source}")
    public ResponseEntity<ApiResponse<Page<LeadResponse>>> getLeadsBySource(
            @PathVariable LeadSource source,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<LeadResponse> leads = leadService.getLeadsBySource(source, page, size);
            return ResponseEntity.ok(ApiResponse.success(leads));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch leads by source: " + e.getMessage()));
        }
    }
    
    @GetMapping("/my-leads")
    public ResponseEntity<ApiResponse<Page<LeadResponse>>> getMyLeads(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<LeadResponse> leads = leadService.getMyLeads(page, size);
            return ResponseEntity.ok(ApiResponse.success(leads));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch my leads: " + e.getMessage()));
        }
    }
    
    @GetMapping("/stats/count")
    public ResponseEntity<ApiResponse<Long>> getTotalLeadCount() {
        try {
            long count = leadService.getTotalLeadCount();
            return ResponseEntity.ok(ApiResponse.success(count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get lead count: " + e.getMessage()));
        }
    }
    
    @GetMapping("/stats/count/{status}")
    public ResponseEntity<ApiResponse<Long>> getLeadCountByStatus(@PathVariable LeadStatus status) {
        try {
            long count = leadService.getLeadCountByStatus(status);
            return ResponseEntity.ok(ApiResponse.success(count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get lead count by status: " + e.getMessage()));
        }
    }
    
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<LeadResponse>>> getRecentLeads(
            @RequestParam(defaultValue = "7") int days) {
        try {
            List<LeadResponse> leads = leadService.getRecentLeads(days);
            return ResponseEntity.ok(ApiResponse.success(leads));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch recent leads: " + e.getMessage()));
        }
    }
    
    @PostMapping("/convert")
    public ResponseEntity<ApiResponse<ConvertLeadResponse>> convertLead(@Valid @RequestBody ConvertLeadRequest request) {
        try {
            ConvertLeadResponse response = leadConversionService.convertLead(request);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("already converted")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error(e.getMessage(), "ALREADY_CONVERTED"));
            } else if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(e.getMessage(), "NOT_FOUND"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error(e.getMessage()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to convert lead: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{leadId}/score-breakdown")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLeadScoreBreakdown(@PathVariable Long leadId) {
        try {
            LeadResponse lead = leadService.getLeadById(leadId);
            if (lead == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Lead not found"));
            }
            
            // Convert LeadResponse back to Lead entity for scoring
            Lead leadEntity = new Lead();
            leadEntity.setLeadId(lead.getLeadId());
            leadEntity.setFirstName(lead.getFirstName());
            leadEntity.setLastName(lead.getLastName());
            leadEntity.setEmail(lead.getEmail());
            leadEntity.setPhoneNumber(lead.getPhoneNumber());
            leadEntity.setCompany(lead.getCompany());
            leadEntity.setJobTitle(lead.getJobTitle());
            leadEntity.setLeadSource(lead.getLeadSource());
            leadEntity.setLeadStatus(lead.getLeadStatus());
            leadEntity.setLeadScore(lead.getLeadScore());
            leadEntity.setNotes(lead.getNotes());
            
            Map<String, Integer> scoreBreakdown = leadScoringService.getScoreBreakdown(leadEntity);
            int totalScore = scoreBreakdown.get("total");
            
            Map<String, Object> response = new HashMap<>();
            response.put("leadId", leadId);
            response.put("currentScore", lead.getLeadScore());
            response.put("calculatedScore", totalScore);
            response.put("breakdown", scoreBreakdown);
            response.put("grade", leadScoringService.getScoreGrade(totalScore));
            response.put("description", leadScoringService.getScoreDescription(totalScore));
            
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get lead score breakdown: " + e.getMessage()));
        }
    }
    
    @PostMapping("/{leadId}/recalculate-score")
    public ResponseEntity<ApiResponse<LeadResponse>> recalculateLeadScore(@PathVariable Long leadId) {
        try {
            LeadResponse updatedLead = leadService.recalculateLeadScore(leadId);
            return ResponseEntity.ok(ApiResponse.success(updatedLead));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to recalculate lead score: " + e.getMessage()));
        }
    }
}
