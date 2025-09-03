package com.crmplatform.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInvitationRequest {
    @NotBlank
    @Email
    private String email;

    private String firstName;
    private String lastName;

    // Expected values: SUPER_ADMIN, TENANT_ADMIN, SALES_MANAGER, SALES_REP, SUPPORT_AGENT, READ_ONLY
    @NotBlank
    private String role;
    
    // Required for SALES_REP role - specifies which manager will supervise this rep
    private Long managerId;
    
    // Optional fields for better organization
    private String department;
    private String territory;
}

