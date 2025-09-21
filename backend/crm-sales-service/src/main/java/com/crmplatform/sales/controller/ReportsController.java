package com.crmplatform.sales.controller;

import com.crmplatform.sales.service.ReportsService;
import com.crmplatform.common.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportsController {
    
    @Autowired
    private ReportsService reportsService;
    
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardReport() {
        try {
            Map<String, Object> report = reportsService.getDashboardReport();
            return ResponseEntity.ok(ApiResponse.success(report));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to generate dashboard report: " + e.getMessage()));
        }
    }
    
    @GetMapping("/leads/overview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLeadOverviewReport() {
        try {
            Map<String, Object> report = reportsService.getLeadOverviewReport();
            return ResponseEntity.ok(ApiResponse.success(report));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to generate lead overview report: " + e.getMessage()));
        }
    }
    
    @GetMapping("/leads/dispositions")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDispositionReport() {
        try {
            Map<String, Object> report = reportsService.getDispositionReport();
            return ResponseEntity.ok(ApiResponse.success(report));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to generate disposition report: " + e.getMessage()));
        }
    }
    
    @GetMapping("/leads/sources")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSourcePerformanceReport() {
        try {
            Map<String, Object> report = reportsService.getSourcePerformanceReport();
            return ResponseEntity.ok(ApiResponse.success(report));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to generate source performance report: " + e.getMessage()));
        }
    }
    
    @GetMapping("/leads/scores")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLeadScoreReport() {
        try {
            Map<String, Object> report = reportsService.getLeadScoreReport();
            return ResponseEntity.ok(ApiResponse.success(report));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to generate lead score report: " + e.getMessage()));
        }
    }
    
    @GetMapping("/leads/time-based")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTimeBasedReport(
            @RequestParam(defaultValue = "30") int days) {
        try {
            Map<String, Object> report = reportsService.getTimeBasedReport(days);
            return ResponseEntity.ok(ApiResponse.success(report));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to generate time-based report: " + e.getMessage()));
        }
    }
    
    @GetMapping("/activity/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getActivitySummaryReport() {
        try {
            Map<String, Object> report = reportsService.getActivitySummaryReport();
            return ResponseEntity.ok(ApiResponse.success(report));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to generate activity summary report: " + e.getMessage()));
        }
    }
}
