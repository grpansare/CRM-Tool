package com.crmplatform.sales.dto;

import com.crmplatform.sales.entity.TaskPriority;
import com.crmplatform.sales.entity.TaskStatus;
import com.crmplatform.sales.entity.TaskType;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateTaskRequest {
    
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;
    
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;
    
    private TaskStatus status;
    
    private TaskPriority priority;
    
    private TaskType type;
    
    private Long assignedToUserId;
    
    private LocalDateTime dueDate;
    
    // Related entity fields
    private Long relatedContactId;
    private Long relatedAccountId;
    private Long relatedDealId;
    private Long relatedLeadId;
    private String relatedEntityType;
}
