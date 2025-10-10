package com.crmplatform.sales.repository;

import com.crmplatform.sales.entity.LeadAssignmentRule;
import com.crmplatform.sales.entity.AssignmentStrategy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeadAssignmentRuleRepository extends JpaRepository<LeadAssignmentRule, Long> {
    
    // Find active rules by tenant ordered by priority
    List<LeadAssignmentRule> findByTenantIdAndIsActiveOrderByPriorityOrderAsc(Long tenantId, Boolean isActive);
    
    // Find all rules by tenant
    Page<LeadAssignmentRule> findByTenantIdOrderByPriorityOrderAsc(Long tenantId, Pageable pageable);
    
    // Find rules by tenant and active status
    List<LeadAssignmentRule> findByTenantIdAndIsActive(Long tenantId, Boolean isActive);
    
    // Find rules by assignment strategy
    List<LeadAssignmentRule> findByTenantIdAndAssignmentStrategyAndIsActive(
        Long tenantId, AssignmentStrategy strategy, Boolean isActive);
    
    // Find rule by name
    Optional<LeadAssignmentRule> findByTenantIdAndRuleName(Long tenantId, String ruleName);
    
    // Check if rule name exists
    boolean existsByTenantIdAndRuleName(Long tenantId, String ruleName);
    
    // Count active rules
    long countByTenantIdAndIsActive(Long tenantId, Boolean isActive);
    
    // Get rules with highest priority
    @Query("SELECT r FROM LeadAssignmentRule r WHERE r.tenantId = :tenantId " +
           "AND r.isActive = true " +
           "ORDER BY r.priorityOrder ASC")
    List<LeadAssignmentRule> findActiveRulesByPriority(@Param("tenantId") Long tenantId);
    
    // Find rules by created user
    List<LeadAssignmentRule> findByTenantIdAndCreatedByOrderByCreatedAtDesc(Long tenantId, Long createdBy);
}
