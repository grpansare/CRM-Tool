package com.crmplatform.sales.repository;

import com.crmplatform.sales.entity.Task;
import com.crmplatform.sales.entity.TaskStatus;
import com.crmplatform.sales.entity.TaskPriority;
import com.crmplatform.sales.entity.TaskType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    // Basic queries
    Page<Task> findByTenantId(Long tenantId, Pageable pageable);
    
    Optional<Task> findByTaskIdAndTenantId(Long taskId, Long tenantId);
    
    // User-specific queries
    Page<Task> findByTenantIdAndAssignedToUserId(Long tenantId, Long assignedToUserId, Pageable pageable);
    
    Page<Task> findByTenantIdAndCreatedByUserId(Long tenantId, Long createdByUserId, Pageable pageable);
    
    // Status-based queries
    Page<Task> findByTenantIdAndStatus(Long tenantId, TaskStatus status, Pageable pageable);
    
    Page<Task> findByTenantIdAndAssignedToUserIdAndStatus(Long tenantId, Long assignedToUserId, TaskStatus status, Pageable pageable);
    
    // Priority-based queries
    Page<Task> findByTenantIdAndPriority(Long tenantId, TaskPriority priority, Pageable pageable);
    
    Page<Task> findByTenantIdAndAssignedToUserIdAndPriority(Long tenantId, Long assignedToUserId, TaskPriority priority, Pageable pageable);
    
    // Type-based queries
    Page<Task> findByTenantIdAndType(Long tenantId, TaskType type, Pageable pageable);
    
    // Due date queries
    @Query("SELECT t FROM Task t WHERE t.tenantId = :tenantId AND t.dueDate IS NOT NULL AND DATE(t.dueDate) = DATE(:date) AND t.status != 'COMPLETED'")
    List<Task> findTasksDueOnDate(@Param("tenantId") Long tenantId, @Param("date") LocalDateTime date);
    
    @Query("SELECT t FROM Task t WHERE t.tenantId = :tenantId AND t.assignedToUserId = :userId AND t.dueDate IS NOT NULL AND DATE(t.dueDate) = DATE(:date) AND t.status != 'COMPLETED'")
    List<Task> findUserTasksDueOnDate(@Param("tenantId") Long tenantId, @Param("userId") Long userId, @Param("date") LocalDateTime date);
    
    @Query("SELECT t FROM Task t WHERE t.tenantId = :tenantId AND t.dueDate IS NOT NULL AND t.dueDate < :now AND t.status != 'COMPLETED'")
    List<Task> findOverdueTasks(@Param("tenantId") Long tenantId, @Param("now") LocalDateTime now);
    
    @Query("SELECT t FROM Task t WHERE t.tenantId = :tenantId AND t.assignedToUserId = :userId AND t.dueDate IS NOT NULL AND t.dueDate < :now AND t.status != 'COMPLETED'")
    List<Task> findUserOverdueTasks(@Param("tenantId") Long tenantId, @Param("userId") Long userId, @Param("now") LocalDateTime now);
    
    @Query("SELECT t FROM Task t WHERE t.tenantId = :tenantId AND t.dueDate IS NOT NULL AND t.dueDate BETWEEN :startDate AND :endDate AND t.status != 'COMPLETED'")
    List<Task> findTasksDueBetween(@Param("tenantId") Long tenantId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Related entity queries
    List<Task> findByTenantIdAndRelatedContactId(Long tenantId, Long relatedContactId);
    
    List<Task> findByTenantIdAndRelatedAccountId(Long tenantId, Long relatedAccountId);
    
    List<Task> findByTenantIdAndRelatedDealId(Long tenantId, Long relatedDealId);
    
    List<Task> findByTenantIdAndRelatedLeadId(Long tenantId, Long relatedLeadId);
    
    // Search queries
    @Query("SELECT t FROM Task t WHERE t.tenantId = :tenantId AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Task> searchTasks(@Param("tenantId") Long tenantId, @Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT t FROM Task t WHERE t.tenantId = :tenantId AND t.assignedToUserId = :userId AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Task> searchUserTasks(@Param("tenantId") Long tenantId, @Param("userId") Long userId, @Param("searchTerm") String searchTerm, Pageable pageable);
    
    // Analytics queries
    @Query("SELECT COUNT(t) FROM Task t WHERE t.tenantId = :tenantId AND t.status = :status")
    long countByTenantIdAndStatus(@Param("tenantId") Long tenantId, @Param("status") TaskStatus status);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.tenantId = :tenantId AND t.assignedToUserId = :userId AND t.status = :status")
    long countByTenantIdAndAssignedToUserIdAndStatus(@Param("tenantId") Long tenantId, @Param("userId") Long userId, @Param("status") TaskStatus status);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.tenantId = :tenantId AND t.priority = :priority AND t.status != 'COMPLETED'")
    long countByTenantIdAndPriorityAndNotCompleted(@Param("tenantId") Long tenantId, @Param("priority") TaskPriority priority);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.tenantId = :tenantId AND t.dueDate IS NOT NULL AND t.dueDate < :now AND t.status != 'COMPLETED'")
    long countOverdueTasks(@Param("tenantId") Long tenantId, @Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.tenantId = :tenantId AND t.assignedToUserId = :userId AND t.dueDate IS NOT NULL AND t.dueDate < :now AND t.status != 'COMPLETED'")
    long countUserOverdueTasks(@Param("tenantId") Long tenantId, @Param("userId") Long userId, @Param("now") LocalDateTime now);
}
