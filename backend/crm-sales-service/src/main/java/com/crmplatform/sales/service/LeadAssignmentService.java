package com.crmplatform.sales.service;

import com.crmplatform.sales.entity.*;
import com.crmplatform.sales.repository.*;
import com.crmplatform.common.security.UserContext;
import com.crmplatform.sales.entity.AssignmentMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class LeadAssignmentService {
    
    private static final Logger log = LoggerFactory.getLogger(LeadAssignmentService.class);
    
    @Autowired
    private LeadAssignmentRuleRepository assignmentRuleRepository;
    
    @Autowired
    private LeadAssignmentHistoryRepository assignmentHistoryRepository;
    
    @Autowired
    private UserLeadWorkloadRepository workloadRepository;
    
    @Autowired
    private LeadRoutingQueueRepository routingQueueRepository;
    
    @Autowired
    private LeadRepository leadRepository;
    
    // Round-robin state tracking (in production, use Redis or database)
    private final Map<Long, Integer> roundRobinCounters = new HashMap<>();
    
    /**
     * Automatically assign a lead based on active assignment rules
     */
    public Long assignLead(Lead lead) {
        try {
            log.info("Starting lead assignment for leadId: {}", lead.getLeadId());
            
            // Get active assignment rules ordered by priority
            List<LeadAssignmentRule> rules = assignmentRuleRepository
                .findByTenantIdAndIsActiveOrderByPriorityOrderAsc(lead.getTenantId(), true);
            
            if (rules.isEmpty()) {
                log.warn("No active assignment rules found for tenant: {}", lead.getTenantId());
                return assignToDefaultUser(lead);
            }
            
            // Try each rule until one matches and assigns successfully
            for (LeadAssignmentRule rule : rules) {
                if (evaluateRuleConditions(lead, rule)) {
                    Long assignedUserId = executeAssignmentStrategy(lead, rule);
                    if (assignedUserId != null) {
                        // Update lead owner
                        lead.setOwnerUserId(assignedUserId);
                        leadRepository.save(lead);
                        
                        // Record assignment history
                        recordAssignmentHistory(lead, rule, assignedUserId);
                        
                        // Update workload tracking
                        updateUserWorkload(assignedUserId, lead.getTenantId());
                        
                        log.info("Lead {} assigned to user {} using rule {}", 
                            lead.getLeadId(), assignedUserId, rule.getRuleName());
                        
                        return assignedUserId;
                    }
                }
            }
            
            // If no rule matched, assign to default user
            log.info("No assignment rule matched for lead {}, using default assignment", lead.getLeadId());
            return assignToDefaultUser(lead);
            
        } catch (Exception e) {
            log.error("Error assigning lead {}: {}", lead.getLeadId(), e.getMessage(), e);
            return assignToDefaultUser(lead);
        }
    }
    
    /**
     * Evaluate if a lead matches the rule conditions
     */
    private boolean evaluateRuleConditions(Lead lead, LeadAssignmentRule rule) {
        try {
            Map<String, Object> conditions = rule.getConditionsMap();
            
            if (conditions.isEmpty()) {
                return true; // No conditions means rule applies to all leads
            }
            
            // Check lead source condition
            if (conditions.containsKey("leadSource")) {
                List<String> allowedSources = (List<String>) conditions.get("leadSource");
                if (lead.getLeadSource() != null && 
                    !allowedSources.contains(lead.getLeadSource().name())) {
                    return false;
                }
            }
            
            // Check lead score condition
            if (conditions.containsKey("leadScore")) {
                Map<String, Integer> scoreRange = (Map<String, Integer>) conditions.get("leadScore");
                int leadScore = lead.getLeadScore() != null ? lead.getLeadScore() : 0;
                
                if (scoreRange.containsKey("min") && leadScore < scoreRange.get("min")) {
                    return false;
                }
                if (scoreRange.containsKey("max") && leadScore > scoreRange.get("max")) {
                    return false;
                }
            }
            
            // Check lead status condition
            if (conditions.containsKey("leadStatus")) {
                List<String> allowedStatuses = (List<String>) conditions.get("leadStatus");
                if (lead.getLeadStatus() != null && 
                    !allowedStatuses.contains(lead.getLeadStatus().name())) {
                    return false;
                }
            }
            
            // Check company size condition (if available)
            if (conditions.containsKey("companySize")) {
                Map<String, Integer> sizeRange = (Map<String, Integer>) conditions.get("companySize");
                int companySize = lead.getEmployeeCount() != null ? lead.getEmployeeCount() : 0;
                
                if (sizeRange.containsKey("min") && companySize < sizeRange.get("min")) {
                    return false;
                }
                if (sizeRange.containsKey("max") && companySize > sizeRange.get("max")) {
                    return false;
                }
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("Error evaluating rule conditions for rule {}: {}", rule.getRuleId(), e.getMessage());
            return false;
        }
    }
    
    /**
     * Execute the assignment strategy defined in the rule
     */
    private Long executeAssignmentStrategy(Lead lead, LeadAssignmentRule rule) {
        List<Long> availableUsers = getAvailableUsers(rule);
        
        if (availableUsers.isEmpty()) {
            log.warn("No available users found for rule: {}", rule.getRuleName());
            return null;
        }
        
        switch (rule.getAssignmentStrategy()) {
            case ROUND_ROBIN:
                return assignRoundRobin(availableUsers, rule.getRuleId());
                
            case LOAD_BALANCED:
                return assignLoadBalanced(availableUsers, lead.getTenantId());
                
            case RANDOM:
                return assignRandom(availableUsers);
                
            case SKILL_BASED:
                return assignSkillBased(availableUsers, lead);
                
            case TERRITORY_BASED:
                return assignTerritoryBased(availableUsers, lead);
                
            default:
                return assignRoundRobin(availableUsers, rule.getRuleId());
        }
    }
    
    /**
     * Get available users from the rule configuration
     */
    private List<Long> getAvailableUsers(LeadAssignmentRule rule) {
        List<Long> userIds = rule.getAssignedUserIdsList();
        
        // Filter out users who are at capacity or unavailable
        return userIds.stream()
            .filter(userId -> isUserAvailable(userId, rule.getTenantId()))
            .collect(Collectors.toList());
    }
    
    /**
     * Check if a user is available for lead assignment
     */
    private boolean isUserAvailable(Long userId, Long tenantId) {
        Optional<UserLeadWorkload> workloadOpt = workloadRepository
            .findByTenantIdAndUserId(tenantId, userId);
        
        if (workloadOpt.isEmpty()) {
            // Create default workload entry
            UserLeadWorkload workload = new UserLeadWorkload();
            workload.setTenantId(tenantId);
            workload.setUserId(userId);
            workload.setIsAvailable(true);
            workload.setMaxLeadCapacity(50);
            workloadRepository.save(workload);
            return true;
        }
        
        UserLeadWorkload workload = workloadOpt.get();
        return workload.getIsAvailable() && 
               workload.getActiveLeadsCount() < workload.getMaxLeadCapacity();
    }
    
    /**
     * Round-robin assignment strategy
     */
    private Long assignRoundRobin(List<Long> userIds, Long ruleId) {
        int currentIndex = roundRobinCounters.getOrDefault(ruleId, 0);
        Long selectedUserId = userIds.get(currentIndex % userIds.size());
        
        // Update counter for next assignment
        roundRobinCounters.put(ruleId, (currentIndex + 1) % userIds.size());
        
        return selectedUserId;
    }
    
    /**
     * Load-balanced assignment strategy
     */
    private Long assignLoadBalanced(List<Long> userIds, Long tenantId) {
        return userIds.stream()
            .min(Comparator.comparing(userId -> getCurrentWorkload(userId, tenantId)))
            .orElse(userIds.get(0));
    }
    
    /**
     * Random assignment strategy
     */
    private Long assignRandom(List<Long> userIds) {
        Random random = new Random();
        return userIds.get(random.nextInt(userIds.size()));
    }
    
    /**
     * Skill-based assignment (placeholder - implement based on user skills)
     */
    private Long assignSkillBased(List<Long> userIds, Lead lead) {
        // For now, use load-balanced as fallback
        // In production, implement skill matching logic
        return assignLoadBalanced(userIds, lead.getTenantId());
    }
    
    /**
     * Territory-based assignment (placeholder - implement based on geography)
     */
    private Long assignTerritoryBased(List<Long> userIds, Lead lead) {
        // For now, use round-robin as fallback
        // In production, implement territory matching logic
        return assignRoundRobin(userIds, 0L);
    }
    
    /**
     * Get current workload for a user
     */
    private int getCurrentWorkload(Long userId, Long tenantId) {
        return workloadRepository.findByTenantIdAndUserId(tenantId, userId)
            .map(UserLeadWorkload::getActiveLeadsCount)
            .orElse(0);
    }
    
    /**
     * Assign to default user when no rules match
     */
    private Long assignToDefaultUser(Lead lead) {
        // In production, implement default user logic
        // For now, keep current owner or assign to creator
        return lead.getOwnerUserId() != null ? lead.getOwnerUserId() : UserContext.getCurrentUserId();
    }
    
    /**
     * Record assignment history
     */
    private void recordAssignmentHistory(Lead lead, LeadAssignmentRule rule, Long assignedUserId) {
        LeadAssignmentHistory history = new LeadAssignmentHistory();
        history.setTenantId(lead.getTenantId());
        history.setLeadId(lead.getLeadId());
        history.setRuleId(rule.getRuleId());
        history.setAssignedFromUserId(lead.getOwnerUserId());
        history.setAssignedToUserId(assignedUserId);
        history.setAssignmentReason("Automatic assignment via rule: " + rule.getRuleName());
        history.setAssignmentMethod(AssignmentMethod.RULE_BASED);
        history.setAssignedBy(UserContext.getCurrentUserId());
        
        assignmentHistoryRepository.save(history);
    }
    
    /**
     * Update user workload tracking
     */
    private void updateUserWorkload(Long userId, Long tenantId) {
        UserLeadWorkload workload = workloadRepository
            .findByTenantIdAndUserId(tenantId, userId)
            .orElse(new UserLeadWorkload());
        
        if (workload.getWorkloadId() == null) {
            workload.setTenantId(tenantId);
            workload.setUserId(userId);
            workload.setActiveLeadsCount(0);
            workload.setTotalLeadsAssigned(0);
        }
        
        workload.setActiveLeadsCount(workload.getActiveLeadsCount() + 1);
        workload.setTotalLeadsAssigned(workload.getTotalLeadsAssigned() + 1);
        workload.setLastActivityAt(LocalDateTime.now());
        
        workloadRepository.save(workload);
    }
    
    /**
     * Manually reassign a lead
     */
    public void reassignLead(Long leadId, Long newUserId, String reason) {
        Lead lead = leadRepository.findById(leadId)
            .orElseThrow(() -> new RuntimeException("Lead not found"));
        
        Long previousUserId = lead.getOwnerUserId();
        lead.setOwnerUserId(newUserId);
        leadRepository.save(lead);
        
        // Record manual assignment
        LeadAssignmentHistory history = new LeadAssignmentHistory();
        history.setTenantId(lead.getTenantId());
        history.setLeadId(leadId);
        history.setAssignedFromUserId(previousUserId);
        history.setAssignedToUserId(newUserId);
        history.setAssignmentReason(reason);
        history.setAssignmentMethod(AssignmentMethod.MANUAL);
        history.setAssignedBy(UserContext.getCurrentUserId());
        
        assignmentHistoryRepository.save(history);
        
        // Update workload tracking
        if (previousUserId != null) {
            decrementUserWorkload(previousUserId, lead.getTenantId());
        }
        updateUserWorkload(newUserId, lead.getTenantId());
        
        log.info("Lead {} manually reassigned from user {} to user {}", 
            leadId, previousUserId, newUserId);
    }
    
    /**
     * Decrement user workload when lead is reassigned
     */
    private void decrementUserWorkload(Long userId, Long tenantId) {
        workloadRepository.findByTenantIdAndUserId(tenantId, userId)
            .ifPresent(workload -> {
                workload.setActiveLeadsCount(Math.max(0, workload.getActiveLeadsCount() - 1));
                workloadRepository.save(workload);
            });
    }
}


