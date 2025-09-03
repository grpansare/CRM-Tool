package com.crmplatform.sales.repository;

import com.crmplatform.sales.entity.SalesPipeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalesPipelineRepository extends JpaRepository<SalesPipeline, Long> {
    
    List<SalesPipeline> findByTenantIdOrderByPipelineName(Long tenantId);
    
    Optional<SalesPipeline> findByPipelineIdAndTenantId(Long pipelineId, Long tenantId);
    
    @Query("SELECT COUNT(p) FROM SalesPipeline p WHERE p.tenantId = :tenantId")
    long countByTenantId(@Param("tenantId") Long tenantId);
    
    boolean existsByPipelineNameAndTenantId(String pipelineName, Long tenantId);
}
