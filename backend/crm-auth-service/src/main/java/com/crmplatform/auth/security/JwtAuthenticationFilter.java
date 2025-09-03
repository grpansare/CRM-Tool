package com.crmplatform.auth.security;

import com.crmplatform.common.security.JwtTokenProvider;
import com.crmplatform.common.security.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
        log.info("=== JWT FILTER DEBUG === JwtAuthenticationFilter constructor called with JwtTokenProvider: {}", jwtTokenProvider);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        log.info("=== JWT FILTER DEBUG === Processing request: {} {}", method, requestURI);
        
        // Skip JWT processing only for login and tenant registration endpoints
        if (requestURI.equals("/api/v1/auth/login") || requestURI.startsWith("/api/v1/tenants/") || 
            requestURI.startsWith("/actuator/") || requestURI.startsWith("/health/")) {
            log.info("=== JWT FILTER DEBUG === Skipping JWT processing for public endpoint: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            // First check for user context headers from API Gateway
            String headerUserId = request.getHeader("X-User-Id");
            String headerTenantId = request.getHeader("X-Tenant-Id");
            String headerRole = request.getHeader("X-User-Role");
            String headerUsername = request.getHeader("X-Username");
            
            log.info("=== JWT FILTER DEBUG === Headers received - UserId: {}, TenantId: {}, Role: {}, Username: {}", 
                     headerUserId, headerTenantId, headerRole, headerUsername);
            
            if (StringUtils.hasText(headerUserId) && StringUtils.hasText(headerRole)) {
                log.info("Found user context headers - User: {}, Role: {}, UserId: {}, TenantId: {}", 
                         headerUsername, headerRole, headerUserId, headerTenantId);
                
                List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + headerRole)
                );
                
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(headerUsername, null, authorities);
                
                JwtAuthenticationDetails details = new JwtAuthenticationDetails();
                details.setUserId(Long.parseLong(headerUserId));
                details.setTenantId(headerTenantId != null ? Long.parseLong(headerTenantId) : null);
                details.setRole(headerRole);
                authentication.setDetails(details);
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                // Set UserContext for service layer access
                UserContext.setCurrentUserId(Long.parseLong(headerUserId));
                UserContext.setCurrentTenantId(headerTenantId != null ? Long.parseLong(headerTenantId) : null);
                UserContext.setCurrentUsername(headerUsername);
                UserContext.setCurrentRole(headerRole);
                
                log.info("Successfully set authentication from headers for user: {} with authorities: {}", 
                        headerUsername, authentication.getAuthorities());
            } else {
                // Fallback to JWT token processing
                String jwt = getJwtFromRequest(request);
                log.info("JWT Token extracted: {}", jwt != null ? "Present (length: " + jwt.length() + ")" : "Not found");
                
                if (StringUtils.hasText(jwt)) {
                    boolean isValid = jwtTokenProvider.validateToken(jwt);
                    log.info("JWT Token validation result: {}", isValid);
                    
                    if (isValid) {
                        String username = jwtTokenProvider.getUsernameFromToken(jwt);
                        String role = jwtTokenProvider.getRoleFromToken(jwt);
                        Long userId = jwtTokenProvider.getUserIdFromToken(jwt);
                        Long tenantId = jwtTokenProvider.getTenantIdFromToken(jwt);
                        
                        log.info("JWT Authentication - User: {}, Role: {}, UserId: {}, TenantId: {}", 
                                 username, role, userId, tenantId);
                        
                        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                            new SimpleGrantedAuthority("ROLE_" + role)
                        );
                        log.info("Created authorities: {}", authorities);
                        
                        UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                        
                        JwtAuthenticationDetails details = new JwtAuthenticationDetails();
                        details.setUserId(userId);
                        details.setTenantId(tenantId);
                        details.setRole(role);
                        authentication.setDetails(details);
                        
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        
                        // Set UserContext for service layer access
                        UserContext.setCurrentUserId(userId);
                        UserContext.setCurrentTenantId(tenantId);
                        UserContext.setCurrentUsername(username);
                        UserContext.setCurrentRole(role);
                        
                        log.info("Successfully set authentication in SecurityContext for user: {} with authorities: {}", 
                                username, authentication.getAuthorities());
                    } else {
                        log.warn("JWT token validation failed for request: {} {}", method, requestURI);
                    }
                } else {
                    log.info("No JWT token or user headers found for request: {} {}", method, requestURI);
                }
            }
            
            var contextAuth = SecurityContextHolder.getContext().getAuthentication();
            log.info("Verification - Authentication in context: {}, Authenticated: {}, Authorities: {}", 
                    contextAuth != null, contextAuth != null ? contextAuth.isAuthenticated() : false,
                    contextAuth != null ? contextAuth.getAuthorities() : "null");
        } catch (Exception ex) {
            log.error("Error in JWT authentication filter for request: {} {}", method, requestURI, ex);
        }

        log.info("=== JWT FILTER DEBUG === Proceeding with filter chain for: {} {}", method, requestURI);
        
        try {
            filterChain.doFilter(request, response);
            log.info("=== JWT FILTER DEBUG === Completed filter chain for: {} {}", method, requestURI);
        } finally {
            // Clear UserContext after request processing is completely done
            UserContext.clear();
            log.debug("UserContext cleared after request completion");
        }
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
