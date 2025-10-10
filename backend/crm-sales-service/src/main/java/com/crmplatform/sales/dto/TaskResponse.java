package com.crmplatform.sales.dto;

import com.crmplatform.sales.entity.Task;
import com.crmplatform.sales.entity.TaskPriority;
import com.crmplatform.sales.entity.TaskStatus;
import com.crmplatform.sales.entity.TaskType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskResponse {
    
    private Long taskId;
    private Long tenantId;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private TaskType type;
    private Long assignedToUserId;
    private String assignedToUserName;
    private Long createdByUserId;
    private String createdByUserName;
    private LocalDateTime dueDate;
    private LocalDateTime completedAt;
    
    // Related entity information
    private Long relatedContactId;
    private String relatedContactName;
    private Long relatedAccountId;
    private String relatedAccountName;
    private Long relatedDealId;
    private String relatedDealTitle;
    private Long relatedLeadId;
    private String relatedLeadName;
    private String relatedEntityType;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Computed fields
    private boolean overdue;
    private boolean dueToday;
    private boolean dueSoon;
    private long daysUntilDue;
    
    public TaskResponse(Task task) {
        this.taskId = task.getTaskId();
        this.tenantId = task.getTenantId();
        this.title = task.getTitle();
        this.description = task.getDescription();
        this.status = task.getStatus();
        this.priority = task.getPriority();
        this.type = task.getType();
        this.assignedToUserId = task.getAssignedToUserId();
        this.createdByUserId = task.getCreatedByUserId();
        this.dueDate = task.getDueDate();
        this.completedAt = task.getCompletedAt();
        this.relatedContactId = task.getRelatedContactId();
        this.relatedAccountId = task.getRelatedAccountId();
        this.relatedDealId = task.getRelatedDealId();
        this.relatedLeadId = task.getRelatedLeadId();
        this.relatedEntityType = task.getRelatedEntityType();
        this.createdAt = task.getCreatedAt();
        this.updatedAt = task.getUpdatedAt();
        
        // Calculate computed fields
        this.overdue = task.isOverdue();
        this.dueToday = task.isDueToday();
        this.dueSoon = task.isDueSoon();
        this.daysUntilDue = calculateDaysUntilDue(task.getDueDate());
    }
    
    private long calculateDaysUntilDue(LocalDateTime dueDate) {
        if (dueDate == null) return -1;
        LocalDateTime now = LocalDateTime.now();
        return java.time.Duration.between(now, dueDate).toDays();
    }
}
