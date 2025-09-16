package com.crmplatform.sales.service;

import com.crmplatform.sales.entity.Lead;
import com.crmplatform.sales.entity.LeadSource;
import com.crmplatform.sales.entity.LeadStatus;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class LeadScoringService {
    
    private static final Logger log = LoggerFactory.getLogger(LeadScoringService.class);
    
    // Scoring weights (total should be 100)
    private static final int DEMOGRAPHIC_WEIGHT = 30;
    private static final int FIRMOGRAPHIC_WEIGHT = 25;
    private static final int BEHAVIORAL_WEIGHT = 25;
    private static final int SOURCE_WEIGHT = 20;
    
    // Email domain scoring patterns
    private static final Pattern BUSINESS_EMAIL_PATTERN = Pattern.compile(
        ".*@(?!gmail|yahoo|hotmail|outlook|aol|icloud|live|msn|protonmail|yandex).*\\.[a-z]{2,}"
    );
    
    public int calculateLeadScore(Lead lead) {
        try {
            log.info("Calculating lead score for leadId: {}", lead.getLeadId());
            
            int demographicScore = calculateDemographicScore(lead);
            int firmographicScore = calculateFirmographicScore(lead);
            int behavioralScore = calculateBehavioralScore(lead);
            int sourceScore = calculateSourceScore(lead);
            
            int totalScore = Math.min(100, Math.max(0, 
                (demographicScore * DEMOGRAPHIC_WEIGHT / 100) +
                (firmographicScore * FIRMOGRAPHIC_WEIGHT / 100) +
                (behavioralScore * BEHAVIORAL_WEIGHT / 100) +
                (sourceScore * SOURCE_WEIGHT / 100)
            ));
            
            log.info("Lead score calculated: leadId={}, demographic={}, firmographic={}, behavioral={}, source={}, total={}", 
                lead.getLeadId(), demographicScore, firmographicScore, behavioralScore, sourceScore, totalScore);
            
            return totalScore;
            
        } catch (Exception e) {
            log.error("Error calculating lead score for leadId: {}", lead.getLeadId(), e);
            return 0;
        }
    }
    
    private int calculateDemographicScore(Lead lead) {
        int score = 0;
        
        // Email quality (40 points)
        if (lead.getEmail() != null && !lead.getEmail().trim().isEmpty()) {
            if (BUSINESS_EMAIL_PATTERN.matcher(lead.getEmail().toLowerCase()).matches()) {
                score += 40; // Business email domain
            } else {
                score += 20; // Personal email domain
            }
        }
        
        // Job title relevance (35 points)
        if (lead.getJobTitle() != null && !lead.getJobTitle().trim().isEmpty()) {
            String jobTitle = lead.getJobTitle().toLowerCase();
            if (jobTitle.contains("ceo") || jobTitle.contains("president") || jobTitle.contains("founder")) {
                score += 35; // C-level executives
            } else if (jobTitle.contains("director") || jobTitle.contains("vp") || jobTitle.contains("vice president")) {
                score += 30; // Directors/VPs
            } else if (jobTitle.contains("manager") || jobTitle.contains("head")) {
                score += 25; // Managers
            } else if (jobTitle.contains("lead") || jobTitle.contains("senior")) {
                score += 20; // Senior roles
            } else {
                score += 15; // Other roles
            }
        }
        
        // Contact completeness (25 points)
        int completenessScore = 0;
        if (lead.getPhoneNumber() != null && !lead.getPhoneNumber().trim().isEmpty()) completenessScore += 8;
        if (lead.getCompany() != null && !lead.getCompany().trim().isEmpty()) completenessScore += 10;
        if (lead.getJobTitle() != null && !lead.getJobTitle().trim().isEmpty()) completenessScore += 7;
        score += completenessScore;
        
        return Math.min(100, score);
    }
    
    private int calculateFirmographicScore(Lead lead) {
        int score = 0;
        
        // Company presence (30 points)
        if (lead.getCompany() != null && !lead.getCompany().trim().isEmpty()) {
            String company = lead.getCompany().toLowerCase();
            
            // Company size scoring (20 points)
            if (lead.getEmployeeCount() != null) {
                if (lead.getEmployeeCount() >= 1000) score += 20; // Enterprise
                else if (lead.getEmployeeCount() >= 500) score += 15; // Large business
                else if (lead.getEmployeeCount() >= 100) score += 10; // Medium business
                else if (lead.getEmployeeCount() >= 10) score += 5; // Small business
            }
            
            // Annual revenue scoring (20 points)
            if (lead.getAnnualRevenue() != null) {
                if (lead.getAnnualRevenue() >= 10_000_000) score += 20; // $10M+
                else if (lead.getAnnualRevenue() >= 1_000_000) score += 15; // $1M-$10M
                else if (lead.getAnnualRevenue() >= 100_000) score += 10; // $100K-$1M
                else if (lead.getAnnualRevenue() >= 10_000) score += 5; // $10K-$100K
            }
            
            // Industry scoring (10 points)
            if (lead.getIndustry() != null && !lead.getIndustry().trim().isEmpty()) {
                String industry = lead.getIndustry().toLowerCase();
                // Higher scores for industries with typically larger budgets
                if (industry.contains("tech") || industry.contains("finance") || 
                    industry.contains("health") || industry.contains("manufacturing")) {
                    score += 10;
                } else if (industry.contains("retail") || industry.contains("education") || 
                          industry.contains("real estate") || industry.contains("professional")) {
                    score += 7;
                } else {
                    score += 5; // Other industries
                }
            }
            
            // Estimate company size based on company name patterns
            if (company.contains("corp") || company.contains("corporation") || 
                company.contains("inc") || company.contains("ltd") || 
                company.contains("llc") || company.contains("group")) {
                score += 40; // Likely established company
            } else if (company.contains("solutions") || company.contains("systems") || 
                      company.contains("technologies") || company.contains("consulting")) {
                score += 35; // Professional services company
            } else if (company.contains("startup") || company.contains("studio")) {
                score += 25; // Startup/smaller company
            } else {
                score += 30; // Generic company
            }
        }
        
        // Job title indicates company level (30 points)
        if (lead.getJobTitle() != null && !lead.getJobTitle().trim().isEmpty()) {
            String jobTitle = lead.getJobTitle().toLowerCase();
            if (jobTitle.contains("ceo") || jobTitle.contains("president") || 
                jobTitle.contains("founder") || jobTitle.contains("owner")) {
                score += 30; // Decision maker at company
            } else if (jobTitle.contains("director") || jobTitle.contains("vp") || 
                      jobTitle.contains("vice president") || jobTitle.contains("head")) {
                score += 25; // Senior leadership
            } else if (jobTitle.contains("manager") || jobTitle.contains("lead")) {
                score += 20; // Management level
            } else {
                score += 15; // Individual contributor
            }
        }
        
        // Email domain quality (20 points)
        if (lead.getEmail() != null && !lead.getEmail().trim().isEmpty()) {
            String email = lead.getEmail().toLowerCase();
            if (BUSINESS_EMAIL_PATTERN.matcher(email).matches()) {
                // Business domain suggests established company
                score += 20;
            } else {
                score += 5; // Personal email domain
            }
        }
        
        return Math.min(100, score);
    }
    
    private int calculateBehavioralScore(Lead lead) {
        int score = 0;
        
        // Lead status progression (60 points)
        if (lead.getLeadStatus() != null) {
            switch (lead.getLeadStatus()) {
                case QUALIFIED:
                    score += 60;
                    break;
                case CONTACTED:
                    score += 40;
                    break;
                case NURTURING:
                    score += 30;
                    break;
                case NEW:
                    score += 20;
                    break;
                case UNQUALIFIED:
                    score += 0;
                    break;
                case CONVERTED:
                    score += 100; // Already converted
                    break;
            }
        }
        
        // Notes indicate engagement (20 points)
        if (lead.getNotes() != null && !lead.getNotes().trim().isEmpty()) {
            if (lead.getNotes().length() > 100) {
                score += 20; // Detailed notes indicate high engagement
            } else {
                score += 10; // Some notes
            }
        }
        
        // Phone number provided indicates higher intent (20 points)
        if (lead.getPhoneNumber() != null && !lead.getPhoneNumber().trim().isEmpty()) {
            score += 20; // Providing phone shows higher engagement
        }
        
        return Math.min(100, score);
    }
    
    private int calculateSourceScore(Lead lead) {
        if (lead.getLeadSource() == null) {
            return 30; // Default score for unknown source
        }
        
        // Source quality scoring based on typical conversion rates
        switch (lead.getLeadSource()) {
            case REFERRAL:
                return 90; // Highest quality - warm introductions
            case PARTNER:
                return 85; // High quality - trusted partnerships
            case WEBINAR:
                return 80; // High intent - educational engagement
            case CONTENT_DOWNLOAD:
                return 75; // Good intent - seeking information
            case WEBSITE:
                return 70; // Organic interest
            case EMAIL_CAMPAIGN:
                return 65; // Targeted marketing
            case TRADE_SHOW:
                return 60; // Face-to-face interaction
            case SOCIAL_MEDIA:
                return 55; // Social engagement
            case ADVERTISEMENT:
                return 50; // Paid acquisition
            case COLD_CALL:
                return 40; // Outbound effort
            case OTHER:
            default:
                return 30; // Unknown quality
        }
    }
    
    public Map<String, Integer> getScoreBreakdown(Lead lead) {
        Map<String, Integer> breakdown = new HashMap<>();
        
        int demographicScore = calculateDemographicScore(lead);
        int firmographicScore = calculateFirmographicScore(lead);
        int behavioralScore = calculateBehavioralScore(lead);
        int sourceScore = calculateSourceScore(lead);
        
        breakdown.put("demographic", (demographicScore * DEMOGRAPHIC_WEIGHT / 100));
        breakdown.put("firmographic", (firmographicScore * FIRMOGRAPHIC_WEIGHT / 100));
        breakdown.put("behavioral", (behavioralScore * BEHAVIORAL_WEIGHT / 100));
        breakdown.put("source", (sourceScore * SOURCE_WEIGHT / 100));
        breakdown.put("total", calculateLeadScore(lead));
        
        return breakdown;
    }
    
    public String getScoreGrade(int score) {
        if (score >= 80) return "A"; // Hot lead
        if (score >= 60) return "B"; // Warm lead
        if (score >= 40) return "C"; // Cold lead
        return "D"; // Poor lead
    }
    
    public String getScoreDescription(int score) {
        if (score >= 80) return "Hot Lead - High priority, immediate follow-up recommended";
        if (score >= 60) return "Warm Lead - Good potential, schedule follow-up within 24 hours";
        if (score >= 40) return "Cold Lead - Moderate potential, nurture with content marketing";
        return "Poor Lead - Low potential, consider for long-term nurturing campaigns";
    }
}
