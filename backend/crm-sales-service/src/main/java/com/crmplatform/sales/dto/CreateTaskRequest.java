package com.crmplatform.sales.dto;

import com.crmplatform.sales.entity.TaskPriority;
import com.crmplatform.sales.entity.TaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateTaskRequest {
    
    @NotBlank(message = "Task title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;
    
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;
    
    @NotNull(message = "Priority is required")
    private TaskPriority priority;
    
    @NotNull(message = "Task type is required")
    private TaskType type;
    
    @NotNull(message = "Assigned user ID is required")
    private Long assignedToUserId;
    
    private LocalDateTime dueDate;
    
    // Related entity fields
    private Long relatedContactId;
    private Long relatedAccountId;
    private Long relatedDealId;
    private Long relatedLeadId;
    private String relatedEntityType;
}
