package com.crmplatform.sales.service;

import com.crmplatform.common.security.UserContext;
import com.crmplatform.sales.entity.Deal;
import com.crmplatform.sales.entity.PipelineStage;
import com.crmplatform.sales.repository.DealRepository;
import com.crmplatform.sales.repository.PipelineStageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesManagerService {
    
    private final DealRepository dealRepository;
    private final PipelineStageRepository pipelineStageRepository;
    
    public Map<String, Object> getTeamStatistics() {
        Long tenantId = UserContext.getCurrentTenantId();
        log.info("Fetching team statistics for tenant: {}", tenantId);
        
        Map<String, Object> teamStats = new HashMap<>();
        
        // Get all deals for the tenant
        List<Deal> allDeals = dealRepository.findByTenantId(tenantId);
        
        // Get all pipeline stages for win rate calculation
        List<PipelineStage> allStages = pipelineStageRepository.findByTenantId(tenantId);
        Map<Long, PipelineStage> stageMap = allStages.stream()
                .collect(Collectors.toMap(PipelineStage::getStageId, stage -> stage));
        
        // Calculate basic metrics
        int totalDeals = allDeals.size();
        BigDecimal totalRevenue = allDeals.stream()
                .map(Deal::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal averageDealSize = totalDeals > 0 ? 
                totalRevenue.divide(BigDecimal.valueOf(totalDeals), 2, RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;
        
        // Calculate win rate (deals in WON stages)
        long wonDeals = allDeals.stream()
                .filter(deal -> {
                    PipelineStage stage = stageMap.get(deal.getStageId());
                    return stage != null && "WON".equals(stage.getStageType().name());
                })
                .count();
        
        int winRate = totalDeals > 0 ? (int) ((wonDeals * 100) / totalDeals) : 0;
        
        // Calculate pipeline value (deals not in WON or LOST stages)
        BigDecimal pipelineValue = allDeals.stream()
                .filter(deal -> {
                    PipelineStage stage = stageMap.get(deal.getStageId());
                    return stage != null && 
                           !"WON".equals(stage.getStageType().name()) && 
                           !"LOST".equals(stage.getStageType().name());
                })
                .map(Deal::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Get unique team members (deal owners)
        Set<Long> teamMembers = allDeals.stream()
                .map(Deal::getOwnerUserId)
                .collect(Collectors.toSet());
        
        int teamSize = teamMembers.size();
        
        // Build team performance data
        List<Map<String, Object>> teamPerformance = buildTeamPerformance(allDeals, stageMap, teamMembers);
        
        // Populate response
        teamStats.put("teamSize", teamSize);
        teamStats.put("totalDeals", totalDeals);
        teamStats.put("totalRevenue", totalRevenue.intValue());
        teamStats.put("averageDealSize", averageDealSize.intValue());
        teamStats.put("winRate", winRate);
        teamStats.put("pipelineValue", pipelineValue.intValue());
        teamStats.put("teamPerformance", teamPerformance);
        
        log.info("Team statistics calculated: {} team members, {} deals, ${} revenue", 
                teamSize, totalDeals, totalRevenue);
        
        return teamStats;
    }
    
    private List<Map<String, Object>> buildTeamPerformance(List<Deal> allDeals, 
                                                           Map<Long, PipelineStage> stageMap, 
                                                           Set<Long> teamMembers) {
        List<Map<String, Object>> teamPerformance = new ArrayList<>();
        
        for (Long userId : teamMembers) {
            List<Deal> userDeals = allDeals.stream()
                    .filter(deal -> deal.getOwnerUserId().equals(userId))
                    .collect(Collectors.toList());
            
            int userDealCount = userDeals.size();
            BigDecimal userRevenue = userDeals.stream()
                    .map(Deal::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Calculate user win rate
            long userWonDeals = userDeals.stream()
                    .filter(deal -> {
                        PipelineStage stage = stageMap.get(deal.getStageId());
                        return stage != null && "WON".equals(stage.getStageType().name());
                    })
                    .count();
            
            int userWinRate = userDealCount > 0 ? (int) ((userWonDeals * 100) / userDealCount) : 0;
            
            // Performance score based on deals, revenue, and win rate
            int performanceScore = calculatePerformanceScore(userDealCount, userRevenue.intValue(), userWinRate);
            
            Map<String, Object> memberStats = new HashMap<>();
            memberStats.put("name", "User " + userId); // TODO: Get actual user name from auth service
            memberStats.put("userId", userId);
            memberStats.put("deals", userDealCount);
            memberStats.put("revenue", userRevenue.intValue());
            memberStats.put("winRate", userWinRate);
            memberStats.put("performance", performanceScore);
            
            teamPerformance.add(memberStats);
        }
        
        // Sort by performance score descending
        teamPerformance.sort((a, b) -> 
                Integer.compare((Integer) b.get("performance"), (Integer) a.get("performance")));
        
        return teamPerformance;
    }
    
    private int calculatePerformanceScore(int deals, int revenue, int winRate) {
        // Simple performance scoring algorithm
        // 40% weight on deals, 40% on revenue, 20% on win rate
        double dealScore = Math.min(deals * 2, 40); // Max 40 points for deals
        double revenueScore = Math.min(revenue / 1000.0, 40); // Max 40 points for revenue (per $1000)
        double winRateScore = winRate * 0.2; // Max 20 points for win rate
        
        return (int) Math.min(dealScore + revenueScore + winRateScore, 100);
    }
}
