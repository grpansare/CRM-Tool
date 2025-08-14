package com.crmplatform.auth.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignAuthConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // TODO: replace with actual token retrieval logic
            String token = "your-service-token-or-jwt";
            requestTemplate.header("Authorization", "Bearer " + token);
        };
    }
}
