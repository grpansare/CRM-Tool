package com.crmplatform.sales.repository;

import com.crmplatform.sales.entity.DealStageHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DealStageHistoryRepository extends JpaRepository<DealStageHistory, Long> {
    
    @Query("SELECT dsh FROM DealStageHistory dsh WHERE dsh.tenantId = :tenantId AND dsh.dealId = :dealId ORDER BY dsh.changedAt DESC")
    List<DealStageHistory> findByTenantIdAndDealIdOrderByChangedAtDesc(@Param("tenantId") Long tenantId, 
                                                                     @Param("dealId") Long dealId);
    
    @Query("SELECT dsh FROM DealStageHistory dsh WHERE dsh.tenantId = :tenantId AND dsh.dealId = :dealId ORDER BY dsh.changedAt DESC LIMIT 1")
    Optional<DealStageHistory> findLatestByTenantIdAndDealId(@Param("tenantId") Long tenantId, 
                                                            @Param("dealId") Long dealId);
    
    @Query("SELECT dsh FROM DealStageHistory dsh WHERE dsh.tenantId = :tenantId AND dsh.toStageId = :stageId ORDER BY dsh.changedAt DESC")
    List<DealStageHistory> findByTenantIdAndToStageIdOrderByChangedAtDesc(@Param("tenantId") Long tenantId, 
                                                                        @Param("stageId") Long stageId);
    
    // Method needed by DealService for deleting stage history
    @Modifying
    @Query("DELETE FROM DealStageHistory dsh WHERE dsh.tenantId = :tenantId AND dsh.dealId = :dealId")
    void deleteByTenantIdAndDealId(@Param("tenantId") Long tenantId, @Param("dealId") Long dealId);
}