package com.crmplatform.sales.repository;

import com.crmplatform.sales.entity.Lead;
import com.crmplatform.sales.entity.LeadStatus;
import com.crmplatform.sales.entity.LeadSource;
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
public interface LeadRepository extends JpaRepository<Lead, Long> {
    
    // Find all leads by tenant ID
    Page<Lead> findByTenantId(Long tenantId, Pageable pageable);
    
    // Find lead by ID and tenant ID
    Optional<Lead> findByLeadIdAndTenantId(Long leadId, Long tenantId);
    
    // Find leads by owner user ID and tenant ID
    Page<Lead> findByOwnerUserIdAndTenantId(Long ownerUserId, Long tenantId, Pageable pageable);
    
    // Find leads by status and tenant ID
    Page<Lead> findByLeadStatusAndTenantId(LeadStatus leadStatus, Long tenantId, Pageable pageable);
    
    // Find leads by source and tenant ID
    Page<Lead> findByLeadSourceAndTenantId(LeadSource leadSource, Long tenantId, Pageable pageable);
    
    // Search leads by name, email, or company
    @Query("SELECT l FROM Lead l WHERE l.tenantId = :tenantId AND " +
           "(LOWER(CONCAT(COALESCE(l.firstName, ''), ' ', l.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(l.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(l.company) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Lead> searchLeads(@Param("tenantId") Long tenantId, @Param("searchTerm") String searchTerm, Pageable pageable);
    
    // Find leads by tenant ID and status with pagination
    Page<Lead> findByTenantIdAndLeadStatus(Long tenantId, LeadStatus leadStatus, Pageable pageable);
    
    // Count leads by status for dashboard
    long countByTenantIdAndLeadStatus(Long tenantId, LeadStatus leadStatus);
    
    // Count total leads for tenant
    long countByTenantId(Long tenantId);
    
    // Find recent leads (created in last N days)
    @Query("SELECT l FROM Lead l WHERE l.tenantId = :tenantId AND l.createdAt >= :startDate ORDER BY l.createdAt DESC")
    List<Lead> findRecentLeads(@Param("tenantId") Long tenantId, @Param("startDate") LocalDateTime startDate);
    
    // Check if email exists for tenant (for duplicate prevention)
    boolean existsByEmailAndTenantId(String email, Long tenantId);
}
