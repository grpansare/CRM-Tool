package com.crmplatform.auth.controller;

import com.crmplatform.auth.dto.TenantStatsResponse;
import com.crmplatform.auth.dto.UserInvitationRequest;
import com.crmplatform.auth.dto.TenantSettingsRequest;
import com.crmplatform.auth.entity.User;
import com.crmplatform.auth.service.TenantAdminService;
import com.crmplatform.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tenant-admin")
@RequiredArgsConstructor
@Slf4j
public class TenantAdminController {
    
    private final TenantAdminService tenantAdminService;
    
    /**
     * Get organization-wide dashboard statistics
     */
    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN')")

    public ResponseEntity<ApiResponse<TenantStatsResponse>> getDashboardStats() {
        log.info("Getting dashboard statistics for tenant admin");
        
     
        
        ApiResponse<TenantStatsResponse> response = tenantAdminService.getDashboardStats();
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get recent activity for the organization
     */
    @GetMapping("/dashboard/recent-activity")
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN')")

    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getRecentActivity(
    		@RequestParam(name = "limit", defaultValue = "10") int limit) {
        log.info("Getting recent activity for tenant admin, limit: {}", limit);
        
        
        ApiResponse<List<Map<String, Object>>> response = tenantAdminService.getRecentActivity(limit);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all users in the organization
     */
    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN')")

    public ResponseEntity<ApiResponse<List<User>>> getAllUsers(
    		@RequestParam(name = "page", defaultValue = "0") int page,
    		@RequestParam(name = "size", defaultValue = "20") int size,
    		@RequestParam(name = "search", required = false) String search) {
        log.info("Getting all users for tenant admin - page: {}, size: {}, search: {}", page, size, search);
        
        ApiResponse<List<User>> response = tenantAdminService.getAllUsers(page, size, search);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Invite a new user to the organization
     */
    @PostMapping("/users/invite")
    public ResponseEntity<ApiResponse<String>> inviteUser(@Valid @RequestBody UserInvitationRequest request) {
        log.info("Inviting user: {} to organization", request.getEmail());
        
        ApiResponse<String> response = tenantAdminService.inviteUser(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Update user role or status
     */
    @PutMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<User>> updateUser(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> updates) {
        log.info("Updating user: {} with updates: {}", userId, updates);
        
        ApiResponse<User> response = tenantAdminService.updateUser(userId, updates);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Deactivate/reactivate a user
     */
    @PutMapping("/users/{userId}/status")
    public ResponseEntity<ApiResponse<User>> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam(name = "active") boolean active) {
        log.info("Updating user status: {} to active: {}", userId, active);
        
        ApiResponse<User> response = tenantAdminService.updateUserStatus(userId, active);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Delete a user from the organization
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long userId) {
        log.info("Deleting user: {}", userId);
        
        ApiResponse<String> response = tenantAdminService.deleteUser(userId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Update organization settings
     */
    @PutMapping("/settings")
    public ResponseEntity<ApiResponse<String>> updateOrganizationSettings(
            @Valid @RequestBody TenantSettingsRequest request) {
        log.info("Updating organization settings");
        
        ApiResponse<String> response = tenantAdminService.updateOrganizationSettings(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get pending invitations
     */
    @GetMapping("/invitations/pending")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPendingInvitations() {
        log.info("Getting pending invitations for tenant admin");
        
        ApiResponse<List<Map<String, Object>>> response = tenantAdminService.getPendingInvitations();
        return ResponseEntity.ok(response);
    }
    
    /**
     * Cancel a pending invitation
     */
    @DeleteMapping("/invitations/{invitationId}")
    public ResponseEntity<ApiResponse<String>> cancelInvitation(@PathVariable Long invitationId) {
        log.info("Canceling invitation: {}", invitationId);
        
        ApiResponse<String> response = tenantAdminService.cancelInvitation(invitationId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Resend an invitation
     */
    @PostMapping("/invitations/{invitationId}/resend")
    public ResponseEntity<ApiResponse<String>> resendInvitation(@PathVariable Long invitationId) {
        log.info("Resending invitation: {}", invitationId);
        
        ApiResponse<String> response = tenantAdminService.resendInvitation(invitationId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get available managers for assignment
     */
    @GetMapping("/managers")
    public ResponseEntity<ApiResponse<List<User>>> getAvailableManagers() {
        log.info("Getting available managers for tenant admin");
        
        ApiResponse<List<User>> response = tenantAdminService.getAvailableManagers();
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update subscription plan
     */
    @PutMapping("/subscription")
    public ResponseEntity<ApiResponse<String>> updateSubscription(
    		@RequestParam(name = "plan") String plan) {
        log.info("Updating subscription plan to: {}", plan);
        
        ApiResponse<String> response = tenantAdminService.updateSubscriptionPlan(plan);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
