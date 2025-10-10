package com.crmplatform.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {


    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth Service Routes
                .route("auth-service", r -> r
                        .path("/api/v1/auth/**")
                        .filters(f -> f
                                .rewritePath("/api/v1/auth/(?<segment>.*)", "/api/v1/auth/${segment}")
                                .circuitBreaker(config -> config
                                        .setName("auth-service-circuit-breaker")
                                        .setFallbackUri("forward:/fallback/auth"))
                        )
                        .uri("lb://crm-auth-service"))
                
                
                // Tenant Service Routes (part of auth service)
                .route("tenant-service", r -> r
                        .path("/api/v1/tenants/**")
                        .filters(f -> f
                                .rewritePath("/api/v1/tenants/(?<segment>.*)", "/api/v1/tenants/${segment}")
                                .circuitBreaker(config -> config
                                        .setName("auth-service-circuit-breaker")
                                        .setFallbackUri("forward:/fallback/auth"))
                        )
                        .uri("lb://crm-auth-service"))
                
                // Tenant Admin Routes (auth service)
                .route("tenant-admin", r -> r
                        .path("/api/v1/tenant-admin/**")
                        .filters(f -> f
                                .rewritePath("/api/v1/tenant-admin/(?<segment>.*)", "/api/v1/tenant-admin/${segment}")
                                .circuitBreaker(config -> config
                                        .setName("auth-service-circuit-breaker")
                                        .setFallbackUri("forward:/fallback/auth"))
                        )
                        .uri("lb://crm-auth-service"))
                
                // Contacts Service Routes
                .route("contacts-service", r -> r
                        .path("/api/v1/contacts/**", "/api/v1/accounts/**")
                        .filters(f -> f
                                .rewritePath("/api/v1/(?<segment>.*)", "/api/v1/${segment}")
                                .circuitBreaker(config -> config
                                        .setName("contacts-service-circuit-breaker")
                                        .setFallbackUri("forward:/fallback/contacts"))
                        )
                        .uri("lb://crm-contacts-service"))
                
                // Sales Service Routes
                .route("sales-service", r -> r
                        .path("/api/v1/deals/**", "/api/v1/pipelines/**", "/api/v1/sales-manager/**", "/api/v1/analytics/**", "/api/v1/leads/**", "/api/v1/reports/**", "/api/v1/tasks/**", "/api/v1/documents/**", "/api/v1/lead-assignment/**", "/api/v1/emails/**")
                        .filters(f -> f
                                .rewritePath("/api/v1/(?<segment>.*)", "/api/v1/${segment}")
                                .circuitBreaker(config -> config
                                        .setName("sales-service-circuit-breaker")
                                        .setFallbackUri("forward:/fallback/sales"))
                        )
                        .uri("lb://crm-sales-service"))
                
                // Activity Service Routes (future)
                .route("activity-service", r -> r
                        .path("/api/v1/activities/**", "/api/v1/timeline/**")
                        .filters(f -> f
                                .rewritePath("/api/v1/(?<segment>.*)", "/api/v1/${segment}")
                                .circuitBreaker(config -> config
                                        .setName("activity-service-circuit-breaker")
                                        .setFallbackUri("forward:/fallback/activity"))
                        )
                        .uri("lb://crm-activity-service"))
                
                // Email Service Routes
                .route("email-service", r -> r
                        .path("/api/v1/email/**")
                        .filters(f -> f
                                .rewritePath("/api/v1/email/(?<segment>.*)", "/api/email/${segment}")
                                .circuitBreaker(config -> config
                                        .setName("email-service-circuit-breaker")
                                        .setFallbackUri("forward:/fallback/email"))
                        )
                        .uri("lb://crm-email-service"))
                
                // User Management Routes
                .route("user-management", r -> r
                        .path("/api/v1/users/**")
                        .filters(f -> f
                                .rewritePath("/api/v1/users/(?<segment>.*)", "/api/v1/users/${segment}")
                                .circuitBreaker(config -> config
                                        .setName("user-management-circuit-breaker")
                                        .setFallbackUri("forward:/fallback/users"))
                        )
                        .uri("lb://crm-auth-service"))
                
                // Health Check Routes
                .route("health-checks", r -> r
                        .path("/health/**", "/actuator/**")
                        .uri("lb://crm-auth-service"))
                
                .build();
    }
} 