package com.crmplatform.sales.service;

import com.crmplatform.sales.dto.*;
import com.crmplatform.sales.entity.Deal;
import com.crmplatform.sales.entity.PipelineStage;
import com.crmplatform.sales.entity.SalesPipeline;
import com.crmplatform.sales.repository.DealRepository;
import com.crmplatform.sales.repository.PipelineStageRepository;
import com.crmplatform.sales.repository.SalesPipelineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SalesPipelineService {
    
    private final SalesPipelineRepository pipelineRepository;
    private final PipelineStageRepository stageRepository;
    private final DealRepository dealRepository;
    
    public List<SalesPipelineResponse> getAllPipelines(Long tenantId) {
        List<SalesPipeline> pipelines = pipelineRepository.findByTenantIdOrderByPipelineName(tenantId);
        return pipelines.stream()
                .map(this::buildPipelineResponse)
                .collect(Collectors.toList());
    }
    
    public SalesPipelineResponse getPipelineById(Long pipelineId, Long tenantId) {
        SalesPipeline pipeline = pipelineRepository.findByPipelineIdAndTenantId(pipelineId, tenantId)
                .orElseThrow(() -> new RuntimeException("Pipeline not found"));
        return buildPipelineResponse(pipeline);
    }
    
    public SalesPipelineResponse createPipeline(SalesPipelineRequest request, Long tenantId) {
        log.debug("Creating pipeline with tenantId: {} and name: {}", tenantId, request.getPipelineName());
        
        if (tenantId == null) {
            log.error("TenantId is null - cannot create pipeline");
            throw new RuntimeException("Tenant ID is required");
        }
        
        if (pipelineRepository.existsByPipelineNameAndTenantId(request.getPipelineName(), tenantId)) {
            throw new RuntimeException("Pipeline with this name already exists");
        }
        
        SalesPipeline pipeline = SalesPipeline.builder()
                .pipelineName(request.getPipelineName())
                .tenantId(tenantId)
                .build();
        
        log.debug("Saving pipeline: {}", pipeline);
        pipeline = pipelineRepository.save(pipeline);
        
        // Create default stages
        createDefaultStages(pipeline.getPipelineId(), tenantId);
        
        return buildPipelineResponse(pipeline);
    }
    
    public SalesPipelineResponse updatePipeline(Long pipelineId, SalesPipelineRequest request, Long tenantId) {
        SalesPipeline pipeline = pipelineRepository.findByPipelineIdAndTenantId(pipelineId, tenantId)
                .orElseThrow(() -> new RuntimeException("Pipeline not found"));
        
        if (!pipeline.getPipelineName().equals(request.getPipelineName()) &&
            pipelineRepository.existsByPipelineNameAndTenantId(request.getPipelineName(), tenantId)) {
            throw new RuntimeException("Pipeline with this name already exists");
        }
        
        pipeline.setPipelineName(request.getPipelineName());
        pipeline = pipelineRepository.save(pipeline);
        
        return buildPipelineResponse(pipeline);
    }
    
    public void deletePipeline(Long pipelineId, Long tenantId) {
        SalesPipeline pipeline = pipelineRepository.findByPipelineIdAndTenantId(pipelineId, tenantId)
                .orElseThrow(() -> new RuntimeException("Pipeline not found"));
        
        // Check if pipeline has deals
        List<PipelineStage> stages = stageRepository.findByPipelineIdAndTenantIdOrderByStageOrder(pipelineId, tenantId);
        for (PipelineStage stage : stages) {
            long dealCount = dealRepository.countByStageIdAndTenantId(stage.getStageId(), tenantId);
            if (dealCount > 0) {
                throw new RuntimeException("Cannot delete pipeline with existing deals");
            }
        }
        
        // Delete stages first
        stageRepository.deleteAll(stages);
        pipelineRepository.delete(pipeline);
    }
    
    public PipelineStageResponse createStage(Long pipelineId, PipelineStageRequest request, Long tenantId) {
        // Verify pipeline exists
        pipelineRepository.findByPipelineIdAndTenantId(pipelineId, tenantId)
                .orElseThrow(() -> new RuntimeException("Pipeline not found"));
        
        PipelineStage stage = PipelineStage.builder()
                .pipelineId(pipelineId)
                .tenantId(tenantId)
                .stageName(request.getStageName())
                .stageOrder(request.getStageOrder())
                .stageType(PipelineStage.StageType.valueOf(request.getStageType()))
                .winProbability(request.getWinProbability())
                .build();
        
        stage = stageRepository.save(stage);
        return buildStageResponse(stage);
    }
    
    public PipelineStageResponse updateStage(Long stageId, PipelineStageRequest request, Long tenantId) {
        PipelineStage stage = stageRepository.findByStageIdAndTenantId(stageId, tenantId)
                .orElseThrow(() -> new RuntimeException("Stage not found"));
        
        stage.setStageName(request.getStageName());
        stage.setStageOrder(request.getStageOrder());
        stage.setStageType(PipelineStage.StageType.valueOf(request.getStageType()));
        stage.setWinProbability(request.getWinProbability());
        
        stage = stageRepository.save(stage);
        return buildStageResponse(stage);
    }
    
    public void deleteStage(Long stageId, Long tenantId) {
        PipelineStage stage = stageRepository.findByStageIdAndTenantId(stageId, tenantId)
                .orElseThrow(() -> new RuntimeException("Stage not found"));
        
        // Check if stage has deals
        long dealCount = dealRepository.countByStageIdAndTenantId(stageId, tenantId);
        if (dealCount > 0) {
            throw new RuntimeException("Cannot delete stage with existing deals");
        }
        
        stageRepository.delete(stage);
    }
    
    private void createDefaultStages(Long pipelineId, Long tenantId) {
        List<PipelineStage> defaultStages = List.of(
            PipelineStage.builder()
                .pipelineId(pipelineId)
                .tenantId(tenantId)
                .stageName("Prospecting")
                .stageOrder(1)
                .stageType(PipelineStage.StageType.OPEN)
                .winProbability(new BigDecimal("10"))
                .build(),
            PipelineStage.builder()
                .pipelineId(pipelineId)
                .tenantId(tenantId)
                .stageName("Qualification")
                .stageOrder(2)
                .stageType(PipelineStage.StageType.OPEN)
                .winProbability(new BigDecimal("25"))
                .build(),
            PipelineStage.builder()
                .pipelineId(pipelineId)
                .tenantId(tenantId)
                .stageName("Proposal")
                .stageOrder(3)
                .stageType(PipelineStage.StageType.OPEN)
                .winProbability(new BigDecimal("50"))
                .build(),
            PipelineStage.builder()
                .pipelineId(pipelineId)
                .tenantId(tenantId)
                .stageName("Negotiation")
                .stageOrder(4)
                .stageType(PipelineStage.StageType.OPEN)
                .winProbability(new BigDecimal("75"))
                .build(),
            PipelineStage.builder()
                .pipelineId(pipelineId)
                .tenantId(tenantId)
                .stageName("Closed Won")
                .stageOrder(5)
                .stageType(PipelineStage.StageType.WON)
                .winProbability(new BigDecimal("100"))
                .build(),
            PipelineStage.builder()
                .pipelineId(pipelineId)
                .tenantId(tenantId)
                .stageName("Closed Lost")
                .stageOrder(6)
                .stageType(PipelineStage.StageType.LOST)
                .winProbability(new BigDecimal("0"))
                .build()
        );
        
        stageRepository.saveAll(defaultStages);
    }
    
    private SalesPipelineResponse buildPipelineResponse(SalesPipeline pipeline) {
        List<PipelineStage> stages = stageRepository.findByPipelineIdAndTenantIdOrderByStageOrder(
                pipeline.getPipelineId(), pipeline.getTenantId());
        
        List<PipelineStageResponse> stageResponses = stages.stream()
                .map(this::buildStageResponse)
                .collect(Collectors.toList());
        
        // Calculate totals
        int totalDeals = stageResponses.stream()
                .mapToInt(PipelineStageResponse::getDealCount)
                .sum();
        
        long totalValue = stageResponses.stream()
                .mapToLong(stage -> stage.getTotalValue() != null ? stage.getTotalValue().longValue() : 0L)
                .sum();
        
        return SalesPipelineResponse.builder()
                .pipelineId(pipeline.getPipelineId())
                .pipelineName(pipeline.getPipelineName())
                .stages(stageResponses)
                .totalDeals(totalDeals)
                .totalValue(totalValue)
                .build();
    }
    
    private PipelineStageResponse buildStageResponse(PipelineStage stage) {
        List<Deal> deals = dealRepository.findByStageIdAndTenantId(stage.getStageId(), stage.getTenantId());
        
        BigDecimal totalValue = deals.stream()
                .map(Deal::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Convert deals to deal responses for frontend
        List<DealResponse> dealResponses = deals.stream()
                .map(this::buildDealResponse)
                .collect(Collectors.toList());
        
        return PipelineStageResponse.builder()
                .stageId(stage.getStageId())
                .stageName(stage.getStageName())
                .stageOrder(stage.getStageOrder())
                .stageType(stage.getStageType().name())
                .winProbability(stage.getWinProbability())
                .dealCount(deals.size())
                .totalValue(totalValue)
                .deals(dealResponses)
                .build();
    }
    
    private DealResponse buildDealResponse(Deal deal) {
        return DealResponse.builder()
                .dealId(deal.getDealId())
                .dealName(deal.getDealName())
                .amount(deal.getAmount())
                .expectedCloseDate(deal.getExpectedCloseDate())
                .ownerUserId(deal.getOwnerUserId())
                .build();
    }
}
