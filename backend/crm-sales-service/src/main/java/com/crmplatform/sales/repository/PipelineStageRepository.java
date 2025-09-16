package com.crmplatform.sales.repository;

import com.crmplatform.sales.entity.PipelineStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PipelineStageRepository extends JpaRepository<PipelineStage, Long> {
    
    @Query("SELECT ps FROM PipelineStage ps WHERE ps.tenantId = :tenantId AND ps.pipelineId = :pipelineId ORDER BY ps.stageOrder")
    List<PipelineStage> findByTenantIdAndPipelineIdOrderByStageOrder(@Param("tenantId") Long tenantId, 
                                                                     @Param("pipelineId") Long pipelineId);
    
    @Query("SELECT ps FROM PipelineStage ps WHERE ps.tenantId = :tenantId AND ps.stageId = :stageId")
    Optional<PipelineStage> findByTenantIdAndStageId(@Param("tenantId") Long tenantId, 
                                                    @Param("stageId") Long stageId);
    
    @Query("SELECT ps FROM PipelineStage ps WHERE ps.tenantId = :tenantId AND ps.stageType = :stageType")
    List<PipelineStage> findByTenantIdAndStageType(@Param("tenantId") Long tenantId, 
                                                  @Param("stageType") PipelineStage.StageType stageType);
    
    @Query("SELECT MAX(ps.stageOrder) FROM PipelineStage ps WHERE ps.tenantId = :tenantId AND ps.pipelineId = :pipelineId")
    Integer findMaxStageOrderByTenantIdAndPipelineId(@Param("tenantId") Long tenantId, 
                                                    @Param("pipelineId") Long pipelineId);
    
    // Method needed by SalesPipelineService
    @Query("SELECT ps FROM PipelineStage ps WHERE ps.pipelineId = :pipelineId AND ps.tenantId = :tenantId ORDER BY ps.stageOrder")
    List<PipelineStage> findByPipelineIdAndTenantIdOrderByStageOrder(@Param("pipelineId") Long pipelineId, 
                                                                     @Param("tenantId") Long tenantId);
    
    // Method needed by SalesPipelineService
    @Query("SELECT ps FROM PipelineStage ps WHERE ps.stageId = :stageId AND ps.tenantId = :tenantId")
    Optional<PipelineStage> findByStageIdAndTenantId(@Param("stageId") Long stageId, 
                                                    @Param("tenantId") Long tenantId);
    
    // Method needed by SalesManagerService
    @Query("SELECT ps FROM PipelineStage ps WHERE ps.tenantId = :tenantId")
    List<PipelineStage> findByTenantId(@Param("tenantId") Long tenantId);
    
    // Method needed by DealService for getting all stages ordered
    @Query("SELECT ps FROM PipelineStage ps WHERE ps.tenantId = :tenantId ORDER BY ps.stageOrder")
    List<PipelineStage> findByTenantIdOrderByStageOrder(@Param("tenantId") Long tenantId);
    
    // Method needed by LeadConversionService for getting first stage
    @Query("SELECT ps FROM PipelineStage ps WHERE ps.tenantId = :tenantId ORDER BY ps.stageOrder LIMIT 1")
    Optional<PipelineStage> findFirstByTenantIdOrderByStageOrder(@Param("tenantId") Long tenantId);
} 