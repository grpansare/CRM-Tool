package com.crmplatform.sales.service;

import com.crmplatform.common.dto.ApiResponse;
import com.crmplatform.common.security.UserContext;
import com.crmplatform.sales.dto.CreateDealRequest;
import com.crmplatform.sales.dto.DealResponse;
import com.crmplatform.sales.dto.UpdateDealStageRequest;
import com.crmplatform.sales.entity.Deal;
import com.crmplatform.sales.entity.DealStageHistory;
import com.crmplatform.sales.entity.PipelineStage;
import com.crmplatform.sales.repository.DealRepository;
import com.crmplatform.sales.repository.DealStageHistoryRepository;
import com.crmplatform.sales.repository.PipelineStageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DealService {
    
    private final DealRepository dealRepository;
    private final PipelineStageRepository pipelineStageRepository;
    private final DealStageHistoryRepository dealStageHistoryRepository;
    private final ContactIntegrationService contactIntegrationService;
    private final EventPublisher eventPublisher;
    
    @Transactional
    public ApiResponse<DealResponse> createDeal(CreateDealRequest request) {
        Long tenantId = UserContext.getCurrentTenantId();
        Long ownerUserId = UserContext.getCurrentUserId();
        
        // Validate stage exists
        Optional<PipelineStage> stage = pipelineStageRepository
                .findByTenantIdAndStageId(tenantId, request.getStageId());
        
        if (stage.isEmpty()) {
            return ApiResponse.error("Invalid stage ID", "INVALID_STAGE");
        }
        
        // Validate contact exists in contacts service
        if (!contactIntegrationService.validateContact(request.getContactId())) {
            return ApiResponse.error("Contact not found", "CONTACT_NOT_FOUND");
        }
        
        // Validate account exists in contacts service
        if (!contactIntegrationService.validateAccount(request.getAccountId())) {
            return ApiResponse.error("Account not found", "ACCOUNT_NOT_FOUND");
        }
        
        // Create deal
        Deal deal = Deal.builder()
                .tenantId(tenantId)
                .dealName(request.getDealName())
                .amount(request.getAmount())
                .expectedCloseDate(request.getExpectedCloseDate())
                .stageId(request.getStageId())
                .contactId(request.getContactId())
                .accountId(request.getAccountId())
                .ownerUserId(ownerUserId)
                .build();
        
        deal = dealRepository.save(deal);
        
        // Create initial stage history entry
        DealStageHistory initialHistory = DealStageHistory.builder()
                .tenantId(tenantId)
                .dealId(deal.getDealId())
                .toStageId(request.getStageId())
                .timeInPreviousStageDays(0)
                .build();
        
        dealStageHistoryRepository.save(initialHistory);
        
        // Publish event (DISABLED - RabbitMQ not required)
        // eventPublisher.publishDealCreated(deal);
        
        // Build response with contact and account details
        DealResponse response = buildDealResponseWithDetails(deal, stage.get());
        
        return ApiResponse.success(response, "Deal created successfully");
    }
    
    @Transactional
    public ApiResponse<DealResponse> updateDealStage(Long dealId, UpdateDealStageRequest request) {
        Long tenantId = UserContext.getCurrentTenantId();
        Long ownerUserId = UserContext.getCurrentUserId();
        
        // Get the deal
        Optional<Deal> dealOpt = dealRepository.findByTenantIdAndDealId(tenantId, dealId);
        if (dealOpt.isEmpty()) {
            return ApiResponse.error("Deal not found", "DEAL_NOT_FOUND");
        }
        
        Deal deal = dealOpt.get();
        
        // Validate user permissions (deal owner or manager)
        if (!deal.getOwnerUserId().equals(ownerUserId)) {
            // TODO: Add manager role check
            return ApiResponse.error("Insufficient permissions", "INSUFFICIENT_PERMISSIONS");
        }
        
        // Validate new stage exists
        Optional<PipelineStage> newStage = pipelineStageRepository
                .findByTenantIdAndStageId(tenantId, request.getNewStageId());
        
        if (newStage.isEmpty()) {
            return ApiResponse.error("Invalid stage ID", "INVALID_STAGE");
        }
        
        Long oldStageId = deal.getStageId();
        
        // Calculate time in previous stage
        Optional<DealStageHistory> lastHistory = dealStageHistoryRepository
                .findLatestByTenantIdAndDealId(tenantId, dealId);
        
        int timeInPreviousStageDays = 0;
        if (lastHistory.isPresent()) {
            LocalDateTime lastChange = lastHistory.get().getChangedAt();
            timeInPreviousStageDays = (int) ChronoUnit.DAYS.between(lastChange, LocalDateTime.now());
        }
        
        // Create stage history entry
        DealStageHistory stageHistory = DealStageHistory.builder()
                .tenantId(tenantId)
                .dealId(dealId)
                .fromStageId(oldStageId)
                .toStageId(request.getNewStageId())
                .timeInPreviousStageDays(timeInPreviousStageDays)
                .build();
        
        dealStageHistoryRepository.save(stageHistory);
        
        // Update deal stage
        deal.setStageId(request.getNewStageId());
        deal = dealRepository.save(deal);
        
        // Publish event (DISABLED - RabbitMQ not required)
        // eventPublisher.publishDealStageChanged(deal, oldStageId, request.getNewStageId());
        
        // Build response with contact and account details
        DealResponse response = buildDealResponseWithDetails(deal, newStage.get());
        
        return ApiResponse.success(response, "Deal stage updated successfully");
    }
    
    public ApiResponse<DealResponse> getDeal(Long dealId) {
        Long tenantId = UserContext.getCurrentTenantId();
        
        Optional<Deal> dealOpt = dealRepository.findByTenantIdAndDealId(tenantId, dealId);
        if (dealOpt.isEmpty()) {
            return ApiResponse.error("Deal not found", "DEAL_NOT_FOUND");
        }
        
        Deal deal = dealOpt.get();
        Optional<PipelineStage> stage = pipelineStageRepository
                .findByTenantIdAndStageId(tenantId, deal.getStageId());
        
        DealResponse response = buildDealResponseWithDetails(deal, stage.orElse(null));
        return ApiResponse.success(response);
    }
    
    public ApiResponse<List<DealResponse>> getDealsByOwner() {
        Long tenantId = UserContext.getCurrentTenantId();
        Long ownerUserId = UserContext.getCurrentUserId();
        
        List<Deal> deals = dealRepository.findByTenantIdAndOwnerUserId(tenantId, ownerUserId);
        List<DealResponse> responses = deals.stream()
                .map(deal -> {
                    Optional<PipelineStage> stage = pipelineStageRepository
                            .findByTenantIdAndStageId(tenantId, deal.getStageId());
                    return buildDealResponseWithDetails(deal, stage.orElse(null));
                })
                .toList();
        
        return ApiResponse.success(responses);
    }
    
    public ApiResponse<List<DealResponse>> getDealsByContact(Long contactId) {
        Long tenantId = UserContext.getCurrentTenantId();
        
        // Validate contact exists
        if (!contactIntegrationService.validateContact(contactId)) {
            return ApiResponse.error("Contact not found", "CONTACT_NOT_FOUND");
        }
        
        List<Deal> deals = dealRepository.findByTenantIdAndContactId(tenantId, contactId);
        List<DealResponse> responses = deals.stream()
                .map(deal -> {
                    Optional<PipelineStage> stage = pipelineStageRepository
                            .findByTenantIdAndStageId(tenantId, deal.getStageId());
                    return buildDealResponseWithDetails(deal, stage.orElse(null));
                })
                .toList();
        
        return ApiResponse.success(responses);
    }
    
    public ApiResponse<List<DealResponse>> getDealsByAccount(Long accountId) {
        Long tenantId = UserContext.getCurrentTenantId();
        
        // Validate account exists
        if (!contactIntegrationService.validateAccount(accountId)) {
            return ApiResponse.error("Account not found", "ACCOUNT_NOT_FOUND");
        }
        
        List<Deal> deals = dealRepository.findByTenantIdAndAccountId(tenantId, accountId);
        List<DealResponse> responses = deals.stream()
                .map(deal -> {
                    Optional<PipelineStage> stage = pipelineStageRepository
                            .findByTenantIdAndStageId(tenantId, deal.getStageId());
                    return buildDealResponseWithDetails(deal, stage.orElse(null));
                })
                .toList();
        
        return ApiResponse.success(responses);
    }
    
    private DealResponse buildDealResponseWithDetails(Deal deal, PipelineStage stage) {
        DealResponse.PipelineStageResponse stageResponse = null;
        if (stage != null) {
            stageResponse = DealResponse.PipelineStageResponse.builder()
                    .stageId(stage.getStageId())
                    .stageName(stage.getStageName())
                    .stageOrder(stage.getStageOrder())
                    .stageType(stage.getStageType().name())
                    .winProbability(stage.getWinProbability())
                    .build();
        }
        
        // Get contact details
        DealResponse.ContactSummary contactSummary = null;
        Map<String, Object> contactDetails = contactIntegrationService.getContactDetails(deal.getContactId());
        if (contactDetails != null) {
            contactSummary = DealResponse.ContactSummary.builder()
                    .contactId(deal.getContactId())
                    .firstName((String) contactDetails.get("firstName"))
                    .lastName((String) contactDetails.get("lastName"))
                    .primaryEmail((String) contactDetails.get("primaryEmail"))
                    .jobTitle((String) contactDetails.get("jobTitle"))
                    .phoneNumber((String) contactDetails.get("phoneNumber"))
                    .build();
        }
        
        // Get account details
        DealResponse.AccountSummary accountSummary = null;
        Map<String, Object> accountDetails = contactIntegrationService.getAccountDetails(deal.getAccountId());
        if (accountDetails != null) {
            accountSummary = DealResponse.AccountSummary.builder()
                    .accountId(deal.getAccountId())
                    .accountName((String) accountDetails.get("accountName"))
                    .website((String) accountDetails.get("website"))
                    .industry((String) accountDetails.get("industry"))
                    .build();
        }
        
        return DealResponse.builder()
                .dealId(deal.getDealId())
                .dealName(deal.getDealName())
                .amount(deal.getAmount())
                .expectedCloseDate(deal.getExpectedCloseDate())
                .stageId(deal.getStageId())
                .stageName(stage != null ? stage.getStageName() : null)
                .stageType(stage != null ? stage.getStageType().name() : null)
                .winProbability(stage != null ? stage.getWinProbability() : null)
                .contactId(deal.getContactId())
                .accountId(deal.getAccountId())
                .ownerUserId(deal.getOwnerUserId())
                .createdAt(deal.getCreatedAt())
                .currentStage(stageResponse)
                .contact(contactSummary)
                .account(accountSummary)
                .build();
    }
} 