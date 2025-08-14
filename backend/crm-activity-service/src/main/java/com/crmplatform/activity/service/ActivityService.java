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
        
        // Publish Activity.Created event - DISABLED FOR NOW
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
    
    public Page<ActivityResponse> getUserActivities(Long userId, Long tenantId, int page, int size) {
        log.info("Retrieving activities for user: {}, tenant: {}", userId, tenantId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<Activity> activities = activityRepository.findByTenantIdAndUserIdOrderByTimestampDesc(tenantId, userId, pageable);
        
        return activities.map(this::mapToResponse);
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
