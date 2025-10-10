package com.crmplatform.sales.repository;

import com.crmplatform.sales.entity.LeadAssignmentHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LeadAssignmentHistoryRepository extends JpaRepository<LeadAssignmentHistory, Long> {
    
    // Find assignment history by lead
    List<LeadAssignmentHistory> findByTenantIdAndLeadIdOrderByAssignedAtDesc(Long tenantId, Long leadId);
    
    // Find assignments by user
    Page<LeadAssignmentHistory> findByTenantIdAndAssignedToUserIdOrderByAssignedAtDesc(
        Long tenantId, Long assignedToUserId, Pageable pageable);
    
    // Find assignments by rule
    List<LeadAssignmentHistory> findByTenantIdAndRuleIdOrderByAssignedAtDesc(Long tenantId, Long ruleId);
    
    // Find recent assignments
    List<LeadAssignmentHistory> findByTenantIdAndAssignedAtAfterOrderByAssignedAtDesc(
        Long tenantId, LocalDateTime after);
    
    // Count assignments by user in date range
    @Query("SELECT COUNT(h) FROM LeadAssignmentHistory h WHERE h.tenantId = :tenantId " +
           "AND h.assignedToUserId = :userId AND h.assignedAt BETWEEN :startDate AND :endDate")
    long countAssignmentsByUserInDateRange(@Param("tenantId") Long tenantId, 
                                         @Param("userId") Long userId,
                                         @Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);
    
    // Find assignments by method
    List<LeadAssignmentHistory> findByTenantIdAndAssignmentMethodOrderByAssignedAtDesc(
        Long tenantId, String assignmentMethod);
}
