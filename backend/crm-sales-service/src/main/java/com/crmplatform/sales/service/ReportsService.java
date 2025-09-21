package com.crmplatform.sales.service;

import com.crmplatform.sales.entity.Lead;
import com.crmplatform.sales.entity.LeadDisposition;
import com.crmplatform.sales.entity.LeadStatus;
import com.crmplatform.sales.entity.LeadSource;
import com.crmplatform.sales.repository.LeadRepository;
import com.crmplatform.common.security.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportsService {
    
    @Autowired
    private LeadRepository leadRepository;
    
    // Lead Overview Analytics
    public Map<String, Object> getLeadOverviewReport() {
        Long tenantId = UserContext.getCurrentTenantId();
        
        Map<String, Object> report = new HashMap<>();
        
        // Total leads count
        long totalLeads = leadRepository.countByTenantId(tenantId);
        report.put("totalLeads", totalLeads);
        
        // Leads by status
        Map<String, Long> leadsByStatus = new HashMap<>();
        for (LeadStatus status : LeadStatus.values()) {
            long count = leadRepository.countByTenantIdAndLeadStatus(tenantId, status);
            leadsByStatus.put(status.name(), count);
        }
        report.put("leadsByStatus", leadsByStatus);
        
        // Recent leads (last 30 days)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Lead> recentLeads = leadRepository.findRecentLeads(tenantId, thirtyDaysAgo);
        report.put("recentLeadsCount", recentLeads.size());
        
        // Conversion rate
        long convertedLeads = leadRepository.countByTenantIdAndLeadStatus(tenantId, LeadStatus.CONVERTED);
        double conversionRate = totalLeads > 0 ? (double) convertedLeads / totalLeads * 100 : 0;
        report.put("conversionRate", Math.round(conversionRate * 100.0) / 100.0);
        
        return report;
    }
    
    // Lead Disposition Analytics
    public Map<String, Object> getDispositionReport() {
        Long tenantId = UserContext.getCurrentTenantId();
        
        Map<String, Object> report = new HashMap<>();
        
        // Disposition statistics
        List<Object[]> dispositionStats = leadRepository.getDispositionStatistics(tenantId);
        Map<String, Long> dispositionCounts = new HashMap<>();
        
        for (Object[] stat : dispositionStats) {
            LeadDisposition disposition = (LeadDisposition) stat[0];
            Long count = (Long) stat[1];
            dispositionCounts.put(disposition.name(), count);
        }
        report.put("dispositionCounts", dispositionCounts);
        
        // Disposition categories summary
        Map<String, Long> categorySummary = new HashMap<>();
        long positiveCount = 0;
        long negativeCount = 0;
        long followUpCount = 0;
        
        for (Map.Entry<String, Long> entry : dispositionCounts.entrySet()) {
            LeadDisposition disposition = LeadDisposition.valueOf(entry.getKey());
            Long count = entry.getValue();
            
            if (disposition.isPositive()) {
                positiveCount += count;
            } else if (disposition.isNegative()) {
                negativeCount += count;
            } else if (disposition.requiresFollowUp()) {
                followUpCount += count;
            }
        }
        
        categorySummary.put("positive", positiveCount);
        categorySummary.put("negative", negativeCount);
        categorySummary.put("followUp", followUpCount);
        report.put("categorySummary", categorySummary);
        
        // Leads requiring follow-up
        List<Lead> followUpLeads = leadRepository.findLeadsRequiringFollowUp(tenantId, LocalDateTime.now());
        report.put("followUpRequired", followUpLeads.size());
        
        return report;
    }
    
    // Lead Source Performance
    public Map<String, Object> getSourcePerformanceReport() {
        Long tenantId = UserContext.getCurrentTenantId();
        
        Map<String, Object> report = new HashMap<>();
        Map<String, Map<String, Object>> sourcePerformance = new HashMap<>();
        
        for (LeadSource source : LeadSource.values()) {
            Map<String, Object> sourceData = new HashMap<>();
            
            // Total leads from this source
            long totalFromSource = leadRepository.findByTenantIdAndLeadSource(tenantId, source, null).getTotalElements();
            sourceData.put("total", totalFromSource);
            
            // Converted leads from this source
            long convertedFromSource = leadRepository.countByTenantIdAndLeadStatus(tenantId, LeadStatus.CONVERTED);
            // Note: This is simplified - in real implementation, you'd need a query that filters by both source and status
            sourceData.put("converted", 0); // Placeholder
            
            // Conversion rate for this source
            double sourceConversionRate = totalFromSource > 0 ? (double) 0 / totalFromSource * 100 : 0; // Placeholder
            sourceData.put("conversionRate", sourceConversionRate);
            
            sourcePerformance.put(source.name(), sourceData);
        }
        
        report.put("sourcePerformance", sourcePerformance);
        return report;
    }
    
    // Lead Score Distribution
    public Map<String, Object> getLeadScoreReport() {
        Long tenantId = UserContext.getCurrentTenantId();
        
        Map<String, Object> report = new HashMap<>();
        
        // Get all leads with scores
        List<Lead> allLeads = leadRepository.findByTenantId(tenantId, null).getContent();
        
        // Score distribution
        Map<String, Long> scoreDistribution = new HashMap<>();
        scoreDistribution.put("A_Grade_80_100", 0L);
        scoreDistribution.put("B_Grade_60_79", 0L);
        scoreDistribution.put("C_Grade_40_59", 0L);
        scoreDistribution.put("D_Grade_0_39", 0L);
        scoreDistribution.put("No_Score", 0L);
        
        for (Lead lead : allLeads) {
            Integer score = lead.getLeadScore();
            if (score == null || score == 0) {
                scoreDistribution.put("No_Score", scoreDistribution.get("No_Score") + 1);
            } else if (score >= 80) {
                scoreDistribution.put("A_Grade_80_100", scoreDistribution.get("A_Grade_80_100") + 1);
            } else if (score >= 60) {
                scoreDistribution.put("B_Grade_60_79", scoreDistribution.get("B_Grade_60_79") + 1);
            } else if (score >= 40) {
                scoreDistribution.put("C_Grade_40_59", scoreDistribution.get("C_Grade_40_59") + 1);
            } else {
                scoreDistribution.put("D_Grade_0_39", scoreDistribution.get("D_Grade_0_39") + 1);
            }
        }
        
        report.put("scoreDistribution", scoreDistribution);
        
        // Average score
        double averageScore = allLeads.stream()
            .filter(lead -> lead.getLeadScore() != null && lead.getLeadScore() > 0)
            .mapToInt(Lead::getLeadScore)
            .average()
            .orElse(0.0);
        report.put("averageScore", Math.round(averageScore * 100.0) / 100.0);
        
        return report;
    }
    
    // Time-based Analytics
    public Map<String, Object> getTimeBasedReport(int days) {
        Long tenantId = UserContext.getCurrentTenantId();
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        
        Map<String, Object> report = new HashMap<>();
        
        // Leads created over time
        List<Lead> recentLeads = leadRepository.findRecentLeads(tenantId, startDate);
        Map<String, Long> leadsByDay = recentLeads.stream()
            .collect(Collectors.groupingBy(
                lead -> lead.getCreatedAt().toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                Collectors.counting()
            ));
        report.put("leadsByDay", leadsByDay);
        
        // Contacts made over time
        List<Lead> contactedLeads = leadRepository.findLeadsContactedInDateRange(tenantId, startDate, LocalDateTime.now());
        Map<String, Long> contactsByDay = contactedLeads.stream()
            .filter(lead -> lead.getLastContactDate() != null)
            .collect(Collectors.groupingBy(
                lead -> lead.getLastContactDate().toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                Collectors.counting()
            ));
        report.put("contactsByDay", contactsByDay);
        
        return report;
    }
    
    // Activity Summary
    public Map<String, Object> getActivitySummaryReport() {
        Long tenantId = UserContext.getCurrentTenantId();
        
        Map<String, Object> report = new HashMap<>();
        
        // Total leads with dispositions
        List<Lead> leadsWithDispositions = leadRepository.findByTenantId(tenantId, null)
            .getContent()
            .stream()
            .filter(lead -> lead.getCurrentDisposition() != null)
            .collect(Collectors.toList());
        
        report.put("leadsWithDispositions", leadsWithDispositions.size());
        
        // Leads without dispositions
        List<Lead> leadsWithoutDispositions = leadRepository.findLeadsWithoutDisposition(tenantId);
        report.put("leadsWithoutDispositions", leadsWithoutDispositions.size());
        
        // Recent activity (last 7 days)
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        List<Lead> recentActivity = leadRepository.findLeadsContactedInDateRange(tenantId, weekAgo, LocalDateTime.now());
        report.put("recentActivityCount", recentActivity.size());
        
        return report;
    }
    
    // Comprehensive Dashboard Report
    public Map<String, Object> getDashboardReport() {
        Map<String, Object> dashboard = new HashMap<>();
        
        dashboard.put("overview", getLeadOverviewReport());
        dashboard.put("dispositions", getDispositionReport());
        dashboard.put("scores", getLeadScoreReport());
        dashboard.put("sources", getSourcePerformanceReport());
        dashboard.put("activity", getActivitySummaryReport());
        dashboard.put("timeBasedLast30Days", getTimeBasedReport(30));
        
        return dashboard;
    }
}
