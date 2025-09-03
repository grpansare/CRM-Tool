package com.crmplatform.sales.controller;

import com.crmplatform.common.dto.ApiResponse;
import com.crmplatform.common.security.UserContext;
import com.crmplatform.sales.dto.*;
import com.crmplatform.sales.service.SalesPipelineService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pipelines")
@RequiredArgsConstructor
@Slf4j
public class SalesPipelineController {
    
    private final SalesPipelineService pipelineService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<SalesPipelineResponse>>> getAllPipelines() {
        try {
            Long tenantId = UserContext.getCurrentTenantId();
            List<SalesPipelineResponse> pipelines = pipelineService.getAllPipelines(tenantId);
            
            return ResponseEntity.ok(ApiResponse.<List<SalesPipelineResponse>>builder()
                    .success(true)
                    .data(pipelines)
                    .message("Pipelines retrieved successfully")
                    .build());
        } catch (Exception e) {
            log.error("Error retrieving pipelines", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<SalesPipelineResponse>>builder()
                            .success(false)
                            .message("Failed to retrieve pipelines: " + e.getMessage())
                            .build());
        }
    }
    
    @GetMapping("/{pipelineId}")
    public ResponseEntity<ApiResponse<SalesPipelineResponse>> getPipelineById(@PathVariable Long pipelineId) {
        try {
            Long tenantId = UserContext.getCurrentTenantId();
            SalesPipelineResponse pipeline = pipelineService.getPipelineById(pipelineId, tenantId);
            
            return ResponseEntity.ok(ApiResponse.<SalesPipelineResponse>builder()
                    .success(true)
                    .data(pipeline)
                    .message("Pipeline retrieved successfully")
                    .build());
        } catch (Exception e) {
            log.error("Error retrieving pipeline with ID: {}", pipelineId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<SalesPipelineResponse>builder()
                            .success(false)
                            .message("Pipeline not found: " + e.getMessage())
                            .build());
        }
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<SalesPipelineResponse>> createPipeline(@Valid @RequestBody SalesPipelineRequest request) {
        try {
            Long tenantId = UserContext.getCurrentTenantId();
            Long userId = UserContext.getCurrentUserId();
            String username = UserContext.getCurrentUsername();
            
            log.debug("=== PIPELINE CREATION DEBUG ===");
            log.debug("Request: {}", request);
            log.debug("UserContext - TenantId: {}, UserId: {}, Username: {}", tenantId, userId, username);
            
            if (tenantId == null) {
                log.error("TenantId is null in UserContext");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.<SalesPipelineResponse>builder()
                                .success(false)
                                .message("Tenant context not found. Please ensure you are properly authenticated.")
                                .build());
            }
            
            SalesPipelineResponse pipeline = pipelineService.createPipeline(request, tenantId);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.<SalesPipelineResponse>builder()
                            .success(true)
                            .data(pipeline)
                            .message("Pipeline created successfully")
                            .build());
        } catch (Exception e) {
            log.error("Error creating pipeline", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<SalesPipelineResponse>builder()
                            .success(false)
                            .message("Failed to create pipeline: " + e.getMessage())
                            .build());
        }
    }
    
    @PutMapping("/{pipelineId}")
    public ResponseEntity<ApiResponse<SalesPipelineResponse>> updatePipeline(
            @PathVariable Long pipelineId, 
            @Valid @RequestBody SalesPipelineRequest request) {
        try {
            Long tenantId = UserContext.getCurrentTenantId();
            SalesPipelineResponse pipeline = pipelineService.updatePipeline(pipelineId, request, tenantId);
            
            return ResponseEntity.ok(ApiResponse.<SalesPipelineResponse>builder()
                    .success(true)
                    .data(pipeline)
                    .message("Pipeline updated successfully")
                    .build());
        } catch (Exception e) {
            log.error("Error updating pipeline with ID: {}", pipelineId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<SalesPipelineResponse>builder()
                            .success(false)
                            .message("Failed to update pipeline: " + e.getMessage())
                            .build());
        }
    }
    
    @DeleteMapping("/{pipelineId}")
    public ResponseEntity<ApiResponse<Void>> deletePipeline(@PathVariable Long pipelineId) {
        try {
            Long tenantId = UserContext.getCurrentTenantId();
            pipelineService.deletePipeline(pipelineId, tenantId);
            
            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .success(true)
                    .message("Pipeline deleted successfully")
                    .build());
        } catch (Exception e) {
            log.error("Error deleting pipeline with ID: {}", pipelineId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message("Failed to delete pipeline: " + e.getMessage())
                            .build());
        }
    }
    
    @PostMapping("/{pipelineId}/stages")
    public ResponseEntity<ApiResponse<PipelineStageResponse>> createStage(
            @PathVariable Long pipelineId,
            @Valid @RequestBody PipelineStageRequest request) {
        try {
            Long tenantId = UserContext.getCurrentTenantId();
            PipelineStageResponse stage = pipelineService.createStage(pipelineId, request, tenantId);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.<PipelineStageResponse>builder()
                            .success(true)
                            .data(stage)
                            .message("Stage created successfully")
                            .build());
        } catch (Exception e) {
            log.error("Error creating stage for pipeline ID: {}", pipelineId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<PipelineStageResponse>builder()
                            .success(false)
                            .message("Failed to create stage: " + e.getMessage())
                            .build());
        }
    }
    
    @PutMapping("/stages/{stageId}")
    public ResponseEntity<ApiResponse<PipelineStageResponse>> updateStage(
            @PathVariable Long stageId,
            @Valid @RequestBody PipelineStageRequest request) {
        try {
            Long tenantId = UserContext.getCurrentTenantId();
            PipelineStageResponse stage = pipelineService.updateStage(stageId, request, tenantId);
            
            return ResponseEntity.ok(ApiResponse.<PipelineStageResponse>builder()
                    .success(true)
                    .data(stage)
                    .message("Stage updated successfully")
                    .build());
        } catch (Exception e) {
            log.error("Error updating stage with ID: {}", stageId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<PipelineStageResponse>builder()
                            .success(false)
                            .message("Failed to update stage: " + e.getMessage())
                            .build());
        }
    }
    
    @DeleteMapping("/stages/{stageId}")
    public ResponseEntity<ApiResponse<Void>> deleteStage(@PathVariable Long stageId) {
        try {
            Long tenantId = UserContext.getCurrentTenantId();
            pipelineService.deleteStage(stageId, tenantId);
            
            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .success(true)
                    .message("Stage deleted successfully")
                    .build());
        } catch (Exception e) {
            log.error("Error deleting stage with ID: {}", stageId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message("Failed to delete stage: " + e.getMessage())
                            .build());
        }
    }
}
