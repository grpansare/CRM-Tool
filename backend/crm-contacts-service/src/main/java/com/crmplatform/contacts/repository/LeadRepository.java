package com.crmplatform.contacts.repository;

import com.crmplatform.contacts.entity.Lead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {
    
    Page<Lead> findByTenantId(Long tenantId, Pageable pageable);
    
    Optional<Lead> findByLeadIdAndTenantId(Long leadId, Long tenantId);
    
    List<Lead> findByTenantIdAndOwnerUserId(Long tenantId, Long ownerUserId);
    
    @Query("SELECT l FROM Lead l WHERE l.tenantId = :tenantId AND " +
           "(LOWER(l.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(l.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(l.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(l.company) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Lead> findByTenantIdAndSearchTerm(@Param("tenantId") Long tenantId, 
                                          @Param("searchTerm") String searchTerm, 
                                          Pageable pageable);
    
    List<Lead> findByTenantIdAndLeadStatus(Long tenantId, Lead.LeadStatus leadStatus);
    
    List<Lead> findByTenantIdAndLeadSource(Long tenantId, Lead.LeadSource leadSource);
    
    @Query("SELECT COUNT(l) FROM Lead l WHERE l.tenantId = :tenantId AND l.leadStatus = :status")
    Long countByTenantIdAndLeadStatus(@Param("tenantId") Long tenantId, @Param("status") Lead.LeadStatus status);
    
    @Query("SELECT l FROM Lead l WHERE l.tenantId = :tenantId AND l.leadScore >= :minScore ORDER BY l.leadScore DESC")
    List<Lead> findHighScoringLeads(@Param("tenantId") Long tenantId, @Param("minScore") Integer minScore);
    
    boolean existsByEmailAndTenantId(String email, Long tenantId);
}
