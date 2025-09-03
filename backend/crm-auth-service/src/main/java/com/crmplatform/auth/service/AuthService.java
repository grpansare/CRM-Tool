package com.crmplatform.auth.service;

import com.crmplatform.auth.dto.LoginRequest;
import com.crmplatform.auth.dto.LoginResponse;
import com.crmplatform.auth.entity.User;
import com.crmplatform.auth.entity.UserPermission;
import com.crmplatform.auth.repository.UserPermissionRepository;
import com.crmplatform.auth.repository.UserRepository;
import com.crmplatform.common.dto.ApiResponse;
import com.crmplatform.common.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional
    public ApiResponse<LoginResponse> login(LoginRequest request) {
        try {
            // Find user by email
            Optional<User> userOpt = userRepository.findByEmailAndActive(request.getEmail());
            if (userOpt.isEmpty()) {
                log.warn("Login attempt failed for email: {} - User not found", request.getEmail());
                return ApiResponse.error("Invalid credentials", "INVALID_CREDENTIALS");
            }
            
            User user = userOpt.get();
            
            // Validate password
            if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                log.warn("Login attempt failed for user: {} - Invalid password", user.getUserId());
                return ApiResponse.error("Invalid credentials", "INVALID_CREDENTIALS");
            }
            
            // Check if user is active
            if (!user.getIsActive()) {
                log.warn("Login attempt failed for user: {} - User inactive", user.getUserId());
                return ApiResponse.error("Account is deactivated", "ACCOUNT_DEACTIVATED");
            }
            
            // Update last login
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
            
            // Generate JWT token
            String accessToken = jwtTokenProvider.generateToken(
                user.getUsername(), 
                user.getUserId(), 
                user.getTenantId(), 
                user.getRole().name()
            );
            
            // Generate refresh token (for remember me functionality)
            String refreshToken = null;
            if (Boolean.TRUE.equals(request.getRememberMe())) {
                refreshToken = jwtTokenProvider.generateToken(
                    user.getUsername(), 
                    user.getUserId(), 
                    user.getTenantId(), 
                    user.getRole().name()
                );
            }
            
            // Get user permissions
            List<String> permissions = getUserPermissions(user.getTenantId(), user.getUserId());
            
            // Build response
            LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                    .userId(user.getUserId())
                    .tenantId(user.getTenantId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .role(user.getRole().name())
                    .managerId(user.getManagerId())
                    .lastLogin(user.getLastLogin())
                    .isActive(user.getIsActive())
                    .build();
            
            LoginResponse loginResponse = LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(86400L) // 24 hours
                    .user(userInfo)
                    .permissions(permissions)
                    .build();
            
            log.info("User {} logged in successfully", user.getUserId());
            
            return ApiResponse.success(loginResponse, "Login successful");
            
        } catch (Exception e) {
            log.error("Login error for email: {}", request.getEmail(), e);
            return ApiResponse.error("Login failed", "LOGIN_ERROR");
        }
    }
    
    public ApiResponse<Boolean> validateToken(String token) {
        try {
            boolean isValid = jwtTokenProvider.validateToken(token);
            if (isValid) {
                return ApiResponse.success(true, "Token is valid");
            } else {
                return ApiResponse.error("Token is invalid", "INVALID_TOKEN");
            }
        } catch (Exception e) {
            log.error("Token validation error", e);
            return ApiResponse.error("Token validation failed", "VALIDATION_ERROR");
        }
    }
    
    public ApiResponse<User> getUserFromToken(String token) {
        try {
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            if (userId == null) {
                return ApiResponse.error("Invalid token", "INVALID_TOKEN");
            }
            
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ApiResponse.error("User not found", "USER_NOT_FOUND");
            }
            
            return ApiResponse.success(userOpt.get());
        } catch (Exception e) {
            log.error("Error getting user from token", e);
            return ApiResponse.error("Token processing failed", "TOKEN_ERROR");
        }
    }
    
    public List<Long> getTeamMemberIds(Long tenantId, Long managerId) {
        List<User> teamMembers = userRepository.findTeamMembersByManagerId(tenantId, managerId);
        return teamMembers.stream()
                .map(User::getUserId)
                .collect(Collectors.toList());
    }
    
    public boolean hasPermission(Long tenantId, Long userId, String resource, String action) {
        List<UserPermission> permissions = userPermissionRepository
                .findByTenantIdAndUserIdAndResourceAndAction(tenantId, userId, resource, action);
        return !permissions.isEmpty();
    }
    
    public boolean hasPermissionScope(Long tenantId, Long userId, String resource, String action, String scope) {
        List<UserPermission> permissions = userPermissionRepository
                .findByTenantIdAndUserIdAndResourceAndAction(tenantId, userId, resource, action);
        
        return permissions.stream()
                .anyMatch(permission -> scope.equals(permission.getScope()));
    }
    
    private List<String> getUserPermissions(Long tenantId, Long userId) {
        List<UserPermission> permissions = userPermissionRepository
                .findByTenantIdAndUserId(tenantId, userId);
        
        return permissions.stream()
                .map(permission -> permission.getResource() + ":" + permission.getAction() + ":" + permission.getScope())
                .collect(Collectors.toList());
    }
} 