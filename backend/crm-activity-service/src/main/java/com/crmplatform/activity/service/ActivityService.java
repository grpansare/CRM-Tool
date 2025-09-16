package com.crmplatform.activity.service;

import com.crmplatform.activity.dto.ActivityResponse;
import com.crmplatform.activity.dto.CreateActivityRequest;
import com.crmplatform.activity.entity.Activity;
import com.crmplatform.activity.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityService {
    
    private final ActivityRepository activityRepository;
    // private final EventPublisher eventPublisher; // Disabled for now
    
    public ActivityResponse createActivity(CreateActivityRequest request, Long tenantId, Long userId) {
        log.info("Creating activity for tenant: {}, user: {}, type: {}", tenantId, userId, request.getType());
        
        // Generate human-readable activity ID
        String activityId = generateActivityId();
        
        // Build activity associations
        Activity.ActivityAssociations associations = null;
        if (request.getAssociations() != null) {
            associations = Activity.ActivityAssociations.builder()
                    .contacts(request.getAssociations().getContacts())
                    .accounts(request.getAssociations().getAccounts())
                    .deals(request.getAssociations().getDeals())
                    .leads(request.getAssociations().getLeads())
                    .build();
        }
        
        // Create activity entity
        Activity activity = Activity.builder()
                .tenantId(tenantId)
                .activityId(activityId)
                .userId(userId)
                .type(request.getType())
                .timestamp(LocalDateTime.now())
                .content(request.getContent())
                .outcome(request.getOutcome())
                .associations(associations)
                .build();
        
        // Save to database
        Activity savedActivity = activityRepository.save(activity);
        log.info("Activity created successfully with ID: {}", savedActivity.getId());
        
        // Publish Activity.Created event - Disabled for now
        // eventPublisher.publishActivityCreated(savedActivity);
        
        return mapToResponse(savedActivity);
    }
    
    public Page<ActivityResponse> getContactTimeline(Long contactId, Long tenantId, int page, int size) {
        log.info("Retrieving timeline for contact: {}, tenant: {}", contactId, tenantId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<Activity> activities = activityRepository.findByTenantIdAndContactId(tenantId, contactId, pageable);
        
        return activities.map(this::mapToResponse);
    }
    
    public Page<ActivityResponse> getAccountTimeline(Long accountId, Long tenantId, int page, int size) {
        log.info("Retrieving timeline for account: {}, tenant: {}", accountId, tenantId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<Activity> activities = activityRepository.findByTenantIdAndAccountId(tenantId, accountId, pageable);
        
        return activities.map(this::mapToResponse);
    }
    
    public Page<ActivityResponse> getDealTimeline(Long dealId, Long tenantId, int page, int size) {
        log.info("Retrieving timeline for deal: {}, tenant: {}", dealId, tenantId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<Activity> activities = activityRepository.findByTenantIdAndDealId(tenantId, dealId, pageable);
        
        return activities.map(this::mapToResponse);
    }
    
    public Page<ActivityResponse> getLeadTimeline(Long leadId, Long tenantId, int page, int size) {
        log.info("Retrieving timeline for lead: {}, tenant: {}", leadId, tenantId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<Activity> activities = activityRepository.findByTenantIdAndLeadId(tenantId, leadId, pageable);
        
        return activities.map(this::mapToResponse);
    }
    
    public Page<ActivityResponse> getUserActivities(Long userId, Long tenantId, int page, int size) {
        log.info("Retrieving activities for user: {}, tenant: {}", userId, tenantId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<Activity> activities = activityRepository.findByTenantIdAndUserIdOrderByTimestampDesc(tenantId, userId, pageable);
        
        return activities.map(this::mapToResponse);
    }
    
    // Company-wide activity tracking methods
    public Page<ActivityResponse> getCompanyActivities(List<Long> accountIds, Long tenantId, int page, int size) {
        log.info("Retrieving company-wide activities for {} accounts, tenant: {}", accountIds.size(), tenantId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<Activity> activities = activityRepository.findByTenantIdAndAccountIds(tenantId, accountIds, pageable);
        
        return activities.map(this::mapToResponse);
    }
    
    public List<ActivityResponse> getRecentCompanyActivities(List<Long> accountIds, Long tenantId, int days) {
        log.info("Retrieving recent company activities for {} accounts, tenant: {}, days: {}", accountIds.size(), tenantId, days);
        
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<Activity> activities = activityRepository.findRecentActivitiesByAccountIds(tenantId, accountIds, since);
        
        return activities.stream()
                .map(this::mapToResponse)
                .toList();
    }
    
    public Long getActivityCountForAccount(Long accountId, Long tenantId) {
        log.info("Getting activity count for account: {}, tenant: {}", accountId, tenantId);
        
        return activityRepository.countByTenantIdAndAccountId(tenantId, accountId);
    }
    
    private String generateActivityId() {
        return "act_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    private ActivityResponse mapToResponse(Activity activity) {
        ActivityResponse.ActivityAssociationsResponse associations = null;
        if (activity.getAssociations() != null) {
            associations = ActivityResponse.ActivityAssociationsResponse.builder()
                    .contacts(activity.getAssociations().getContacts())
                    .accounts(activity.getAssociations().getAccounts())
                    .deals(activity.getAssociations().getDeals())
                    .leads(activity.getAssociations().getLeads())
                    .build();
        }
        
        return ActivityResponse.builder()
                .id(activity.getId())
                .activityId(activity.getActivityId())
                .userId(activity.getUserId())
                .type(activity.getType())
                .timestamp(activity.getTimestamp())
                .content(activity.getContent())
                .outcome(activity.getOutcome())
                .associations(associations)
                .build();
    }
}
