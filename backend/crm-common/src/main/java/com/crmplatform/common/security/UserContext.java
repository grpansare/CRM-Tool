package com.crmplatform.common.security;

import lombok.Data;

@Data
public class UserContext {
    private Long userId;
    private Long tenantId;
    private String username;
    private String role;

    private static final ThreadLocal<UserContext> contextHolder = new ThreadLocal<>();

    public static void setCurrentUser(UserContext userContext) {
        contextHolder.set(userContext);
    }

    public static UserContext getCurrentUser() {
        return contextHolder.get();
    }

    public static void clear() {
        contextHolder.remove();
    }

    public static Long getCurrentUserId() {
        UserContext context = getCurrentUser();
        return context != null ? context.getUserId() : null;
    }

    public static Long getCurrentTenantId() {
        UserContext context = getCurrentUser();
        return context != null ? context.getTenantId() : null;
    }

    public static String getCurrentUsername() {
        UserContext context = getCurrentUser();
        return context != null ? context.getUsername() : null;
    }

    public static String getCurrentUserRole() {
        UserContext context = getCurrentUser();
        return context != null ? context.getRole() : null;
    }
    
    // Setter methods for individual fields
    public static void setCurrentUserId(Long userId) {
        UserContext context = getCurrentUser();
        if (context == null) {
            context = new UserContext();
            setCurrentUser(context);
        }
        context.setUserId(userId);
    }
    
    public static void setCurrentTenantId(Long tenantId) {
        UserContext context = getCurrentUser();
        if (context == null) {
            context = new UserContext();
            setCurrentUser(context);
        }
        context.setTenantId(tenantId);
    }
    
    public static void setCurrentUsername(String username) {
        UserContext context = getCurrentUser();
        if (context == null) {
            context = new UserContext();
            setCurrentUser(context);
        }
        context.setUsername(username);
    }
    
    public static void setCurrentRole(String role) {
        UserContext context = getCurrentUser();
        if (context == null) {
            context = new UserContext();
            setCurrentUser(context);
        }
        context.setRole(role);
    }
} 