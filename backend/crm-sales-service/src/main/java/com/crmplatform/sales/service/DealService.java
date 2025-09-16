package com.crmplatform.sales.service;

import com.crmplatform.common.dto.ApiResponse;
import com.crmplatform.common.security.UserContext;
import com.crmplatform.sales.dto.AccountDealSummaryResponse;
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

import java.math.BigDecimal;
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
    // private final EventPublisher eventPublisher; // Disabled until RabbitMQ is configured
    
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
        
        // Publish event - Enable automation (Disabled until RabbitMQ is configured)
        // eventPublisher.publishDealCreated(deal);
        
        // Build response with contact and account details
        DealResponse response = buildDealResponseWithDetails(deal, stage.get());
        
        return ApiResponse.success(response, "Deal created successfully");
    }
    
    @Transactional
    public ApiResponse<DealResponse> updateDealStage(Long dealId, UpdateDealStageRequest request) {
        Long tenantId = UserContext.getCurrentTenantId();
        Long ownerUserId = UserContext.getCurrentUserId();
        
        log.info("=== UPDATE DEAL STAGE DEBUG === DealId: {}, NewStageId: {}, TenantId: {}, UserId: {}", 
                 dealId, request.getNewStageId(), tenantId, ownerUserId);
        
        // Get the deal
        Optional<Deal> dealOpt = dealRepository.findByTenantIdAndDealId(tenantId, dealId);
        if (dealOpt.isEmpty()) {
            log.warn("Deal not found: dealId={}, tenantId={}", dealId, tenantId);
            return ApiResponse.error("Deal not found", "DEAL_NOT_FOUND");
        }
        
        Deal deal = dealOpt.get();
        log.info("Found deal: dealId={}, ownerId={}, currentStageId={}", deal.getDealId(), deal.getOwnerUserId(), deal.getStageId());
        
        // Validate user permissions (deal owner or manager) - Allow managers to update any deal
        String userRole = UserContext.getCurrentUserRole();
        if (!deal.getOwnerUserId().equals(ownerUserId) && 
            !"SALES_MANAGER".equals(userRole) && !"TENANT_ADMIN".equals(userRole)) {
            log.warn("Insufficient permissions: userId={}, dealOwnerId={}, userRole={}", ownerUserId, deal.getOwnerUserId(), userRole);
            return ApiResponse.error("Insufficient permissions", "INSUFFICIENT_PERMISSIONS");
        }
        
        // Validate new stage exists
        Optional<PipelineStage> newStage = pipelineStageRepository
                .findByTenantIdAndStageId(tenantId, request.getNewStageId());
        
        if (newStage.isEmpty()) {
            log.warn("Invalid stage ID: stageId={}, tenantId={}", request.getNewStageId(), tenantId);
            return ApiResponse.error("Invalid stage ID", "INVALID_STAGE");
        }
        
        log.info("Stage validation passed: stageId={}, stageName={}", newStage.get().getStageId(), newStage.get().getStageName());
        
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
        
        // Publish event - Enable automation (Disabled until RabbitMQ is configured)
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
    
    @Transactional
    public ApiResponse<Void> deleteDeal(Long dealId) {
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
        
        // Delete stage history first (foreign key constraint)
        dealStageHistoryRepository.deleteByTenantIdAndDealId(tenantId, dealId);
        
        // Delete the deal
        dealRepository.delete(deal);
        
        return ApiResponse.success(null, "Deal deleted successfully");
    }
    
    @Transactional
    public ApiResponse<DealResponse> updateDeal(Long dealId, CreateDealRequest request) {
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
        
        // Update deal fields
        deal.setDealName(request.getDealName());
        deal.setAmount(request.getAmount());
        deal.setExpectedCloseDate(request.getExpectedCloseDate());
        deal.setContactId(request.getContactId());
        deal.setAccountId(request.getAccountId());
        
        // Handle stage change if needed
        if (!deal.getStageId().equals(request.getStageId())) {
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
                    .toStageId(request.getStageId())
                    .timeInPreviousStageDays(timeInPreviousStageDays)
                    .build();
            
            dealStageHistoryRepository.save(stageHistory);
            deal.setStageId(request.getStageId());
        }
        
        deal = dealRepository.save(deal);
        
        // Build response with contact and account details
        DealResponse response = buildDealResponseWithDetails(deal, stage.get());
        
        return ApiResponse.success(response, "Deal updated successfully");
    }
    
    public ApiResponse<List<DealResponse.PipelineStageResponse>> getAvailableStages() {
        Long tenantId = UserContext.getCurrentTenantId();
        
        List<PipelineStage> stages = pipelineStageRepository.findByTenantIdOrderByStageOrder(tenantId);
        
        List<DealResponse.PipelineStageResponse> stageResponses = stages.stream()
                .map(stage -> DealResponse.PipelineStageResponse.builder()
                        .stageId(stage.getStageId())
                        .stageName(stage.getStageName())
                        .stageOrder(stage.getStageOrder())
                        .stageType(stage.getStageType().name())
                        .winProbability(stage.getWinProbability())
                        .build())
                .toList();
        
        return ApiResponse.success(stageResponses, "Stages retrieved successfully");
    }
    
    // Account-level deal aggregation methods
    public ApiResponse<AccountDealSummaryResponse> getAccountDealSummary(Long accountId) {
        Long tenantId = UserContext.getCurrentTenantId();
        
        // Validate account exists
        if (!contactIntegrationService.validateAccount(accountId)) {
            return ApiResponse.error("Account not found", "ACCOUNT_NOT_FOUND");
        }
        
        // Get account details
        Map<String, Object> accountDetails = contactIntegrationService.getAccountDetails(accountId);
        String accountName = accountDetails != null ? (String) accountDetails.get("accountName") : "Unknown Account";
        
        // Get deal aggregation data
        Long totalDeals = dealRepository.countDealsByAccount(tenantId, accountId);
        BigDecimal totalDealValue = dealRepository.getTotalDealValueByAccount(tenantId, accountId);
        Long wonDeals = dealRepository.countWonDealsByAccount(tenantId, accountId);
        BigDecimal wonDealValue = dealRepository.getWonDealValueByAccount(tenantId, accountId);
        
        // Get top open deals
        List<Deal> topOpenDeals = dealRepository.findOpenDealsByAccountOrderByAmount(tenantId, accountId);
        List<DealResponse> topOpenDealResponses = topOpenDeals.stream()
                .limit(5) // Top 5 deals
                .map(deal -> {
                    Optional<PipelineStage> stage = pipelineStageRepository
                            .findByTenantIdAndStageId(tenantId, deal.getStageId());
                    return buildDealResponseWithDetails(deal, stage.orElse(null));
                })
                .toList();
        
        // Handle null values
        if (totalDealValue == null) totalDealValue = BigDecimal.ZERO;
        if (wonDealValue == null) wonDealValue = BigDecimal.ZERO;
        if (totalDeals == null) totalDeals = 0L;
        if (wonDeals == null) wonDeals = 0L;
        
        AccountDealSummaryResponse summary = new AccountDealSummaryResponse(
                accountId, accountName, totalDeals, totalDealValue, wonDeals, wonDealValue);
        summary.setTopOpenDeals(topOpenDealResponses);
        
        return ApiResponse.success(summary, "Account deal summary retrieved successfully");
    }
    
    public ApiResponse<List<AccountDealSummaryResponse>> getAccountDealSummaries(List<Long> accountIds) {
        Long tenantId = UserContext.getCurrentTenantId();
        
        List<AccountDealSummaryResponse> summaries = accountIds.stream()
                .map(accountId -> {
                    // Get account details
                    Map<String, Object> accountDetails = contactIntegrationService.getAccountDetails(accountId);
                    String accountName = accountDetails != null ? (String) accountDetails.get("accountName") : "Unknown Account";
                    
                    // Get deal aggregation data
                    Long totalDeals = dealRepository.countDealsByAccount(tenantId, accountId);
                    BigDecimal totalDealValue = dealRepository.getTotalDealValueByAccount(tenantId, accountId);
                    Long wonDeals = dealRepository.countWonDealsByAccount(tenantId, accountId);
                    BigDecimal wonDealValue = dealRepository.getWonDealValueByAccount(tenantId, accountId);
                    
                    // Handle null values
                    if (totalDealValue == null) totalDealValue = BigDecimal.ZERO;
                    if (wonDealValue == null) wonDealValue = BigDecimal.ZERO;
                    if (totalDeals == null) totalDeals = 0L;
                    if (wonDeals == null) wonDeals = 0L;
                    
                    return new AccountDealSummaryResponse(
                            accountId, accountName, totalDeals, totalDealValue, wonDeals, wonDealValue);
                })
                .toList();
        
        return ApiResponse.success(summaries, "Account deal summaries retrieved successfully");
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