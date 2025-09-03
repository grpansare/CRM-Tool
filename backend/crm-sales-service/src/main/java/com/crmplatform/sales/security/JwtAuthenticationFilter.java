package com.crmplatform.sales.security;

import com.crmplatform.common.security.JwtTokenProvider;
import com.crmplatform.common.security.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        log.info("=== SALES SERVICE JWT FILTER DEBUG === Processing: {} {}", method, requestURI);
        
        try {
            // First check for user context headers from API Gateway
            String headerUserId = request.getHeader("X-User-Id");
            String headerTenantId = request.getHeader("X-Tenant-Id");
            String headerRole = request.getHeader("X-User-Role");
            String headerUsername = request.getHeader("X-Username");
            
            log.info("=== HEADER CHECK === X-User-Id: {}, X-Tenant-Id: {}, X-User-Role: {}, X-Username: {}", 
                     headerUserId, headerTenantId, headerRole, headerUsername);
            
            if (StringUtils.hasText(headerUserId) && StringUtils.hasText(headerRole)) {
                log.info("Found user context headers - User: {}, Role: {}, UserId: {}, TenantId: {}", 
                         headerUsername, headerRole, headerUserId, headerTenantId);
                
                // Set UserContext from headers
                UserContext.setCurrentUserId(Long.parseLong(headerUserId));
                if (StringUtils.hasText(headerTenantId)) {
                    UserContext.setCurrentTenantId(Long.parseLong(headerTenantId));
                }
                UserContext.setCurrentUsername(headerUsername);
                UserContext.setCurrentRole(headerRole);
                
                List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + headerRole)
                );
                
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(headerUsername, null, authorities);
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Successfully set authentication and UserContext from headers for user: {}", headerUsername);
            } else {
                // Fallback to JWT token processing
                String jwt = getJwtFromRequest(request);
                log.info("=== JWT FALLBACK === JWT token present: {}, length: {}", 
                         StringUtils.hasText(jwt), jwt != null ? jwt.length() : 0);
                
                if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                    log.info("JWT token validation successful");
                    String username = jwtTokenProvider.getUsernameFromToken(jwt);
                    String role = jwtTokenProvider.getRoleFromToken(jwt);
                    Long userId = jwtTokenProvider.getUserIdFromToken(jwt);
                    Long tenantId = jwtTokenProvider.getTenantIdFromToken(jwt);
                    
                    // Set UserContext from JWT
                    UserContext.setCurrentUserId(userId);
                    UserContext.setCurrentTenantId(tenantId);
                    UserContext.setCurrentUsername(username);
                    UserContext.setCurrentRole(role);
                    
                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + role)
                    );
                    
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("Successfully set authentication and UserContext from JWT for user: {}", username);
                } else {
                    log.warn("=== AUTHENTICATION FAILED === No valid JWT token or headers found for: {} {}", method, requestURI);
                }
            }
        } catch (Exception ex) {
            log.error("Error in JWT authentication filter for request: {} {}", method, requestURI, ex);
        }

        log.info("=== SALES SERVICE JWT FILTER === Proceeding to filter chain for: {} {}", method, requestURI);
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            // Clear UserContext after request processing to prevent memory leaks
            UserContext.clear();
            log.debug("UserContext cleared after request processing");
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
