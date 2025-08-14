package com.crmplatform.gateway.controller;

import com.crmplatform.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
@Slf4j
public class FallbackController {

    @GetMapping("/auth")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> authFallback() {
        log.warn("Auth service is unavailable - using fallback response");
        
        Map<String, Object> fallbackData = Map.of(
                "message", "Authentication service is temporarily unavailable",
                "timestamp", LocalDateTime.now(),
                "service", "auth-service",
                "status", "degraded"
        );
        
        ApiResponse<Map<String, Object>> response = ApiResponse.error(
                "Authentication service is temporarily unavailable. Please try again later.",
                "SERVICE_UNAVAILABLE"
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    @GetMapping("/contacts")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> contactsFallback() {
        log.warn("Contacts service is unavailable - using fallback response");
        
        Map<String, Object> fallbackData = Map.of(
                "message", "Contacts service is temporarily unavailable",
                "timestamp", LocalDateTime.now(),
                "service", "contacts-service",
                "status", "degraded"
        );
        
        ApiResponse<Map<String, Object>> response = ApiResponse.error(
                "Contacts service is temporarily unavailable. Please try again later.",
                "SERVICE_UNAVAILABLE"
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    @GetMapping("/sales")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> salesFallback() {
        log.warn("Sales service is unavailable - using fallback response");
        
        Map<String, Object> fallbackData = Map.of(
                "message", "Sales service is temporarily unavailable",
                "timestamp", LocalDateTime.now(),
                "service", "sales-service",
                "status", "degraded"
        );
        
        ApiResponse<Map<String, Object>> response = ApiResponse.error(
                "Sales service is temporarily unavailable. Please try again later.",
                "SERVICE_UNAVAILABLE"
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    @GetMapping("/activity")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> activityFallback() {
        log.warn("Activity service is unavailable - using fallback response");
        
        Map<String, Object> fallbackData = Map.of(
                "message", "Activity service is temporarily unavailable",
                "timestamp", LocalDateTime.now(),
                "service", "activity-service",
                "status", "degraded"
        );
        
        ApiResponse<Map<String, Object>> response = ApiResponse.error(
                "Activity service is temporarily unavailable. Please try again later.",
                "SERVICE_UNAVAILABLE"
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    @GetMapping("/users")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> usersFallback() {
        log.warn("User management service is unavailable - using fallback response");
        
        Map<String, Object> fallbackData = Map.of(
                "message", "User management service is temporarily unavailable",
                "timestamp", LocalDateTime.now(),
                "service", "user-management",
                "status", "degraded"
        );
        
        ApiResponse<Map<String, Object>> response = ApiResponse.error(
                "User management service is temporarily unavailable. Please try again later.",
                "SERVICE_UNAVAILABLE"
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }
} 