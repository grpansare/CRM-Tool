package com.crmplatform.auth.filter;

import com.crmplatform.common.security.UserContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@Order(1)
public class UserContextFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) 
            throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        
        try {
            String userId = request.getHeader("X-User-ID");
            String tenantId = request.getHeader("X-Tenant-ID");
            String username = request.getHeader("X-Username");
            String role = request.getHeader("X-User-Role");
            
            if (userId != null) {
                UserContext.setCurrentUserId(Long.parseLong(userId));
            }
            if (tenantId != null) {
                UserContext.setCurrentTenantId(Long.parseLong(tenantId));
            }
            if (username != null) {
                UserContext.setCurrentUsername(username);
            }
            if (role != null) {
                UserContext.setCurrentRole(role);
            }
            
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            UserContext.clear();
        }
    }
}
