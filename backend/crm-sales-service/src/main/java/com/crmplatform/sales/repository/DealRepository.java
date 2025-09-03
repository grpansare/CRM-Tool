package com.crmplatform.sales.repository;

import com.crmplatform.sales.entity.Deal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface DealRepository extends JpaRepository<Deal, Long> {
    
    @Query("SELECT d FROM Deal d WHERE d.tenantId = :tenantId AND d.ownerUserId = :ownerUserId")
    List<Deal> findByTenantIdAndOwnerUserId(@Param("tenantId") Long tenantId, 
                                           @Param("ownerUserId") Long ownerUserId);
    
    @Query("SELECT d FROM Deal d WHERE d.tenantId = :tenantId AND d.stageId = :stageId")
    List<Deal> findByTenantIdAndStageId(@Param("tenantId") Long tenantId, 
                                       @Param("stageId") Long stageId);
    
    @Query("SELECT d FROM Deal d WHERE d.tenantId = :tenantId AND d.accountId = :accountId")
    List<Deal> findByTenantIdAndAccountId(@Param("tenantId") Long tenantId, 
                                         @Param("accountId") Long accountId);
    
    @Query("SELECT d FROM Deal d WHERE d.tenantId = :tenantId AND d.contactId = :contactId")
    List<Deal> findByTenantIdAndContactId(@Param("tenantId") Long tenantId, 
                                         @Param("contactId") Long contactId);
    
    @Query("SELECT SUM(d.amount) FROM Deal d WHERE d.tenantId = :tenantId AND d.stageId = :stageId")
    BigDecimal getTotalAmountByTenantIdAndStageId(@Param("tenantId") Long tenantId, 
                                                 @Param("stageId") Long stageId);
    
    @Query("SELECT COUNT(d) FROM Deal d WHERE d.tenantId = :tenantId AND d.stageId = :stageId")
    Long countByTenantIdAndStageId(@Param("tenantId") Long tenantId, 
                                  @Param("stageId") Long stageId);
    
    @Query("SELECT d FROM Deal d WHERE d.tenantId = :tenantId AND " +
           "LOWER(d.dealName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Deal> searchDeals(@Param("tenantId") Long tenantId, 
                           @Param("searchTerm") String searchTerm);
    
    Optional<Deal> findByTenantIdAndDealId(Long tenantId, Long dealId);
    
    // Method needed by SalesPipelineService
    @Query("SELECT COUNT(d) FROM Deal d WHERE d.stageId = :stageId AND d.tenantId = :tenantId")
    long countByStageIdAndTenantId(@Param("stageId") Long stageId, 
                                  @Param("tenantId") Long tenantId);
    
    // Method needed by SalesPipelineService  
    @Query("SELECT d FROM Deal d WHERE d.stageId = :stageId AND d.tenantId = :tenantId")
    List<Deal> findByStageIdAndTenantId(@Param("stageId") Long stageId, 
                                       @Param("tenantId") Long tenantId);
    
    // Method needed by SalesManagerService
    @Query("SELECT d FROM Deal d WHERE d.tenantId = :tenantId")
    List<Deal> findByTenantId(@Param("tenantId") Long tenantId);
}