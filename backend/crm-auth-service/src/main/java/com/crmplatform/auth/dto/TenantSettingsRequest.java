package com.crmplatform.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantSettingsRequest {
    private String companyName;
    private String companyAddress;
    private String companyPhone;
    private String companyEmail;
    private String timezone;
    private String locale;
}

