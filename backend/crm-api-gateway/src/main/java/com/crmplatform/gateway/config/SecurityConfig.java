package com.crmplatform.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(request -> {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
                config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                config.setAllowedHeaders(Collections.singletonList("*"));
                config.setAllowCredentials(true);
                config.setMaxAge(3600L);
                config.setExposedHeaders(Collections.singletonList("Authorization"));
                return config;
            }))
            .authorizeExchange(authz -> authz
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                // Standardize on /api/v1 paths
                .pathMatchers(
                    "/api/v1/auth/**",
                    "/api/v1/tenants/register",
                    "/api/v1/tenants/login",
                    "/api/v1/tenants/check-subdomain/**"
                ).permitAll()
                .pathMatchers(
                    "/health/**",
                    "/actuator/health",
                    "/actuator/info",
                    "/fallback/**"
                ).permitAll()
                .anyExchange().authenticated()
            );
        
        return http.build();
    }
    
    // CORS is now configured in the security filter chain

   
} 