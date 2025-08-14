package com.crmplatform.auth.service;

import com.crmplatform.auth.client.EmailServiceClient;
import com.crmplatform.auth.dto.CreateUserRequest;
import com.crmplatform.auth.dto.TenantRegistrationRequest;
import com.crmplatform.auth.dto.TenantRegistrationResponse;
import com.crmplatform.auth.entity.Tenant;
import com.crmplatform.auth.entity.User;
import com.crmplatform.auth.repository.TenantRepository;
import com.crmplatform.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantService {
    
    private final TenantRepository tenantRepository;
    private final UserService userService;
    private final EmailServiceClient emailServiceClient;
    
    /**
     * Standard tenant registration process
     * This follows industry best practices for SaaS tenant onboarding
     */
    @Transactional
    public ApiResponse<TenantRegistrationResponse> registerTenant(TenantRegistrationRequest request) {
        try {
            // 1. Validate terms acceptance
            if (request.getAcceptTerms() == null || !request.getAcceptTerms()) {
                return ApiResponse.error("Terms and conditions must be accepted", "TERMS_NOT_ACCEPTED");
            }
            
            // 2. Validate admin email uniqueness across all tenants
            if (userService.existsByEmailGlobally(request.getAdminEmail())) {
                return ApiResponse.error("Email is already registered", "EMAIL_EXISTS");
            }
            
            // 3. Validate admin username uniqueness across all tenants
            if (userService.existsByUsernameGlobally(request.getAdminUsername())) {
                return ApiResponse.error("Username is already taken", "USERNAME_EXISTS");
            }
            
            // 4. Create tenant
            Tenant tenant = Tenant.builder()
                    .tenantName(request.getTenantName())
                
                    .subscriptionPlan(Tenant.SubscriptionPlan.FREE)
                    .isActive(true)
                    .maxUsers(5) // Default free plan limit
                    .currentUsers(1) // The admin user being created
                    .trialEndsAt(LocalDateTime.now().plusDays(14)) // 14-day trial
                    .companyName(request.getCompanyName())
                    .companyAddress(request.getCompanyAddress())
                    .companyPhone(request.getCompanyPhone())
                    .companyEmail(request.getCompanyEmail())
                    .industry(request.getIndustry())
                    .timezone(request.getTimezone())
                    .locale(request.getLocale())
                    .createdBy(1L) // System user
                    .build();
            tenant = tenantRepository.save(tenant);
            
            // 5. Create tenant admin user
            User adminUser;
            try {
                adminUser = createTenantAdmin(tenant, request);
            } catch (RuntimeException e) {
                log.error("Failed to create tenant admin user: {}", e.getMessage());
                return ApiResponse.error("Failed to create admin user: " + e.getMessage(), "ADMIN_CREATION_ERROR");
            }
            
            // 6. Send welcome email (async)
            sendWelcomeEmail(tenant, adminUser);
            
            // 7. Create response
            TenantRegistrationResponse response = TenantRegistrationResponse.builder()
                    .tenantId(tenant.getTenantId())
                    .tenantName(tenant.getTenantName())
                  
                    .companyName(tenant.getCompanyName())
                    .subscriptionPlan(tenant.getSubscriptionPlan().name())
                    .trialEndsAt(tenant.getTrialEndsAt())
                    .createdAt(tenant.getCreatedAt())
                    .adminUserId(adminUser.getUserId())
                    .adminEmail(adminUser.getEmail())
                    .adminUsername(adminUser.getUsername())
                    .welcomeMessage("Welcome to " + tenant.getTenantName() + "!")
                    .nextSteps("Please check your email to verify your account and get started.")
                    .build();
            
            log.info("Tenant registered successfully: {} (ID: {})", tenant.getTenantName(), tenant.getTenantId());
            return ApiResponse.success(response, "Tenant registered successfully");
            
        } catch (Exception e) {
            log.error("Error registering tenant: {}", request.getTenantName(), e);
            return ApiResponse.error("Failed to register tenant", "REGISTRATION_ERROR");
        }
    }
    
    /**
     * Create tenant admin user with proper permissions
     */
    private User createTenantAdmin(Tenant tenant, TenantRegistrationRequest request) {
        CreateUserRequest adminRequest = CreateUserRequest.builder()
                .username(request.getAdminUsername())
                .email(request.getAdminEmail())
                .password(request.getAdminPassword())
                .firstName(request.getAdminFirstName())
                .lastName(request.getAdminLastName())
                .role("TENANT_ADMIN")
                .isActive(true)
                .build();
        
        // Use a special method that bypasses tenant context for initial admin creation
        return userService.createTenantAdmin(tenant.getTenantId(), adminRequest);
    }
    
    /**
     * Send welcome email asynchronously
     */
    private void sendWelcomeEmail(Tenant tenant, User adminUser) {
        try {
            log.info("Sending welcome email to: {} for tenant: {}", 
                    adminUser.getEmail(), tenant.getTenantName());
            
            // Convert tenantId to String as required by the email service
            String tenantId = String.valueOf(tenant.getTenantId());
            
            ResponseEntity<Map<String, Object>> response = emailServiceClient.sendWelcomeEmail(
                tenantId,
                adminUser.getEmail(),
                adminUser.getFirstName() + " " + adminUser.getLastName(),
                tenant.getTenantName()
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Welcome email successfully queued for: {} (tenant: {})", 
                        adminUser.getEmail(), tenant.getTenantName());
            } else {
                log.warn("Failed to queue welcome email for: {} (tenant: {})", 
                        adminUser.getEmail(), tenant.getTenantName());
            }
            
        } catch (Exception e) {
            log.error("Error sending welcome email to: {} for tenant: {}. Error: {}", 
                    adminUser.getEmail(), tenant.getTenantName(), e.getMessage(), e);
            // Don't throw exception to avoid breaking tenant registration
        }
    }
    
    /**
     * Generate a tenant domain
     */
  
    
    /**
     * Check if tenant exists
     */
  
    
    /**
     * Check if subdomain is in reserved list
     */
   
    
    /**
     * Get tenant by ID
     */
    public Optional<Tenant> getTenantById(Long tenantId) {
        return tenantRepository.findById(tenantId);
    }
    
    /**
     * Update tenant information
     */
    @Transactional
    public ApiResponse<Tenant> updateTenant(Long tenantId, Tenant tenantUpdate) {
        Optional<Tenant> existingTenant = tenantRepository.findById(tenantId);
        
        if (existingTenant.isEmpty()) {
            return ApiResponse.error("Tenant not found", "TENANT_NOT_FOUND");
        }
        
        Tenant tenant = existingTenant.get();
        
        // Update allowed fields
        if (tenantUpdate.getCompanyName() != null) {
            tenant.setCompanyName(tenantUpdate.getCompanyName());
        }
        if (tenantUpdate.getCompanyAddress() != null) {
            tenant.setCompanyAddress(tenantUpdate.getCompanyAddress());
        }
        if (tenantUpdate.getCompanyPhone() != null) {
            tenant.setCompanyPhone(tenantUpdate.getCompanyPhone());
        }
        if (tenantUpdate.getCompanyEmail() != null) {
            tenant.setCompanyEmail(tenantUpdate.getCompanyEmail());
        }
        if (tenantUpdate.getIndustry() != null) {
            tenant.setIndustry(tenantUpdate.getIndustry());
        }
        if (tenantUpdate.getTimezone() != null) {
            tenant.setTimezone(tenantUpdate.getTimezone());
        }
        if (tenantUpdate.getLocale() != null) {
            tenant.setLocale(tenantUpdate.getLocale());
        }
        
        tenant = tenantRepository.save(tenant);
        return ApiResponse.success(tenant, "Tenant updated successfully");
    }
} 