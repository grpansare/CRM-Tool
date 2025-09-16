package com.crmplatform.sales.service;

import com.crmplatform.common.security.UserContext;
import com.crmplatform.sales.dto.AnalyticsResponse;
import com.crmplatform.sales.entity.Deal;
import com.crmplatform.sales.entity.DealStageHistory;
import com.crmplatform.sales.entity.PipelineStage;
import com.crmplatform.sales.repository.DealRepository;
import com.crmplatform.sales.repository.DealStageHistoryRepository;
import com.crmplatform.sales.repository.PipelineStageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final DealRepository dealRepository;
    private final PipelineStageRepository pipelineStageRepository;
    private final DealStageHistoryRepository dealStageHistoryRepository;

    public AnalyticsResponse.RevenueForecast getRevenueForecast() {
        Long tenantId = UserContext.getCurrentTenantId();
        log.info("Generating revenue forecast for tenant: {}", tenantId);

        // Get all open deals
        List<Deal> openDeals = dealRepository.findOpenDealsByTenantId(tenantId);
        Map<Long, PipelineStage> stageMap = pipelineStageRepository.findByTenantId(tenantId)
                .stream()
                .collect(Collectors.toMap(PipelineStage::getStageId, stage -> stage));

        BigDecimal totalPipelineValue = openDeals.stream()
                .map(Deal::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal weightedForecast = openDeals.stream()
                .map(deal -> {
                    PipelineStage stage = stageMap.get(deal.getStageId());
                    if (stage != null && stage.getWinProbability() != null) {
                        return deal.getAmount().multiply(stage.getWinProbability().divide(BigDecimal.valueOf(100)));
                    }
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Best case: all deals close
        BigDecimal bestCaseScenario = totalPipelineValue;

        // Worst case: only deals in final stages close
        BigDecimal worstCaseScenario = openDeals.stream()
                .filter(deal -> {
                    PipelineStage stage = stageMap.get(deal.getStageId());
                    return stage != null && stage.getWinProbability() != null && 
                           stage.getWinProbability().compareTo(BigDecimal.valueOf(80)) >= 0;
                })
                .map(Deal::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Revenue by stage
        Map<Long, List<Deal>> dealsByStage = openDeals.stream()
                .collect(Collectors.groupingBy(Deal::getStageId));

        List<AnalyticsResponse.StageRevenue> revenueByStage = dealsByStage.entrySet().stream()
                .map(entry -> {
                    Long stageId = entry.getKey();
                    List<Deal> stageDeals = entry.getValue();
                    PipelineStage stage = stageMap.get(stageId);
                    
                    BigDecimal totalValue = stageDeals.stream()
                            .map(Deal::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    BigDecimal weightedValue = BigDecimal.ZERO;
                    if (stage != null && stage.getWinProbability() != null) {
                        weightedValue = totalValue.multiply(stage.getWinProbability().divide(BigDecimal.valueOf(100)));
                    }

                    return AnalyticsResponse.StageRevenue.builder()
                            .stageId(stageId)
                            .stageName(stage != null ? stage.getStageName() : "Unknown")
                            .totalValue(totalValue)
                            .weightedValue(weightedValue)
                            .dealCount(stageDeals.size())
                            .winProbability(stage != null ? stage.getWinProbability() : BigDecimal.ZERO)
                            .build();
                })
                .sorted(Comparator.comparing(sr -> stageMap.get(sr.getStageId()).getStageOrder()))
                .collect(Collectors.toList());

        return AnalyticsResponse.RevenueForecast.builder()
                .totalPipelineValue(totalPipelineValue)
                .weightedForecast(weightedForecast)
                .bestCaseScenario(bestCaseScenario)
                .worstCaseScenario(worstCaseScenario)
                .revenueByStage(revenueByStage)
                .build();
    }

    public AnalyticsResponse.SalesVelocity getSalesVelocity() {
        Long tenantId = UserContext.getCurrentTenantId();
        log.info("Calculating sales velocity for tenant: {}", tenantId);

        // Get all closed deals from last 6 months for analysis
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        List<Deal> closedDeals = dealRepository.findClosedDealsAfterDate(tenantId, sixMonthsAgo);
        
        if (closedDeals.isEmpty()) {
            return AnalyticsResponse.SalesVelocity.builder()
                    .averageDealCycleTime(0.0)
                    .averageStageTime(0.0)
                    .velocityByStage(new ArrayList<>())
                    .totalDealsAnalyzed(0)
                    .build();
        }

        // Calculate average deal cycle time
        List<Double> dealCycleTimes = new ArrayList<>();
        Map<Long, List<Integer>> stageTimeMap = new HashMap<>();
        Map<Long, PipelineStage> stageMap = pipelineStageRepository.findByTenantId(tenantId)
                .stream()
                .collect(Collectors.toMap(PipelineStage::getStageId, stage -> stage));

        for (Deal deal : closedDeals) {
            List<DealStageHistory> history = dealStageHistoryRepository.findByTenantIdAndDealIdOrderByChangedAt(tenantId, deal.getDealId());
            
            if (!history.isEmpty()) {
                // Calculate total cycle time
                LocalDateTime firstStage = history.get(0).getChangedAt();
                LocalDateTime lastStage = history.get(history.size() - 1).getChangedAt();
                long cycleTimeDays = ChronoUnit.DAYS.between(firstStage, lastStage);
                dealCycleTimes.add((double) cycleTimeDays);

                // Collect stage times
                for (DealStageHistory stageHistory : history) {
                    if (stageHistory.getTimeInPreviousStageDays() != null && stageHistory.getFromStageId() != null) {
                        stageTimeMap.computeIfAbsent(stageHistory.getFromStageId(), k -> new ArrayList<>())
                                .add(stageHistory.getTimeInPreviousStageDays());
                    }
                }
            }
        }

        Double averageDealCycleTime = dealCycleTimes.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        // Calculate velocity by stage
        List<AnalyticsResponse.StageVelocity> velocityByStage = stageTimeMap.entrySet().stream()
                .map(entry -> {
                    Long stageId = entry.getKey();
                    List<Integer> times = entry.getValue();
                    PipelineStage stage = stageMap.get(stageId);
                    
                    Double averageTime = times.stream()
                            .mapToInt(Integer::intValue)
                            .average()
                            .orElse(0.0);

                    return AnalyticsResponse.StageVelocity.builder()
                            .stageId(stageId)
                            .stageName(stage != null ? stage.getStageName() : "Unknown")
                            .averageTimeInStage(averageTime)
                            .dealsAnalyzed(times.size())
                            .build();
                })
                .sorted(Comparator.comparing(sv -> {
                    PipelineStage stage = stageMap.get(sv.getStageId());
                    return stage != null ? stage.getStageOrder() : Integer.MAX_VALUE;
                }))
                .collect(Collectors.toList());

        Double averageStageTime = velocityByStage.stream()
                .mapToDouble(AnalyticsResponse.StageVelocity::getAverageTimeInStage)
                .average()
                .orElse(0.0);

        return AnalyticsResponse.SalesVelocity.builder()
                .averageDealCycleTime(averageDealCycleTime)
                .averageStageTime(averageStageTime)
                .velocityByStage(velocityByStage)
                .totalDealsAnalyzed(closedDeals.size())
                .build();
    }

    public AnalyticsResponse.ConversionRates getConversionRates() {
        Long tenantId = UserContext.getCurrentTenantId();
        log.info("Calculating conversion rates for tenant: {}", tenantId);

        // Get all deals from last 6 months
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        List<Deal> allDeals = dealRepository.findDealsAfterDate(tenantId, sixMonthsAgo);
        
        if (allDeals.isEmpty()) {
            return AnalyticsResponse.ConversionRates.builder()
                    .overallConversionRate(BigDecimal.ZERO)
                    .conversionByStage(new ArrayList<>())
                    .winRate(BigDecimal.ZERO)
                    .lossRate(BigDecimal.ZERO)
                    .build();
        }

        Map<Long, PipelineStage> stageMap = pipelineStageRepository.findByTenantId(tenantId)
                .stream()
                .collect(Collectors.toMap(PipelineStage::getStageId, stage -> stage));

        // Calculate win/loss rates
        long wonDeals = allDeals.stream()
                .filter(deal -> {
                    PipelineStage stage = stageMap.get(deal.getStageId());
                    return stage != null && PipelineStage.StageType.WON.equals(stage.getStageType());
                })
                .count();

        long lostDeals = allDeals.stream()
                .filter(deal -> {
                    PipelineStage stage = stageMap.get(deal.getStageId());
                    return stage != null && PipelineStage.StageType.LOST.equals(stage.getStageType());
                })
                .count();

        long closedDeals = wonDeals + lostDeals;
        
        BigDecimal winRate = closedDeals > 0 ? 
                BigDecimal.valueOf(wonDeals).divide(BigDecimal.valueOf(closedDeals), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : 
                BigDecimal.ZERO;
        
        BigDecimal lossRate = closedDeals > 0 ? 
                BigDecimal.valueOf(lostDeals).divide(BigDecimal.valueOf(closedDeals), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : 
                BigDecimal.ZERO;

        BigDecimal overallConversionRate = allDeals.size() > 0 ? 
                BigDecimal.valueOf(wonDeals).divide(BigDecimal.valueOf(allDeals.size()), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : 
                BigDecimal.ZERO;

        // Calculate stage-to-stage conversion rates
        Map<String, Integer> stageTransitions = new HashMap<>();
        Map<String, Integer> successfulTransitions = new HashMap<>();

        for (Deal deal : allDeals) {
            List<DealStageHistory> history = dealStageHistoryRepository.findByTenantIdAndDealIdOrderByChangedAt(tenantId, deal.getDealId());
            
            for (int i = 0; i < history.size() - 1; i++) {
                DealStageHistory current = history.get(i);
                DealStageHistory next = history.get(i + 1);
                
                if (current.getToStageId() != null && next.getToStageId() != null) {
                    String transitionKey = current.getToStageId() + "->" + next.getToStageId();
                    stageTransitions.merge(transitionKey, 1, Integer::sum);
                    
                    // Check if this is a forward progression (successful transition)
                    PipelineStage fromStage = stageMap.get(current.getToStageId());
                    PipelineStage toStage = stageMap.get(next.getToStageId());
                    
                    if (fromStage != null && toStage != null && 
                        toStage.getStageOrder() > fromStage.getStageOrder()) {
                        successfulTransitions.merge(transitionKey, 1, Integer::sum);
                    }
                }
            }
        }

        List<AnalyticsResponse.StageConversion> conversionByStage = stageTransitions.entrySet().stream()
                .map(entry -> {
                    String[] stages = entry.getKey().split("->");
                    Long fromStageId = Long.valueOf(stages[0]);
                    Long toStageId = Long.valueOf(stages[1]);
                    
                    PipelineStage fromStage = stageMap.get(fromStageId);
                    PipelineStage toStage = stageMap.get(toStageId);
                    
                    int total = entry.getValue();
                    int successful = successfulTransitions.getOrDefault(entry.getKey(), 0);
                    
                    BigDecimal conversionRate = total > 0 ? 
                            BigDecimal.valueOf(successful).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : 
                            BigDecimal.ZERO;

                    return AnalyticsResponse.StageConversion.builder()
                            .fromStageId(fromStageId)
                            .fromStageName(fromStage != null ? fromStage.getStageName() : "Unknown")
                            .toStageId(toStageId)
                            .toStageName(toStage != null ? toStage.getStageName() : "Unknown")
                            .conversionRate(conversionRate)
                            .totalTransitions(total)
                            .successfulTransitions(successful)
                            .build();
                })
                .collect(Collectors.toList());

        return AnalyticsResponse.ConversionRates.builder()
                .overallConversionRate(overallConversionRate)
                .conversionByStage(conversionByStage)
                .winRate(winRate)
                .lossRate(lossRate)
                .build();
    }

    public AnalyticsResponse.SalesMetrics getComprehensiveMetrics() {
        log.info("Generating comprehensive sales metrics");
        
        AnalyticsResponse.RevenueForecast revenueForecast = getRevenueForecast();
        AnalyticsResponse.SalesVelocity salesVelocity = getSalesVelocity();
        AnalyticsResponse.ConversionRates conversionRates = getConversionRates();
        
        Map<String, Object> additionalMetrics = new HashMap<>();
        additionalMetrics.put("generatedAt", LocalDateTime.now());
        additionalMetrics.put("analysisVersion", "1.0");
        
        return AnalyticsResponse.SalesMetrics.builder()
                .revenueForecast(revenueForecast)
                .salesVelocity(salesVelocity)
                .conversionRates(conversionRates)
                .additionalMetrics(additionalMetrics)
                .build();
    }

    public AnalyticsResponse getComprehensiveAnalytics() {
        log.info("Generating comprehensive analytics");
        
        AnalyticsResponse.RevenueForecast revenueForecast = getRevenueForecast();
        AnalyticsResponse.SalesVelocity salesVelocity = getSalesVelocity();
        AnalyticsResponse.ConversionRates conversionRates = getConversionRates();
        
        return AnalyticsResponse.builder()
                .revenueForecast(revenueForecast)
                .salesVelocity(salesVelocity)
                .conversionRates(conversionRates)
                .build();
    }
}
