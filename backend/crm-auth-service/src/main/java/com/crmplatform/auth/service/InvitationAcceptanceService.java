package com.crmplatform.auth.service;

import com.crmplatform.auth.dto.AcceptInvitationRequest;
import com.crmplatform.auth.dto.CreateUserRequest;
import com.crmplatform.auth.entity.Invitation;
import com.crmplatform.auth.entity.User;
import com.crmplatform.auth.repository.InvitationRepository;
import com.crmplatform.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvitationAcceptanceService {

    private final InvitationRepository invitationRepository;
    private final UserService userService;

    @Transactional
    public ApiResponse<Object> acceptInvitation(AcceptInvitationRequest request) {
        Invitation invitation = invitationRepository.findByToken(request.getToken()).orElse(null);
        if (invitation == null) {
            return ApiResponse.error("Invalid invitation token", "INVALID_TOKEN");
        }
        if (invitation.getStatus() != Invitation.InvitationStatus.PENDING) {
            return ApiResponse.error("Invitation is not valid", "INVITATION_INVALID");
        }
        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            invitation.setStatus(Invitation.InvitationStatus.EXPIRED);
            invitationRepository.save(invitation);
            return ApiResponse.error("Invitation expired", "INVITATION_EXPIRED");
        }

        // Temporarily set tenant context to invited tenant for user creation
        com.crmplatform.common.security.UserContext context = new com.crmplatform.common.security.UserContext();
        context.setTenantId(invitation.getTenantId());
        context.setUserId(invitation.getInvitedByUserId());
        context.setRole(User.UserRole.TENANT_ADMIN.name());
        com.crmplatform.common.security.UserContext.setCurrentUser(context);
        try {
            CreateUserRequest create = CreateUserRequest.builder()
                    .username(request.getUsername())
                    .email(invitation.getEmail())
                    .password(request.getPassword())
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .role(invitation.getRole().name())
                    .managerId(invitation.getManagerId())
                    .isActive(true)
                    .build();

            ApiResponse<User> created = userService.createUser(create);
            if (!created.isSuccess()) {
                return ApiResponse.error(created.getMessage());
            }

            invitation.setStatus(Invitation.InvitationStatus.ACCEPTED);
            invitationRepository.save(invitation);

            return ApiResponse.success(new Object() {
                public final Long userId = created.getData().getUserId();
                public final Long tenantId = created.getData().getTenantId();
                public final String email = created.getData().getEmail();
            }, "Invitation accepted");
        } finally {
            com.crmplatform.common.security.UserContext.clear();
        }
    }
}

