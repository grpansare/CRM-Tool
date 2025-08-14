package com.crmplatform.auth.service;

import com.crmplatform.auth.dto.CreateUserRequest;
import com.crmplatform.auth.entity.User;
import com.crmplatform.auth.entity.UserPermission;
import com.crmplatform.auth.repository.UserPermissionRepository;
import com.crmplatform.auth.repository.UserRepository;
import com.crmplatform.common.dto.ApiResponse;
import com.crmplatform.common.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional
    public ApiResponse<User> createUser(CreateUserRequest request) {
        Long tenantId = UserContext.getCurrentTenantId();
        Long createdBy = UserContext.getCurrentUserId();
        
        try {
            // Check if user already exists
            if (userRepository.existsByEmailAndTenantId(request.getEmail(), tenantId)) {
                return ApiResponse.error("User with this email already exists", "USER_EXISTS");
            }
            
            if (userRepository.existsByUsernameAndTenantId(request.getUsername(), tenantId)) {
                return ApiResponse.error("Username already taken", "USERNAME_EXISTS");
            }
            
            // Validate role
            User.UserRole role;
            try {
                role = User.UserRole.valueOf(request.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                return ApiResponse.error("Invalid role", "INVALID_ROLE");
            }
            
            // Validate manager exists if specified
            if (request.getManagerId() != null) {
                Optional<User> managerOpt = userRepository.findById(request.getManagerId());
                if (managerOpt.isEmpty() || !managerOpt.get().getTenantId().equals(tenantId)) {
                    return ApiResponse.error("Invalid manager ID", "INVALID_MANAGER");
                }
            }
            
            // Create user
            User user = User.builder()
                    .tenantId(tenantId)
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .passwordHash(passwordEncoder.encode(request.getPassword()))
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .role(role)
                    .managerId(request.getManagerId())
                    .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                    .createdBy(createdBy)
                    .build();
            
            user = userRepository.save(user);
            
            // Create default permissions based on role
            createDefaultPermissions(user);
            
            log.info("User created successfully: {}", user.getUserId());
            return ApiResponse.success(user, "User created successfully");
            
        } catch (Exception e) {
            log.error("Error creating user", e);
            return ApiResponse.error("Failed to create user", "CREATE_ERROR");
        }
    }
    
    /**
     * Create tenant admin user (bypasses tenant context for initial setup)
     */
    @Transactional
    public User createTenantAdmin(Long tenantId, CreateUserRequest request) {
        try {
            // Check if user already exists globally
            if (existsByEmailGlobally(request.getEmail())) {
                throw new RuntimeException("Email already registered");
            }
            
            if (existsByUsernameGlobally(request.getUsername())) {
                throw new RuntimeException("Username already taken");
            }
            
            // Validate role
            User.UserRole role;
            try {
                role = User.UserRole.valueOf(request.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid role");
            }
            
            // Create user
            User user = User.builder()
                    .tenantId(tenantId)
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .passwordHash(passwordEncoder.encode(request.getPassword()))
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .role(role)
                    .managerId(null) // Admin has no manager
                    .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                    .createdBy(null) // Self-registration
                    .build();
            
            user = userRepository.save(user);
            
            // Create default permissions based on role
            createDefaultPermissions(user);
            
            log.info("Tenant admin created successfully: {} for tenant: {}", user.getUserId(), tenantId);
            return user;
            
        } catch (Exception e) {
            log.error("Error creating tenant admin", e);
            throw new RuntimeException("Failed to create tenant admin", e);
        }
    }
    
    public ApiResponse<List<User>> getUsersByTenant() {
        Long tenantId = UserContext.getCurrentTenantId();
        
        try {
            List<User> users = userRepository.findByTenantIdAndActive(tenantId);
            return ApiResponse.success(users);
        } catch (Exception e) {
            log.error("Error fetching users for tenant: {}", tenantId, e);
            return ApiResponse.error("Failed to fetch users", "FETCH_ERROR");
        }
    }
    
    public ApiResponse<List<User>> getTeamMembers() {
        Long tenantId = UserContext.getCurrentTenantId();
        Long currentUserId = UserContext.getCurrentUserId();
        
        try {
            List<User> teamMembers = userRepository.findTeamMembersByManagerId(tenantId, currentUserId);
            return ApiResponse.success(teamMembers);
        } catch (Exception e) {
            log.error("Error fetching team members for user: {}", currentUserId, e);
            return ApiResponse.error("Failed to fetch team members", "FETCH_ERROR");
        }
    }
    
    public ApiResponse<User> getUserById(Long userId) {
        Long tenantId = UserContext.getCurrentTenantId();
        
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty() || !userOpt.get().getTenantId().equals(tenantId)) {
                return ApiResponse.error("User not found", "USER_NOT_FOUND");
            }
            
            return ApiResponse.success(userOpt.get());
        } catch (Exception e) {
            log.error("Error fetching user: {}", userId, e);
            return ApiResponse.error("Failed to fetch user", "FETCH_ERROR");
        }
    }
    
    public boolean canAccessUserData(Long targetUserId) {
        Long currentUserId = UserContext.getCurrentUserId();
        Long tenantId = UserContext.getCurrentTenantId();
        
        // Users can always access their own data
        if (currentUserId.equals(targetUserId)) {
            return true;
        }
        
        // Get current user's role
        Optional<User> currentUserOpt = userRepository.findById(currentUserId);
        if (currentUserOpt.isEmpty()) {
            return false;
        }
        
        User currentUser = currentUserOpt.get();
        
        // Super admin and tenant admin can access all user data
        if (currentUser.getRole() == User.UserRole.SUPER_ADMIN || 
            currentUser.getRole() == User.UserRole.TENANT_ADMIN) {
            return true;
        }
        
        // Sales manager can access team members' data
        if (currentUser.getRole() == User.UserRole.SALES_MANAGER) {
            List<User> teamMembers = userRepository.findTeamMembersByManagerId(tenantId, currentUserId);
            return teamMembers.stream().anyMatch(user -> user.getUserId().equals(targetUserId));
        }
        
        // Sales rep can only access their own data
        return false;
    }
    
    private void createDefaultPermissions(User user) {
        Long tenantId = user.getTenantId();
        Long userId = user.getUserId();
        
        switch (user.getRole()) {
            case SUPER_ADMIN:
                // Super admin has all permissions
                createPermission(tenantId, userId, "CONTACTS", "READ", "ALL");
                createPermission(tenantId, userId, "CONTACTS", "WRITE", "ALL");
                createPermission(tenantId, userId, "DEALS", "READ", "ALL");
                createPermission(tenantId, userId, "DEALS", "WRITE", "ALL");
                createPermission(tenantId, userId, "ACCOUNTS", "READ", "ALL");
                createPermission(tenantId, userId, "ACCOUNTS", "WRITE", "ALL");
                createPermission(tenantId, userId, "SETTINGS", "READ", "ALL");
                createPermission(tenantId, userId, "SETTINGS", "WRITE", "ALL");
                break;
                
            case TENANT_ADMIN:
                // Tenant admin has all permissions within tenant
                createPermission(tenantId, userId, "CONTACTS", "READ", "ALL");
                createPermission(tenantId, userId, "CONTACTS", "WRITE", "ALL");
                createPermission(tenantId, userId, "DEALS", "READ", "ALL");
                createPermission(tenantId, userId, "DEALS", "WRITE", "ALL");
                createPermission(tenantId, userId, "ACCOUNTS", "READ", "ALL");
                createPermission(tenantId, userId, "ACCOUNTS", "WRITE", "ALL");
                createPermission(tenantId, userId, "SETTINGS", "READ", "ALL");
                createPermission(tenantId, userId, "SETTINGS", "WRITE", "ALL");
                break;
                
            case SALES_MANAGER:
                // Sales manager can see team's deals and all contacts
                createPermission(tenantId, userId, "CONTACTS", "READ", "ALL");
                createPermission(tenantId, userId, "CONTACTS", "WRITE", "ALL");
                createPermission(tenantId, userId, "DEALS", "READ", "TEAM");
                createPermission(tenantId, userId, "DEALS", "WRITE", "TEAM");
                createPermission(tenantId, userId, "ACCOUNTS", "READ", "ALL");
                createPermission(tenantId, userId, "ACCOUNTS", "WRITE", "ALL");
                createPermission(tenantId, userId, "REPORTS", "READ", "TEAM");
                break;
                
            case SALES_REP:
                // Sales rep can only see their own deals
                createPermission(tenantId, userId, "CONTACTS", "READ", "ALL");
                createPermission(tenantId, userId, "CONTACTS", "WRITE", "OWN");
                createPermission(tenantId, userId, "DEALS", "READ", "OWN");
                createPermission(tenantId, userId, "DEALS", "WRITE", "OWN");
                createPermission(tenantId, userId, "ACCOUNTS", "READ", "ALL");
                createPermission(tenantId, userId, "ACCOUNTS", "WRITE", "OWN");
                break;
                
            case SUPPORT_AGENT:
                // Support agent can read all data but limited write access
                createPermission(tenantId, userId, "CONTACTS", "READ", "ALL");
                createPermission(tenantId, userId, "CONTACTS", "WRITE", "OWN");
                createPermission(tenantId, userId, "ACCOUNTS", "READ", "ALL");
                createPermission(tenantId, userId, "ACCOUNTS", "WRITE", "OWN");
                break;
                
            case READ_ONLY:
                // Read-only user can only read data
                createPermission(tenantId, userId, "CONTACTS", "READ", "ALL");
                createPermission(tenantId, userId, "DEALS", "READ", "ALL");
                createPermission(tenantId, userId, "ACCOUNTS", "READ", "ALL");
                break;
        }
    }
    
    private void createPermission(Long tenantId, Long userId, String resource, String action, String scope) {
        UserPermission permission = UserPermission.builder()
                .tenantId(tenantId)
                .userId(userId)
                .resource(resource)
                .action(action)
                .scope(scope)
                .build();
        
        userPermissionRepository.save(permission);
    }
    
    /**
     * Check if email exists globally across all tenants
     */
    public boolean existsByEmailGlobally(String email) {
        return userRepository.existsByEmail(email);
    }
    
    /**
     * Check if username exists globally across all tenants
     */
    public boolean existsByUsernameGlobally(String username) {
        return userRepository.existsByUsername(username);
    }
} 