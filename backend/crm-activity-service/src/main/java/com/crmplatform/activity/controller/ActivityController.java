package com.crmplatform.activity.controller;

import com.crmplatform.activity.dto.ActivityResponse;
import com.crmplatform.activity.dto.CreateActivityRequest;
import com.crmplatform.activity.service.ActivityService;
import com.crmplatform.common.dto.ApiResponse;
import com.crmplatform.common.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ActivityController {
    
    private final ActivityService activityService;
    
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ActivityResponse>> createActivity(
            @Valid @RequestBody CreateActivityRequest request) {
        
        Long tenantId = UserContext.getCurrentTenantId();
        Long userId = UserContext.getCurrentUserId();
        
        log.info("Creating activity for tenant: {}, user: {}", tenantId, userId);
        
        ActivityResponse response = activityService.createActivity(request, tenantId, userId);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Activity created successfully"));
    }
    
    @GetMapping("/contacts/{contactId}/timeline")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<ActivityResponse>>> getContactTimeline(
            @PathVariable Long contactId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        
        Long tenantId = UserContext.getCurrentTenantId();
        
        log.info("Retrieving timeline for contact: {}, tenant: {}", contactId, tenantId);
        
        Page<ActivityResponse> timeline = activityService.getContactTimeline(contactId, tenantId, page, size);
        
        return ResponseEntity.ok(ApiResponse.success(timeline, "Contact timeline retrieved successfully"));
    }
    
    @GetMapping("/accounts/{accountId}/timeline")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<ActivityResponse>>> getAccountTimeline(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        
        Long tenantId = UserContext.getCurrentTenantId();
        
        log.info("Retrieving timeline for account: {}, tenant: {}", accountId, tenantId);
        
        Page<ActivityResponse> timeline = activityService.getAccountTimeline(accountId, tenantId, page, size);
        
        return ResponseEntity.ok(ApiResponse.success(timeline, "Account timeline retrieved successfully"));
    }
    
    @GetMapping("/deals/{dealId}/timeline")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<ActivityResponse>>> getDealTimeline(
            @PathVariable Long dealId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        
        Long tenantId = UserContext.getCurrentTenantId();
        
        log.info("Retrieving timeline for deal: {}, tenant: {}", dealId, tenantId);
        
        Page<ActivityResponse> timeline = activityService.getDealTimeline(dealId, tenantId, page, size);
        
        return ResponseEntity.ok(ApiResponse.success(timeline, "Deal timeline retrieved successfully"));
    }
    
    @GetMapping("/my-activities")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<ActivityResponse>>> getMyActivities(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        
        Long tenantId = UserContext.getCurrentTenantId();
        Long userId = UserContext.getCurrentUserId();
        
        log.info("Retrieving activities for user: {}, tenant: {}", userId, tenantId);
        
        Page<ActivityResponse> activities = activityService.getUserActivities(userId, tenantId, page, size);
        
        return ResponseEntity.ok(ApiResponse.success(activities, "User activities retrieved successfully"));
    }
}
