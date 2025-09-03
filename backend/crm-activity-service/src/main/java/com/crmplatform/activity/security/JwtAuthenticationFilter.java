package com.crmplatform.activity.security;

import com.crmplatform.common.security.JwtTokenProvider;
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
        log.info("=== ACTIVITY JWT FILTER DEBUG === Processing request: {} {}", method, requestURI);
        
        try {
            // First check for user context headers from API Gateway
            String headerUserId = request.getHeader("X-User-Id");
            String headerTenantId = request.getHeader("X-Tenant-Id");
            String headerRole = request.getHeader("X-User-Role");
            String headerUsername = request.getHeader("X-Username");
            
            if (StringUtils.hasText(headerUserId) && StringUtils.hasText(headerRole)) {
                log.info("=== ACTIVITY JWT FILTER DEBUG === Found user context headers - User: {}, Role: {}, UserId: {}, TenantId: {}", 
                         headerUsername, headerRole, headerUserId, headerTenantId);
                
                List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + headerRole)
                );
                
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(headerUsername, null, authorities);
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("=== ACTIVITY JWT FILTER DEBUG === Successfully set authentication from headers for user: {} with authorities: {}", 
                        headerUsername, authentication.getAuthorities());
            } else {
                // Fallback to JWT token processing
                String jwt = getJwtFromRequest(request);
                
                if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                    String username = jwtTokenProvider.getUsernameFromToken(jwt);
                    String role = jwtTokenProvider.getRoleFromToken(jwt);
                    
                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + role)
                    );
                    
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Successfully set authentication from JWT for user: {}", username);
                }
            }
        } catch (Exception ex) {
            log.error("Error in JWT authentication filter for request: {} {}", method, requestURI, ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
