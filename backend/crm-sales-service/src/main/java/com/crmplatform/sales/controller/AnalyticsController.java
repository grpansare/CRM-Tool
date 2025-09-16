package com.crmplatform.sales.controller;

import com.crmplatform.common.dto.ApiResponse;
import com.crmplatform.sales.dto.AnalyticsResponse;
import com.crmplatform.sales.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/revenue-forecast")
    @PreAuthorize("hasRole('SALES_MANAGER') or hasRole('TENANT_ADMIN') or hasRole('SALES_REP')")
    public ResponseEntity<ApiResponse<AnalyticsResponse.RevenueForecast>> getRevenueForecast() {
        log.info("Getting revenue forecast");
        
        AnalyticsResponse.RevenueForecast forecast = analyticsService.getRevenueForecast();
        return ResponseEntity.ok(ApiResponse.success(forecast, "Revenue forecast generated successfully"));
    }

    @GetMapping("/sales-velocity")
    @PreAuthorize("hasRole('SALES_MANAGER') or hasRole('TENANT_ADMIN') or hasRole('SALES_REP')")
    public ResponseEntity<ApiResponse<AnalyticsResponse.SalesVelocity>> getSalesVelocity() {
        log.info("Getting sales velocity metrics");
        
        AnalyticsResponse.SalesVelocity velocity = analyticsService.getSalesVelocity();
        return ResponseEntity.ok(ApiResponse.success(velocity, "Sales velocity metrics generated successfully"));
    }

    @GetMapping("/conversion-rates")
    @PreAuthorize("hasRole('SALES_MANAGER') or hasRole('TENANT_ADMIN') or hasRole('SALES_REP')")
    public ResponseEntity<ApiResponse<AnalyticsResponse.ConversionRates>> getConversionRates() {
        log.info("Getting conversion rates");
        
        AnalyticsResponse.ConversionRates rates = analyticsService.getConversionRates();
        return ResponseEntity.ok(ApiResponse.success(rates, "Conversion rates generated successfully"));
    }

    @GetMapping("/comprehensive")
    @PreAuthorize("hasRole('SALES_MANAGER') or hasRole('TENANT_ADMIN') or hasRole('SALES_REP')")
    public ResponseEntity<ApiResponse<AnalyticsResponse>> getComprehensiveAnalytics() {
        log.info("Getting comprehensive analytics");
        
        AnalyticsResponse analytics = analyticsService.getComprehensiveAnalytics();
        return ResponseEntity.ok(ApiResponse.success(analytics, "Comprehensive analytics generated successfully"));
    }
}
