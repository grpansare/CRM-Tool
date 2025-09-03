package com.crmplatform.sales.config;

import com.crmplatform.common.security.UserContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;

@Configuration
public class RestTemplateConfig {
    
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList(new UserContextInterceptor()));
        return restTemplate;
    }
    
    private static class UserContextInterceptor implements ClientHttpRequestInterceptor {
        @Override
        public ClientHttpResponse intercept(
                HttpRequest request, 
                byte[] body, 
                ClientHttpRequestExecution execution) throws IOException {
            
            // Add user context headers for service-to-service communication
            Long tenantId = UserContext.getCurrentTenantId();
            Long userId = UserContext.getCurrentUserId();
            String username = UserContext.getCurrentUsername();
            String role = UserContext.getCurrentUserRole();
            
            if (tenantId != null) {
                request.getHeaders().add("X-Tenant-Id", tenantId.toString());
            }
            if (userId != null) {
                request.getHeaders().add("X-User-Id", userId.toString());
            }
            if (username != null) {
                request.getHeaders().add("X-Username", username);
            }
            if (role != null) {
                request.getHeaders().add("X-User-Role", role);
            }
            
            return execution.execute(request, body);
        }
    }
}