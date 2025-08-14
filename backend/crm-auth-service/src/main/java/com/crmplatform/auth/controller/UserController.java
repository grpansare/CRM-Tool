package com.crmplatform.auth.controller;

import com.crmplatform.auth.dto.CreateUserRequest;
import com.crmplatform.auth.entity.User;
import com.crmplatform.auth.service.UserService;
import com.crmplatform.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    
    private final UserService userService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<User>> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("Creating user: {}", request.getEmail());
        
        ApiResponse<User> response = userService.createUser(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getUsers() {
        log.info("Getting users for current tenant");
        
        ApiResponse<List<User>> response = userService.getUsersByTenant();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/team")
    public ResponseEntity<ApiResponse<List<User>>> getTeamMembers() {
        log.info("Getting team members for current user");
        
        ApiResponse<List<User>> response = userService.getTeamMembers();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<User>> getUser(@PathVariable Long userId) {
        log.info("Getting user: {}", userId);
        
        // Check if current user can access this user's data
        if (!userService.canAccessUserData(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied", "ACCESS_DENIED"));
        }
        
        ApiResponse<User> response = userService.getUserById(userId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
} 