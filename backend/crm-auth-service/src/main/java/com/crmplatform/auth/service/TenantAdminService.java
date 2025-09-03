package com.crmplatform.auth.service;

import com.crmplatform.auth.entity.Tenant;
import com.crmplatform.auth.entity.User;
import com.crmplatform.auth.dto.TenantStatsResponse;
import com.crmplatform.auth.repository.TenantRepository;
import com.crmplatform.auth.repository.InvitationRepository;
import com.crmplatform.auth.entity.Invitation;
import com.crmplatform.auth.client.EmailServiceClient;
import com.crmplatform.auth.dto.UserInvitationRequest;
import com.crmplatform.auth.repository.UserRepository;
import com.crmplatform.common.dto.ApiResponse;
import com.crmplatform.common.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantAdminService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final InvitationRepository invitationRepository;
    private final EmailServiceClient emailServiceClient;
    private final TenantIntegrationService tenantIntegrationService;

    public ApiResponse<TenantStatsResponse> getDashboardStats() {
        Long tenantId = UserContext.getCurrentTenantId();
        
        if (tenantId == null) {
            log.error("Tenant ID is not available in the current context");
            throw new IllegalStateException("Tenant information is not available. Please log in again.");
        }

        int totalUsers = userRepository.findByTenantId(tenantId).size();
        int activeUsers = (int) userRepository.findByTenantId(tenantId)
                .stream().filter(u -> Boolean.TRUE.equals(u.getIsActive())).count();

        // Fetch cross-service counts (contacts, accounts, deals, revenue, monthly stats)
        TenantIntegrationService.TenantCounts counts = tenantIntegrationService.fetchCounts(tenantId);

        Tenant tenant = tenantRepository.findById(tenantId).orElse(null);

        TenantStatsResponse response = TenantStatsResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .pendingInvitations(0) // TODO: wire invitations table
                .totalContacts(counts.totalContacts())
                .totalAccounts(counts.totalAccounts())
                .totalDeals(counts.totalDeals())
                .totalRevenue(counts.totalRevenue())
                .monthlyRevenue(counts.monthlyRevenue())
                .dealsThisMonth(counts.dealsThisMonth())
                .contactsThisMonth(counts.contactsThisMonth())
                .subscriptionPlan(tenant != null && tenant.getSubscriptionPlan()!=null ? tenant.getSubscriptionPlan().name() : null)
                .maxUsers(tenant != null && tenant.getMaxUsers()!=null ? tenant.getMaxUsers() : 0)
                .isTrialActive(tenant != null && tenant.getTrialEndsAt() != null && tenant.getTrialEndsAt().isAfter(LocalDateTime.now()))
                .trialEndsAt(tenant != null && tenant.getTrialEndsAt()!=null ? tenant.getTrialEndsAt().toString() : null)
                .subscriptionEndsAt(tenant != null && tenant.getSubscriptionEndsAt()!=null ? tenant.getSubscriptionEndsAt().toString() : null)
                .build();

        return ApiResponse.success(response);
    }

    public ApiResponse<List<Map<String, Object>>> getRecentActivity(int limit) {
        // Placeholder: In a real app this would call an activity/timeline service
        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> i1 = new HashMap<>();
        i1.put("type", "USER_JOINED");
        i1.put("message", "A new user joined the organization");
        i1.put("timestamp", LocalDateTime.now().minusHours(2).toString());
        items.add(i1);

        Map<String, Object> i2 = new HashMap<>();
        i2.put("type", "DEAL_WON");
        i2.put("message", "A deal was moved to Closed Won");
        i2.put("timestamp", LocalDateTime.now().minusHours(4).toString());
        items.add(i2);

        if (items.size() > limit) {
            items = items.subList(0, limit);
        }

        return ApiResponse.success(items);
    }

    public ApiResponse<List<User>> getAllUsers(int page, int size, String search) {
        Long tenantId = UserContext.getCurrentTenantId();
        // Simple implementation; paging/search can be enhanced
        List<User> users = userRepository.findByTenantIdAndActive(tenantId);
        return ApiResponse.success(users);
    }

    public ApiResponse<String> inviteUser(UserInvitationRequest request) {
        Long tenantId = UserContext.getCurrentTenantId();
        Long inviterId = UserContext.getCurrentUserId();

        // Validate manager assignment for SALES_REP role
        if ("SALES_REP".equals(request.getRole())) {
            if (request.getManagerId() == null) {
                return ApiResponse.error("Manager is required for SALES_REP role", "MANAGER_REQUIRED");
            }
            
            // Validate manager exists and is a SALES_MANAGER in same tenant
            Optional<User> managerOpt = userRepository.findById(request.getManagerId());
            if (managerOpt.isEmpty() || 
                !managerOpt.get().getTenantId().equals(tenantId) ||
                !managerOpt.get().getRole().equals(User.UserRole.SALES_MANAGER) ||
                !Boolean.TRUE.equals(managerOpt.get().getIsActive())) {
                return ApiResponse.error("Invalid manager selected", "INVALID_MANAGER");
            }
        }

        // Check for existing email
        if (userRepository.existsByEmailAndTenantId(request.getEmail(), tenantId)) {
            return ApiResponse.error("User with this email already exists", "EMAIL_EXISTS");
        }

        Invitation invitation = Invitation.builder()
                .tenantId(tenantId)
                .email(request.getEmail())
                .role(User.UserRole.valueOf(request.getRole()))
                .invitedByUserId(inviterId)
                .managerId(request.getManagerId())
                .token(java.util.UUID.randomUUID().toString().replaceAll("-", ""))
                .status(Invitation.InvitationStatus.PENDING)
                .expiresAt(java.time.LocalDateTime.now().plusDays(7))
                .build();

        invitation = invitationRepository.save(invitation);

        String inviteUrl = String.format("http://localhost:3000/accept-invitation?token=%s", invitation.getToken());
        try {
            emailServiceClient.sendInvitationEmail(String.valueOf(tenantId), request.getEmail(), inviteUrl, request.getRole());
        } catch (Exception e) {
            log.warn("Failed to send invitation email to {}: {}", request.getEmail(), e.getMessage());
        }

        return ApiResponse.success("Invitation sent");
    }

    public ApiResponse<User> updateUser(Long userId, Map<String, Object> updates) {
        return ApiResponse.error("Not implemented");
    }

    public ApiResponse<User> updateUserStatus(Long userId, boolean active) {
        return ApiResponse.error("Not implemented");
    }

    public ApiResponse<String> deleteUser(Long userId) {
        return ApiResponse.error("Not implemented");
    }

    public ApiResponse<String> updateOrganizationSettings(Object request) {
        return ApiResponse.error("Not implemented");
    }

    public ApiResponse<List<Map<String, Object>>> getPendingInvitations() {
        Long tenantId = UserContext.getCurrentTenantId();
        List<Invitation> invitations = invitationRepository.findPendingByTenant(tenantId);
        List<Map<String, Object>> data = new java.util.ArrayList<>();
        for (Invitation i : invitations) {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("invitationId", i.getInvitationId());
            m.put("email", i.getEmail());
            m.put("role", i.getRole().name());
            m.put("expiresAt", i.getExpiresAt().toString());
            m.put("status", i.getStatus().name());
            data.add(m);
        }
        return ApiResponse.success(data);
    }

    public ApiResponse<String> cancelInvitation(Long invitationId) {
        Long tenantId = UserContext.getCurrentTenantId();
        Invitation inv = invitationRepository.findByTenantIdAndInvitationId(tenantId, invitationId)
                .orElse(null);
        if (inv == null) return ApiResponse.error("Invitation not found");
        inv.setStatus(Invitation.InvitationStatus.CANCELLED);
        invitationRepository.save(inv);
        return ApiResponse.success("Cancelled");
    }

    public ApiResponse<String> resendInvitation(Long invitationId) {
        Long tenantId = UserContext.getCurrentTenantId();
        Invitation inv = invitationRepository.findByTenantIdAndInvitationId(tenantId, invitationId)
                .orElse(null);
        if (inv == null) return ApiResponse.error("Invitation not found");
        String inviteUrl = String.format("http://localhost:3000/accept-invitation?token=%s", inv.getToken());
        try {
            emailServiceClient.sendInvitationEmail(String.valueOf(tenantId), inv.getEmail(), inviteUrl, inv.getRole().name());
        } catch (Exception e) {
            log.warn("Failed to resend invitation email to {}: {}", inv.getEmail(), e.getMessage());
        }
        return ApiResponse.success("Resent");
    }

    public ApiResponse<String> updateSubscriptionPlan(String plan) {
        return ApiResponse.success("Updated (stub)");
    }

    public ApiResponse<List<User>> getAvailableManagers() {
        Long tenantId = UserContext.getCurrentTenantId();
        List<User> managers = userRepository.findByTenantIdAndRoleAndActive(tenantId, User.UserRole.SALES_MANAGER);
        return ApiResponse.success(managers);
    }
}

