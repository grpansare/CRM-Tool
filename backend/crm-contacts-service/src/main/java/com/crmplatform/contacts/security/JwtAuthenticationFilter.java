package com.crmplatform.contacts.security;

import com.crmplatform.common.security.JwtTokenProvider;
import com.crmplatform.common.security.UserContext;
import com.crmplatform.contacts.security.JwtAuthenticationDetails;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        try {
            // First check for user context headers from API Gateway
            String headerUserId = request.getHeader("X-User-Id");
            String headerTenantId = request.getHeader("X-Tenant-Id");
            String headerRole = request.getHeader("X-User-Role");
            String headerUsername = request.getHeader("X-Username");
            
            if (StringUtils.hasText(headerUserId) && StringUtils.hasText(headerRole)) {
                log.info("Found user context headers - User: {}, Role: {}, UserId: {}, TenantId: {}", 
                        headerUsername, headerRole, headerUserId, headerTenantId);
                
                List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + headerRole)
                );
                
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(headerUsername, null, authorities);
                
                // Add user details to authentication
                JwtAuthenticationDetails details = new JwtAuthenticationDetails();
                details.setUserId(Long.parseLong(headerUserId));
                details.setTenantId(headerTenantId != null ? Long.parseLong(headerTenantId) : null);
                details.setRole(headerRole);
                authentication.setDetails(details);
                
                // Set UserContext for service layer
                UserContext.setCurrentUserId(Long.parseLong(headerUserId));
                UserContext.setCurrentTenantId(headerTenantId != null ? Long.parseLong(headerTenantId) : null);
                UserContext.setCurrentUsername(headerUsername);
                UserContext.setCurrentRole(headerRole);
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("Successfully set authentication from headers for user: {} with authorities: {}", 
                        headerUsername, authentication.getAuthorities());
            } else {
                // Fallback to JWT token processing
                String jwt = getJwtFromRequest(request);
                log.debug("JWT Token extracted: {}", jwt != null ? "Present" : "Not found");
                
                if (StringUtils.hasText(jwt)) {
                    boolean isValid = jwtTokenProvider.validateToken(jwt);
                    log.debug("JWT Token validation result: {}", isValid);
                    
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
                        
                        UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                        
                        // Add user details to authentication
                        JwtAuthenticationDetails details = new JwtAuthenticationDetails();
                        details.setUserId(userId);
                        details.setTenantId(tenantId);
                        details.setRole(role);
                        authentication.setDetails(details);
                        
                        // Set UserContext for service layer
                        UserContext.setCurrentUserId(userId);
                        UserContext.setCurrentTenantId(tenantId);
                        UserContext.setCurrentUsername(username);
                        UserContext.setCurrentRole(role);
                        
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.info("Successfully set authentication from JWT for user: {} with authorities: {}", 
                                username, authentication.getAuthorities());
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Error in JWT authentication filter for request: {} {}", method, requestURI, ex);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Invalid or expired token\"}");
            response.setContentType("application/json");
            return;
        }

        filterChain.doFilter(request, response);
        
        // Clean up UserContext after request
        UserContext.clear();
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
