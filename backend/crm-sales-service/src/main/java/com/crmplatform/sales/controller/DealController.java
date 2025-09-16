package com.crmplatform.sales.controller;

import com.crmplatform.common.dto.ApiResponse;
import com.crmplatform.sales.dto.AccountDealSummaryResponse;
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
        
        log.info("=== DEAL CREATION DEBUG ===");
        log.info("Creating deal: {}", request.getDealName());
        log.info("User Context - UserId: {}, TenantId: {}, Username: {}, Role: {}", 
                 com.crmplatform.common.security.UserContext.getCurrentUserId(),
                 com.crmplatform.common.security.UserContext.getCurrentTenantId(),
                 com.crmplatform.common.security.UserContext.getCurrentUsername(),
                 com.crmplatform.common.security.UserContext.getCurrentUserRole());
        log.info("Request payload: {}", request);
        
        try {
            ApiResponse<DealResponse> response = dealService.createDeal(request);
            log.info("Deal creation response: success={}, message={}", response.isSuccess(), response.getMessage());
            
            if (response.isSuccess()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Exception during deal creation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<DealResponse>builder()
                            .success(false)
                            .message("Internal server error: " + e.getMessage())
                            .build());
        }
    }
    
    @GetMapping("/stages")
    public ResponseEntity<ApiResponse<List<DealResponse.PipelineStageResponse>>> getAvailableStages() {
        log.info("=== DEAL CONTROLLER DEBUG === Getting available stages");
        log.info("=== USER CONTEXT === UserId: {}, TenantId: {}, Username: {}, Role: {}", 
                 com.crmplatform.common.security.UserContext.getCurrentUserId(),
                 com.crmplatform.common.security.UserContext.getCurrentTenantId(),
                 com.crmplatform.common.security.UserContext.getCurrentUsername(),
                 com.crmplatform.common.security.UserContext.getCurrentUserRole());
        
        ApiResponse<List<DealResponse.PipelineStageResponse>> response = dealService.getAvailableStages();
        log.info("=== DEAL CONTROLLER DEBUG === Stages response: success={}, data count={}", 
                 response.isSuccess(), response.getData() != null ? response.getData().size() : 0);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{dealId}/stage")
    public ResponseEntity<ApiResponse<DealResponse>> updateDealStage(
            @PathVariable Long dealId,
            @Valid @RequestBody UpdateDealStageRequest request) {
        
        try {
            log.info("=== DEAL STAGE UPDATE REQUEST === DealId: {}, NewStageId: {}", dealId, request.getNewStageId());
            log.info("Request object: {}", request);
            
            if (request == null) {
                log.error("Request body is null");
                return ResponseEntity.badRequest().body(ApiResponse.error("Request body is required", "INVALID_REQUEST"));
            }
            
            if (request.getNewStageId() == null) {
                log.error("NewStageId is null in request: {}", request);
                return ResponseEntity.badRequest().body(ApiResponse.error("New stage ID is required", "INVALID_STAGE_ID"));
            }
            
            ApiResponse<DealResponse> response = dealService.updateDealStage(dealId, request);
            
            if (response.isSuccess()) {
                log.info("Deal stage update successful");
                return ResponseEntity.ok(response);
            } else {
                log.error("Deal stage update failed: {}", response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Exception in updateDealStage: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Internal server error: " + e.getMessage(), "INTERNAL_ERROR"));
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
    
    @DeleteMapping("/{dealId}")
    public ResponseEntity<ApiResponse<Void>> deleteDeal(@PathVariable Long dealId) {
        log.info("Deleting deal: {}", dealId);
        
        ApiResponse<Void> response = dealService.deleteDeal(dealId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PutMapping("/{dealId}")
    public ResponseEntity<ApiResponse<DealResponse>> updateDeal(
            @PathVariable Long dealId,
            @Valid @RequestBody CreateDealRequest request) {
        
        log.info("Updating deal: {}", dealId);
        
        ApiResponse<DealResponse> response = dealService.updateDeal(dealId, request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/account/{accountId}/summary")
    public ResponseEntity<ApiResponse<AccountDealSummaryResponse>> getAccountDealSummary(@PathVariable Long accountId) {
        log.info("Getting deal summary for account: {}", accountId);
        
        ApiResponse<AccountDealSummaryResponse> response = dealService.getAccountDealSummary(accountId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/accounts/summaries")
    public ResponseEntity<ApiResponse<List<AccountDealSummaryResponse>>> getAccountDealSummaries(
            @RequestBody List<Long> accountIds) {
        log.info("Getting deal summaries for {} accounts", accountIds.size());
        
        ApiResponse<List<AccountDealSummaryResponse>> response = dealService.getAccountDealSummaries(accountIds);
        return ResponseEntity.ok(response);
    }
    
}