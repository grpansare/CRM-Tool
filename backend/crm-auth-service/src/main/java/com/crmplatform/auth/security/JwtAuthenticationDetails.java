package com.crmplatform.auth.security;

import lombok.Data;

@Data
public class JwtAuthenticationDetails {
    private Long userId;
    private Long tenantId;
    private String role;
}
