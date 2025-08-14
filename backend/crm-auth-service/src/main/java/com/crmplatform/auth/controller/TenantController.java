package com.crmplatform.auth.controller;

import com.crmplatform.auth.dto.TenantRegistrationRequest;
import com.crmplatform.auth.dto.TenantRegistrationResponse;
import com.crmplatform.auth.dto.AcceptInvitationRequest;
import com.crmplatform.auth.entity.Tenant;
import com.crmplatform.auth.service.TenantService;
import com.crmplatform.auth.service.InvitationAcceptanceService;
import com.crmplatform.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
@Slf4j
public class TenantController {
    
    private final TenantService tenantService;
    private final InvitationAcceptanceService invitationAcceptanceService;
    
    /**
     * Public endpoint for tenant registration
     * This is the standard SaaS onboarding flow
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<TenantRegistrationResponse>> registerTenant(
             @Valid @RequestBody TenantRegistrationRequest request) {
        
        log.info("Tenant registration request for: {}", request.getTenantName());
        
        ApiResponse<TenantRegistrationResponse> response = tenantService.registerTenant(request);
        
        if (response.isSuccess()) {
        	System.out.println(response);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    /**
     * Public: Accept an invitation and create user account
     */
    @PostMapping("/accept-invitation")
    public ResponseEntity<ApiResponse<Object>> acceptInvitation(@Valid @RequestBody AcceptInvitationRequest request) {
        ApiResponse<Object> response = invitationAcceptanceService.acceptInvitation(request);
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get tenant information by subdomain
     */
  
    
    /**
     * Get tenant information by ID (requires authentication)
     */
    @GetMapping("/{tenantId}")
    public ResponseEntity<ApiResponse<Tenant>> getTenantById(@PathVariable Long tenantId) {
        log.info("Getting tenant by ID: {}", tenantId);
        
        Optional<Tenant> tenant = tenantService.getTenantById(tenantId);
        
        if (tenant.isPresent()) {
            ApiResponse<Tenant> response = ApiResponse.success(tenant.get());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Update tenant information (requires authentication and proper permissions)
     */
    @PutMapping("/{tenantId}")
    public ResponseEntity<ApiResponse<Tenant>> updateTenant(
            @PathVariable Long tenantId,
            @RequestBody Tenant tenantUpdate) {
        
        log.info("Updating tenant: {}", tenantId);
        
        ApiResponse<Tenant> response = tenantService.updateTenant(tenantId, tenantUpdate);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get available subscription plans
     */
    @GetMapping("/subscription-plans")
    public ResponseEntity<ApiResponse<Object>> getSubscriptionPlans() {
        log.info("Getting available subscription plans");
        
        var plans = new Object() {
            public final Tenant.SubscriptionPlan[] availablePlans = Tenant.SubscriptionPlan.values();
            public final Object[] planDetails = {
                new Object() {
                    public final String plan = "FREE";
                    public final String name = "Free Plan";
                    public final int maxUsers = 5;
                    public final String price = "$0/month";
                    public final String[] features = {"Basic CRM", "5 Users", "Email Support"};
                },
                new Object() {
                    public final String plan = "STARTER";
                    public final String name = "Starter Plan";
                    public final int maxUsers = 25;
                    public final String price = "$29/month";
                    public final String[] features = {"Full CRM", "25 Users", "Priority Support", "Basic Reports"};
                },
                new Object() {
                    public final String plan = "PROFESSIONAL";
                    public final String name = "Professional Plan";
                    public final int maxUsers = 100;
                    public final String price = "$99/month";
                    public final String[] features = {"Advanced CRM", "100 Users", "24/7 Support", "Advanced Reports", "API Access"};
                },
                new Object() {
                    public final String plan = "ENTERPRISE";
                    public final String name = "Enterprise Plan";
                    public final int maxUsers = 1000;
                    public final String price = "$299/month";
                    public final String[] features = {"Enterprise CRM", "1000 Users", "Dedicated Support", "Custom Reports", "Full API Access", "SSO"};
                }
            };
        };
        
        ApiResponse<Object> response = ApiResponse.success(plans);
        return ResponseEntity.ok(response);
    }
} 