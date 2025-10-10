package com.crmplatform.sales.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "lead_assignment_rules")
@JsonIgnoreProperties(ignoreUnknown = true)
public class LeadAssignmentRule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rule_id")
    private Long ruleId;
    
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    
    @Column(name = "rule_name", nullable = false)
    private String ruleName;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "priority_order")
    private Integer priorityOrder = 0;
    
    @Column(name = "conditions", columnDefinition = "JSON")
    private String conditions;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_strategy")
    private AssignmentStrategy assignmentStrategy = AssignmentStrategy.ROUND_ROBIN;
    
    @Column(name = "assigned_user_ids", columnDefinition = "JSON")
    private String assignedUserIds;
    
    @Column(name = "assigned_team_ids", columnDefinition = "JSON")
    private String assignedTeamIds;
    
    @Column(name = "created_by", nullable = false)
    private Long createdBy;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Helper methods for JSON fields
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public Map<String, Object> getConditionsMap() {
        if (conditions == null || conditions.isEmpty()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(conditions, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }
    
    public void setConditionsMap(Map<String, Object> conditionsMap) {
        try {
            this.conditions = objectMapper.writeValueAsString(conditionsMap);
        } catch (Exception e) {
            this.conditions = "{}";
        }
    }
    
    public List<Long> getAssignedUserIdsList() {
        if (assignedUserIds == null || assignedUserIds.isEmpty()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(assignedUserIds, new TypeReference<List<Long>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
    
    public void setAssignedUserIdsList(List<Long> userIds) {
        try {
            this.assignedUserIds = objectMapper.writeValueAsString(userIds);
        } catch (Exception e) {
            this.assignedUserIds = "[]";
        }
    }
    
    public List<Long> getAssignedTeamIdsList() {
        if (assignedTeamIds == null || assignedTeamIds.isEmpty()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(assignedTeamIds, new TypeReference<List<Long>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
    
    public void setAssignedTeamIdsList(List<Long> teamIds) {
        try {
            this.assignedTeamIds = objectMapper.writeValueAsString(teamIds);
        } catch (Exception e) {
            this.assignedTeamIds = "[]";
        }
    }
    
    // Constructors
    public LeadAssignmentRule() {}
    
    public LeadAssignmentRule(String ruleName, String description, Long tenantId, Long createdBy) {
        this.ruleName = ruleName;
        this.description = description;
        this.tenantId = tenantId;
        this.createdBy = createdBy;
    }
    
    // Getters and Setters
    public Long getRuleId() {
        return ruleId;
    }
    
    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }
    
    public Long getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
    
    public String getRuleName() {
        return ruleName;
    }
    
    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public Integer getPriorityOrder() {
        return priorityOrder;
    }
    
    public void setPriorityOrder(Integer priorityOrder) {
        this.priorityOrder = priorityOrder;
    }
    
    public String getConditions() {
        return conditions;
    }
    
    public void setConditions(String conditions) {
        this.conditions = conditions;
    }
    
    public AssignmentStrategy getAssignmentStrategy() {
        return assignmentStrategy;
    }
    
    public void setAssignmentStrategy(AssignmentStrategy assignmentStrategy) {
        this.assignmentStrategy = assignmentStrategy;
    }
    
    public String getAssignedUserIds() {
        return assignedUserIds;
    }
    
    public void setAssignedUserIds(String assignedUserIds) {
        this.assignedUserIds = assignedUserIds;
    }
    
    public String getAssignedTeamIds() {
        return assignedTeamIds;
    }
    
    public void setAssignedTeamIds(String assignedTeamIds) {
        this.assignedTeamIds = assignedTeamIds;
    }
    
    public Long getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public Long getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}


