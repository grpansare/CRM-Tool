package com.crmplatform.activity.service;

import com.crmplatform.activity.dto.ActivityResponse;
import com.crmplatform.activity.dto.CreateActivityRequest;
import com.crmplatform.activity.entity.Activity;
import com.crmplatform.activity.repository.ActivityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {
    
    @Mock
    private ActivityRepository activityRepository;
    
    @Mock
    private EventPublisher eventPublisher;
    
    @InjectMocks
    private ActivityService activityService;
    
    private CreateActivityRequest createActivityRequest;
    private Activity activity;
    private Long tenantId = 1L;
    private Long userId = 100L;
    private Long contactId = 200L;
    
    @BeforeEach
    void setUp() {
        createActivityRequest = CreateActivityRequest.builder()
                .type(Activity.ActivityType.CALL)
                .content("Called John Smith about the proposal")
                .outcome("Connected")
                .associations(CreateActivityRequest.ActivityAssociationsRequest.builder()
                        .contacts(List.of(contactId))
                        .build())
                .build();
        
        activity = Activity.builder()
                .id("507f1f77bcf86cd799439011")
                .tenantId(tenantId)
                .activityId("act_12345678")
                .userId(userId)
                .type(Activity.ActivityType.CALL)
                .timestamp(LocalDateTime.now())
                .content("Called John Smith about the proposal")
                .outcome("Connected")
                .associations(Activity.ActivityAssociations.builder()
                        .contacts(List.of(contactId))
                        .build())
                .build();
    }
    
    @Test
    void createActivity_ShouldCreateAndReturnActivity() {
        // Given
        when(activityRepository.save(any(Activity.class))).thenReturn(activity);
        
        // When
        ActivityResponse response = activityService.createActivity(createActivityRequest, tenantId, userId);
        
        // Then
        assertNotNull(response);
        assertEquals(activity.getId(), response.getId());
        assertEquals(activity.getType(), response.getType());
        assertEquals(activity.getContent(), response.getContent());
        assertEquals(activity.getOutcome(), response.getOutcome());
        assertEquals(activity.getUserId(), response.getUserId());
        
        verify(activityRepository).save(any(Activity.class));
        verify(eventPublisher).publishActivityCreated(any(Activity.class));
    }
    
    @Test
    void getContactTimeline_ShouldReturnPagedActivities() {
        // Given
        List<Activity> activities = Arrays.asList(activity);
        Page<Activity> activityPage = new PageImpl<>(activities);
        Pageable pageable = PageRequest.of(0, 20);
        
        when(activityRepository.findByTenantIdAndContactId(eq(tenantId), eq(contactId), any(Pageable.class)))
                .thenReturn(activityPage);
        
        // When
        Page<ActivityResponse> response = activityService.getContactTimeline(contactId, tenantId, 0, 20);
        
        // Then
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(activity.getId(), response.getContent().get(0).getId());
        
        verify(activityRepository).findByTenantIdAndContactId(eq(tenantId), eq(contactId), any(Pageable.class));
    }
    
    @Test
    void getUserActivities_ShouldReturnUserActivities() {
        // Given
        List<Activity> activities = Arrays.asList(activity);
        Page<Activity> activityPage = new PageImpl<>(activities);
        
        when(activityRepository.findByTenantIdAndUserIdOrderByTimestampDesc(eq(tenantId), eq(userId), any(Pageable.class)))
                .thenReturn(activityPage);
        
        // When
        Page<ActivityResponse> response = activityService.getUserActivities(userId, tenantId, 0, 20);
        
        // Then
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(activity.getUserId(), response.getContent().get(0).getUserId());
        
        verify(activityRepository).findByTenantIdAndUserIdOrderByTimestampDesc(eq(tenantId), eq(userId), any(Pageable.class));
    }
}
