package com.crmplatform.sales.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lead_assignment_history")
public class LeadAssignmentHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;
    
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    
    @Column(name = "lead_id", nullable = false)
    private Long leadId;
    
    @Column(name = "rule_id")
    private Long ruleId;
    
    @Column(name = "assigned_from_user_id")
    private Long assignedFromUserId;
    
    @Column(name = "assigned_to_user_id", nullable = false)
    private Long assignedToUserId;
    
    @Column(name = "assignment_reason")
    private String assignmentReason;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_method")
    private AssignmentMethod assignmentMethod;
    
    @Column(name = "assigned_by", nullable = false)
    private Long assignedBy;
    
    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;
    
    @PrePersist
    protected void onCreate() {
        assignedAt = LocalDateTime.now();
    }
    
    // Constructors
    public LeadAssignmentHistory() {}
    
    public LeadAssignmentHistory(Long tenantId, Long leadId, Long assignedToUserId, Long assignedBy) {
        this.tenantId = tenantId;
        this.leadId = leadId;
        this.assignedToUserId = assignedToUserId;
        this.assignedBy = assignedBy;
    }
    
    // Getters and Setters
    public Long getHistoryId() {
        return historyId;
    }
    
    public void setHistoryId(Long historyId) {
        this.historyId = historyId;
    }
    
    public Long getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
    
    public Long getLeadId() {
        return leadId;
    }
    
    public void setLeadId(Long leadId) {
        this.leadId = leadId;
    }
    
    public Long getRuleId() {
        return ruleId;
    }
    
    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }
    
    public Long getAssignedFromUserId() {
        return assignedFromUserId;
    }
    
    public void setAssignedFromUserId(Long assignedFromUserId) {
        this.assignedFromUserId = assignedFromUserId;
    }
    
    public Long getAssignedToUserId() {
        return assignedToUserId;
    }
    
    public void setAssignedToUserId(Long assignedToUserId) {
        this.assignedToUserId = assignedToUserId;
    }
    
    public String getAssignmentReason() {
        return assignmentReason;
    }
    
    public void setAssignmentReason(String assignmentReason) {
        this.assignmentReason = assignmentReason;
    }
    
    public AssignmentMethod getAssignmentMethod() {
        return assignmentMethod;
    }
    
    public void setAssignmentMethod(AssignmentMethod assignmentMethod) {
        this.assignmentMethod = assignmentMethod;
    }
    
    public Long getAssignedBy() {
        return assignedBy;
    }
    
    public void setAssignedBy(Long assignedBy) {
        this.assignedBy = assignedBy;
    }
    
    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }
    
    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
}

