//package com.crmplatform.sales.controller;
//
//import com.crmplatform.sales.entity.LeadAssignmentRule;
//import com.crmplatform.sales.entity.AssignmentStrategy;
//import com.crmplatform.sales.service.LeadAssignmentService;
//import com.crmplatform.sales.repository.LeadAssignmentRuleRepository;
//import com.crmplatform.common.dto.ApiResponse;
//import com.crmplatform.common.security.UserContext;
//import jakarta.validation.Valid;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/v1/lead-assignment")
//public class LeadAssignmentController {
//    
//    @Autowired
//    private LeadAssignmentService assignmentService;
//    
//    @Autowired
//    private LeadAssignmentRuleRepository assignmentRuleRepository;
//    
//    /**
//     * Get all assignment rules for current tenant
//     */
//    @GetMapping("/rules")
//    public ResponseEntity<ApiResponse<Page<LeadAssignmentRule>>> getAllRules(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size) {
//        try {
//            Pageable pageable = PageRequest.of(page, size);
//            Page<LeadAssignmentRule> rules = assignmentRuleRepository
//                .findByTenantIdOrderByPriorityOrderAsc(UserContext.getCurrentTenantId(), pageable);
//            
//            return ResponseEntity.ok(ApiResponse.success(rules));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(ApiResponse.error("Failed to fetch assignment rules: " + e.getMessage()));
//        }
//    }
//    
//    /**
//     * Get active assignment rules
//     */
//    @GetMapping("/rules/active")
//    public ResponseEntity<ApiResponse<List<LeadAssignmentRule>>> getActiveRules() {
//        try {
//            List<LeadAssignmentRule> rules = assignmentRuleRepository
//                .findByTenantIdAndIsActiveOrderByPriorityOrderAsc(UserContext.getCurrentTenantId(), true);
//            
//            return ResponseEntity.ok(ApiResponse.success(rules));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(ApiResponse.error("Failed to fetch active assignment rules: " + e.getMessage()));
//        }
//    }
//    
//    /**
//     * Get assignment rule by ID
//     */
//    @GetMapping("/rules/{ruleId}")
//    public ResponseEntity<ApiResponse<LeadAssignmentRule>> getRuleById(@PathVariable Long ruleId) {
//        try {
//            LeadAssignmentRule rule = assignmentRuleRepository.findById(ruleId)
//                .orElseThrow(() -> new RuntimeException("Assignment rule not found"));
//            
//            // Check tenant access
//            if (!rule.getTenantId().equals(UserContext.getCurrentTenantId())) {
//                return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                    .body(ApiResponse.error("Access denied"));
//            }
//            
//            return ResponseEntity.ok(ApiResponse.success(rule));
//        } catch (RuntimeException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                .body(ApiResponse.error(e.getMessage()));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(ApiResponse.error("Failed to fetch assignment rule: " + e.getMessage()));
//        }
//    }
//    
//    /**
//     * Create new assignment rule
//     */
//    @PostMapping("/rules")
//    public ResponseEntity<ApiResponse<LeadAssignmentRule>> createRule(
//            @Valid @RequestBody CreateAssignmentRuleRequest request) {
//        try {
//            // Check if rule name already exists
//            if (assignmentRuleRepository.existsByTenantIdAndRuleName(
//                    UserContext.getCurrentTenantId(), request.getRuleName())) {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body(ApiResponse.error("Assignment rule with this name already exists"));
//            }
//            
//            LeadAssignmentRule rule = new LeadAssignmentRule();
//            rule.setTenantId(UserContext.getCurrentTenantId());
//            rule.setRuleName(request.getRuleName());
//            rule.setDescription(request.getDescription());
//            rule.setPriorityOrder(request.getPriorityOrder());
//            rule.setAssignmentStrategy(request.getAssignmentStrategy());
//            rule.setConditionsMap(request.getConditions());
//            rule.setAssignedUserIdsList(request.getAssignedUserIds());
//            rule.setAssignedTeamIdsList(request.getAssignedTeamIds());
//            rule.setIsActive(request.getIsActive());
//            rule.setCreatedBy(UserContext.getCurrentUserId());
//            
//            LeadAssignmentRule savedRule = assignmentRuleRepository.save(rule);
//            
//            return ResponseEntity.status(HttpStatus.CREATED)
//                .body(ApiResponse.success(savedRule));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(ApiResponse.error("Failed to create assignment rule: " + e.getMessage()));
//        }
//    }
//    
//    /**
//     * Update assignment rule
//     */
//    @PutMapping("/rules/{ruleId}")
//    public ResponseEntity<ApiResponse<LeadAssignmentRule>> updateRule(
//            @PathVariable Long ruleId,
//            @Valid @RequestBody UpdateAssignmentRuleRequest request) {
//        try {
//            LeadAssignmentRule rule = assignmentRuleRepository.findById(ruleId)
//                .orElseThrow(() -> new RuntimeException("Assignment rule not found"));
//            
//            // Check tenant access
//            if (!rule.getTenantId().equals(UserContext.getCurrentTenantId())) {
//                return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                    .body(ApiResponse.error("Access denied"));
//            }
//            
//            // Check if new name conflicts with existing rule
//            if (!rule.getRuleName().equals(request.getRuleName()) &&
//                assignmentRuleRepository.existsByTenantIdAndRuleName(
//                    UserContext.getCurrentTenantId(), request.getRuleName())) {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body(ApiResponse.error("Assignment rule with this name already exists"));
//            }
//            
//            rule.setRuleName(request.getRuleName());
//            rule.setDescription(request.getDescription());
//            rule.setPriorityOrder(request.getPriorityOrder());
//            rule.setAssignmentStrategy(request.getAssignmentStrategy());
//            rule.setConditionsMap(request.getConditions());
//            rule.setAssignedUserIdsList(request.getAssignedUserIds());
//            rule.setAssignedTeamIdsList(request.getAssignedTeamIds());
//            rule.setIsActive(request.getIsActive());
//            rule.setUpdatedBy(UserContext.getCurrentUserId());
//            
//            LeadAssignmentRule updatedRule = assignmentRuleRepository.save(rule);
//            
//            return ResponseEntity.ok(ApiResponse.success(updatedRule));
//        } catch (RuntimeException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                .body(ApiResponse.error(e.getMessage()));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(ApiResponse.error("Failed to update assignment rule: " + e.getMessage()));
//        }
//    }
//    
//    /**
//     * Delete assignment rule
//     */
//    @DeleteMapping("/rules/{ruleId}")
//    public ResponseEntity<ApiResponse<Void>> deleteRule(@PathVariable Long ruleId) {
//        try {
//            LeadAssignmentRule rule = assignmentRuleRepository.findById(ruleId)
//                .orElseThrow(() -> new RuntimeException("Assignment rule not found"));
//            
//            // Check tenant access
//            if (!rule.getTenantId().equals(UserContext.getCurrentTenantId())) {
//                return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                    .body(ApiResponse.error("Access denied"));
//            }
//            
//            assignmentRuleRepository.delete(rule);
//            
//            return ResponseEntity.ok(ApiResponse.success(null, "Assignment rule deleted successfully"));
//        } catch (RuntimeException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                .body(ApiResponse.error(e.getMessage()));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(ApiResponse.error("Failed to delete assignment rule: " + e.getMessage()));
//        }
//    }
//    
//    /**
//     * Manually reassign a lead
//     */
//    @PostMapping("/reassign/{leadId}")
//    public ResponseEntity<ApiResponse<Void>> reassignLead(
//            @PathVariable Long leadId,
//            @RequestBody ReassignLeadRequest request) {
//        try {
//            assignmentService.reassignLead(leadId, request.getNewUserId(), request.getReason());
//            
//            return ResponseEntity.ok(ApiResponse.success(null, "Lead reassigned successfully"));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(ApiResponse.error("Failed to reassign lead: " + e.getMessage()));
//        }
//    }
//}
//
//// Request DTOs
//class CreateAssignmentRuleRequest {
//    private String ruleName;
//    private String description;
//    private Integer priorityOrder = 1;
//    private AssignmentStrategy assignmentStrategy = AssignmentStrategy.ROUND_ROBIN;
//    private Map<String, Object> conditions;
//    private List<Long> assignedUserIds;
//    private List<Long> assignedTeamIds;
//    private Boolean isActive = true;
//    
//    // Getters and setters
//    public String getRuleName() { return ruleName; }
//    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
//    public String getDescription() { return description; }
//    public void setDescription(String description) { this.description = description; }
//    public Integer getPriorityOrder() { return priorityOrder; }
//    public void setPriorityOrder(Integer priorityOrder) { this.priorityOrder = priorityOrder; }
//    public AssignmentStrategy getAssignmentStrategy() { return assignmentStrategy; }
//    public void setAssignmentStrategy(AssignmentStrategy assignmentStrategy) { this.assignmentStrategy = assignmentStrategy; }
//    public Map<String, Object> getConditions() { return conditions; }
//    public void setConditions(Map<String, Object> conditions) { this.conditions = conditions; }
//    public List<Long> getAssignedUserIds() { return assignedUserIds; }
//    public void setAssignedUserIds(List<Long> assignedUserIds) { this.assignedUserIds = assignedUserIds; }
//    public List<Long> getAssignedTeamIds() { return assignedTeamIds; }
//    public void setAssignedTeamIds(List<Long> assignedTeamIds) { this.assignedTeamIds = assignedTeamIds; }
//    public Boolean getIsActive() { return isActive; }
//    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
//}
//
//class UpdateAssignmentRuleRequest {
//    private String ruleName;
//    private String description;
//    private Integer priorityOrder;
//    private AssignmentStrategy assignmentStrategy;
//    private Map<String, Object> conditions;
//    private List<Long> assignedUserIds;
//    private List<Long> assignedTeamIds;
//    private Boolean isActive;
//    
//    // Getters and setters
//    public String getRuleName() { return ruleName; }
//    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
//    public String getDescription() { return description; }
//    public void setDescription(String description) { this.description = description; }
//    public Integer getPriorityOrder() { return priorityOrder; }
//    public void setPriorityOrder(Integer priorityOrder) { this.priorityOrder = priorityOrder; }
//    public AssignmentStrategy getAssignmentStrategy() { return assignmentStrategy; }
//    public void setAssignmentStrategy(AssignmentStrategy assignmentStrategy) { this.assignmentStrategy = assignmentStrategy; }
//    public Map<String, Object> getConditions() { return conditions; }
//    public void setConditions(Map<String, Object> conditions) { this.conditions = conditions; }
//    public List<Long> getAssignedUserIds() { return assignedUserIds; }
//    public void setAssignedUserIds(List<Long> assignedUserIds) { this.assignedUserIds = assignedUserIds; }
//    public List<Long> getAssignedTeamIds() { return assignedTeamIds; }
//    public void setAssignedTeamIds(List<Long> assignedTeamIds) { this.assignedTeamIds = assignedTeamIds; }
//    public Boolean getIsActive() { return isActive; }
//    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
//}
//
//class ReassignLeadRequest {
//    private Long newUserId;
//    private String reason;
//    
//    public Long getNewUserId() { return newUserId; }
//    public void setNewUserId(Long newUserId) { this.newUserId = newUserId; }
//    public String getReason() { return reason; }
//    public void setReason(String reason) { this.reason = reason; }
//}
