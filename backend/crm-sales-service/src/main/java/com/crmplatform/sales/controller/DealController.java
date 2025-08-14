package com.crmplatform.sales.controller;

import com.crmplatform.common.dto.ApiResponse;
import com.crmplatform.sales.dto.CreateDealRequest;
import com.crmplatform.sales.dto.DealResponse;
import com.crmplatform.sales.dto.UpdateDealStageRequest;
import com.crmplatform.sales.service.DealService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/deals")
@RequiredArgsConstructor
@Slf4j
public class DealController {
    
    private final DealService dealService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<DealResponse>> createDeal(
            @Valid @RequestBody CreateDealRequest request) {
        
        log.info("Creating deal: {}", request.getDealName());
        
        ApiResponse<DealResponse> response = dealService.createDeal(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PutMapping("/{dealId}/stage")
    public ResponseEntity<ApiResponse<DealResponse>> updateDealStage(
            @PathVariable Long dealId,
            @Valid @RequestBody UpdateDealStageRequest request) {
        
        log.info("Updating deal {} stage to {}", dealId, request.getNewStageId());
        
        ApiResponse<DealResponse> response = dealService.updateDealStage(dealId, request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/{dealId}")
    public ResponseEntity<ApiResponse<DealResponse>> getDeal(@PathVariable Long dealId) {
        log.info("Getting deal: {}", dealId);
        
        ApiResponse<DealResponse> response = dealService.getDeal(dealId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<DealResponse>>> getMyDeals() {
        log.info("Getting deals for current user");
        
        ApiResponse<List<DealResponse>> response = dealService.getDealsByOwner();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/contact/{contactId}")
    public ResponseEntity<ApiResponse<List<DealResponse>>> getDealsByContact(@PathVariable Long contactId) {
        log.info("Getting deals for contact: {}", contactId);
        
        ApiResponse<List<DealResponse>> response = dealService.getDealsByContact(contactId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/account/{accountId}")
    public ResponseEntity<ApiResponse<List<DealResponse>>> getDealsByAccount(@PathVariable Long accountId) {
        log.info("Getting deals for account: {}", accountId);
        
        ApiResponse<List<DealResponse>> response = dealService.getDealsByAccount(accountId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
} 