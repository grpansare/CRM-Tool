package com.crmplatform.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_permissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPermission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id")
    private Long permissionId;
    
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "resource", nullable = false, length = 100)
    private String resource; // e.g., "contacts", "deals", "accounts"
    
    @Column(name = "action", nullable = false, length = 50)
    private String action; // e.g., "read", "write", "delete", "create"
    
    @Column(name = "scope", nullable = false, length = 50)
    private String scope; // e.g., "own", "team", "all"
    
    public enum PermissionScope {
        OWN,    // User can only access their own data
        TEAM,   // User can access their team's data
        ALL     // User can access all data in tenant
    }
    
    public enum PermissionAction {
        READ, WRITE, DELETE, CREATE
    }
    
    public enum PermissionResource {
        CONTACTS, DEALS, ACCOUNTS, ACTIVITIES, REPORTS, SETTINGS
    }
} 