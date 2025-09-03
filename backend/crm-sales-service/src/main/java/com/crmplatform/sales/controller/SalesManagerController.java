package com.crmplatform.sales.controller;

import com.crmplatform.common.dto.ApiResponse;
import com.crmplatform.sales.service.SalesManagerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/sales-manager")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('SALES_MANAGER') or hasRole('TENANT_ADMIN')")
public class SalesManagerController {

    private final SalesManagerService salesManagerService;

    @GetMapping("/team-stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTeamStats() {
        try {
            log.info("Fetching real-time team statistics from database");
            
            Map<String, Object> teamStats = salesManagerService.getTeamStatistics();
            
            return ResponseEntity.ok(ApiResponse.success(teamStats));
            
        } catch (Exception e) {
            log.error("Error getting team stats", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch team statistics"));
        }
    }
}
