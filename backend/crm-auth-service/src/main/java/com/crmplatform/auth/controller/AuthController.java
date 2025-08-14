package com.crmplatform.auth.controller;

import com.crmplatform.auth.dto.LoginRequest;
import com.crmplatform.auth.dto.LoginResponse;
import com.crmplatform.auth.entity.User;
import com.crmplatform.auth.service.AuthService;
import com.crmplatform.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());
        
        ApiResponse<LoginResponse> response = authService.login(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(@RequestHeader("Authorization") String token) {
        // Remove "Bearer " prefix if present
        String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        
        log.info("Token validation request");
        
        ApiResponse<Boolean> response = authService.validateToken(cleanToken);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> getCurrentUser(@RequestHeader("Authorization") String token) {
        // Remove "Bearer " prefix if present
        String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        
        log.info("Get current user request");
        
        ApiResponse<User> response = authService.getUserFromToken(cleanToken);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
} 