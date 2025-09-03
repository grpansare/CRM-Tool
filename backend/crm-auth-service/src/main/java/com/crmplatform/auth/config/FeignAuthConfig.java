package com.crmplatform.auth.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignAuthConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // No authentication needed for inter-service calls to public endpoints
            // The email service allows /api/email/invitation and /api/email/welcome as public endpoints
        };
    }
}
