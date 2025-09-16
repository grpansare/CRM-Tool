package com.crmplatform.contacts.controller;

import com.crmplatform.contacts.dto.ConvertLeadRequest;
import com.crmplatform.contacts.dto.CreateLeadRequest;
import com.crmplatform.contacts.dto.LeadResponse;
import com.crmplatform.contacts.service.LeadService;
import com.crmplatform.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leads")
@RequiredArgsConstructor
@Slf4j
public class LeadController {
    
    private final LeadService leadService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<Page<LeadResponse>>> getLeads(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Fetching leads - page: {}, size: {}, searchTerm: {}", page, size, searchTerm);
        
        ApiResponse<Page<LeadResponse>> response = leadService.getLeads(searchTerm, page, size);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/{leadId}")
    public ResponseEntity<ApiResponse<LeadResponse>> getLeadById(@PathVariable Long leadId) {
        log.info("Fetching lead by ID: {}", leadId);
        
        ApiResponse<LeadResponse> response = leadService.getLeadById(leadId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<LeadResponse>> createLead(@Valid @RequestBody CreateLeadRequest request) {
        log.info("Creating new lead for: {} {}", request.getFirstName(), request.getLastName());
        
        ApiResponse<LeadResponse> response = leadService.createLead(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PutMapping("/{leadId}")
    public ResponseEntity<ApiResponse<LeadResponse>> updateLead(
            @PathVariable Long leadId,
            @Valid @RequestBody CreateLeadRequest request) {
        
        log.info("Updating lead with ID: {}", leadId);
        
        ApiResponse<LeadResponse> response = leadService.updateLead(leadId, request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/convert")
    public ResponseEntity<ApiResponse<LeadResponse>> convertLead(@Valid @RequestBody ConvertLeadRequest request) {
        log.info("Converting lead with ID: {}", request.getLeadId());
        
        ApiResponse<LeadResponse> response = leadService.convertLead(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @DeleteMapping("/{leadId}")
    public ResponseEntity<ApiResponse<Void>> deleteLead(@PathVariable Long leadId) {
        log.info("Deleting lead with ID: {}", leadId);
        
        ApiResponse<Void> response = leadService.deleteLead(leadId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/my-leads")
    public ResponseEntity<ApiResponse<List<LeadResponse>>> getMyLeads() {
        log.info("Fetching current user's leads");
        
        ApiResponse<List<LeadResponse>> response = leadService.getMyLeads();
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
