package com.crmplatform.gateway.filter;

import com.crmplatform.common.security.JwtTokenProvider;
import com.crmplatform.common.security.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
public class AuthenticationFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    // Public endpoints that don't require authentication
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/validate",
            "/api/v1/tenants/register",
            "/api/v1/tenants/check-subdomain",
            "/health",
            "/actuator/health",
            "/actuator/info"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // Skip authentication for public endpoints
        if (isPublicEndpoint(path)) {
            log.debug("Skipping authentication for public endpoint: {}", path);
            return chain.filter(exchange);
        }

        // Extract JWT token from Authorization header
        String token = extractToken(request);
        if (!StringUtils.hasText(token)) {
            log.warn("Missing JWT token for request: {}", path);
            return unauthorized(exchange, "Missing authentication token");
        }

        try {
            // Validate JWT token
            if (!jwtTokenProvider.validateToken(token)) {
                log.warn("Invalid JWT token for request: {}", path);
                return unauthorized(exchange, "Invalid authentication token");
            }

            // Extract user information from token
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            Long tenantId = jwtTokenProvider.getTenantIdFromToken(token);
            String username = jwtTokenProvider.getUsernameFromToken(token);
            String role = jwtTokenProvider.getRoleFromToken(token);

            // Set user context for downstream services
            UserContext.setCurrentUserId(userId);
            UserContext.setCurrentTenantId(tenantId);
            UserContext.setCurrentUsername(username);
            UserContext.setCurrentRole(role);

            // Add user information to request headers for downstream services
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-ID", String.valueOf(userId))
                    .header("X-Tenant-ID", String.valueOf(tenantId))
                    .header("X-Username", username)
                    .header("X-User-Role", role)
                    .build();

            log.debug("Authenticated user: {} (ID: {}, Tenant: {}, Role: {}) for path: {}", 
                    username, userId, tenantId, role, path);

            return chain.filter(exchange.mutate().request(modifiedRequest).build())
                    .doFinally(signalType -> {
                        // Clean up ThreadLocal context
                        UserContext.clear();
                    });

        } catch (Exception e) {
            log.error("Error processing JWT token for request: {}", path, e);
            return unauthorized(exchange, "Authentication failed");
        }
    }

    @Override
    public int getOrder() {
        return -100; // High priority filter
    }

    private String extractToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private boolean isPublicEndpoint(String path) {
        log.debug("Checking if path '{}' is public endpoint", path);
        boolean isPublic = PUBLIC_ENDPOINTS.stream().anyMatch(endpoint -> {
            boolean matches = path.startsWith(endpoint);
            log.debug("Path '{}' starts with '{}': {}", path, endpoint, matches);
            return matches;
        });
        log.debug("Path '{}' is public: {}", path, isPublic);
        return isPublic;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
} 