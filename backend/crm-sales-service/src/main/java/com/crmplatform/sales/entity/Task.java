package com.crmplatform.sales.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taskId;
    
    @Column(nullable = false)
    private Long tenantId;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.PENDING;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPriority priority = TaskPriority.MEDIUM;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskType type = TaskType.GENERAL;
    
    @Column(nullable = false)
    private Long assignedToUserId;
    
    @Column(nullable = false)
    private Long createdByUserId;
    
    private LocalDateTime dueDate;
    
    private LocalDateTime completedAt;
    
    // Related entity associations
    private Long relatedContactId;
    private Long relatedAccountId;
    private Long relatedDealId;
    private Long relatedLeadId;
    
    @Column(length = 50)
    private String relatedEntityType; // CONTACT, ACCOUNT, DEAL, LEAD
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // Helper methods
    public boolean isOverdue() {
        return dueDate != null && 
               LocalDateTime.now().isAfter(dueDate) && 
               status != TaskStatus.COMPLETED;
    }
    
    public boolean isDueToday() {
        if (dueDate == null) return false;
        LocalDateTime now = LocalDateTime.now();
        return dueDate.toLocalDate().equals(now.toLocalDate()) && 
               status != TaskStatus.COMPLETED;
    }
    
    public boolean isDueSoon() {
        if (dueDate == null) return false;
        LocalDateTime now = LocalDateTime.now();
        return dueDate.isAfter(now) && 
               dueDate.isBefore(now.plusDays(3)) && 
               status != TaskStatus.COMPLETED;
    }
}
