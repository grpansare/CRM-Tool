package com.crmplatform.activity.controller;

import com.crmplatform.activity.dto.ActivityResponse;
import com.crmplatform.activity.dto.CreateActivityRequest;
import com.crmplatform.activity.entity.Activity;
import com.crmplatform.activity.service.ActivityService;
import com.crmplatform.common.security.TenantContext;
import com.crmplatform.common.security.UserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ActivityController.class)
class ActivityControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private ActivityService activityService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private CreateActivityRequest createActivityRequest;
    private ActivityResponse activityResponse;
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
        
        activityResponse = ActivityResponse.builder()
                .id("507f1f77bcf86cd799439011")
                .activityId("act_12345678")
                .userId(userId)
                .type(Activity.ActivityType.CALL)
                .timestamp(LocalDateTime.now())
                .content("Called John Smith about the proposal")
                .outcome("Connected")
                .associations(ActivityResponse.ActivityAssociationsResponse.builder()
                        .contacts(List.of(contactId))
                        .build())
                .build();
    }
    
    @Test
    @WithMockUser(roles = "USER")
    void createActivity_ShouldReturnCreatedActivity() throws Exception {
        // Given
        try (var tenantContext = mockStatic(TenantContext.class);
             var userContext = mockStatic(UserContext.class)) {
            
            tenantContext.when(TenantContext::getCurrentTenantId).thenReturn(tenantId);
            userContext.when(UserContext::getCurrentUserId).thenReturn(userId);
            
            when(activityService.createActivity(any(CreateActivityRequest.class), eq(tenantId), eq(userId)))
                    .thenReturn(activityResponse);
            
            // When & Then
            mockMvc.perform(post("/api/v1/activities")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createActivityRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(activityResponse.getId()))
                    .andExpect(jsonPath("$.data.type").value("CALL"))
                    .andExpect(jsonPath("$.data.content").value("Called John Smith about the proposal"));
            
            verify(activityService).createActivity(any(CreateActivityRequest.class), eq(tenantId), eq(userId));
        }
    }
    
    @Test
    @WithMockUser(roles = "USER")
    void getContactTimeline_ShouldReturnPagedActivities() throws Exception {
        // Given
        List<ActivityResponse> activities = Arrays.asList(activityResponse);
        Page<ActivityResponse> activityPage = new PageImpl<>(activities);
        
        try (var tenantContext = mockStatic(TenantContext.class)) {
            tenantContext.when(TenantContext::getCurrentTenantId).thenReturn(tenantId);
            
            when(activityService.getContactTimeline(eq(contactId), eq(tenantId), eq(0), eq(20)))
                    .thenReturn(activityPage);
            
            // When & Then
            mockMvc.perform(get("/api/v1/activities/contacts/{contactId}/timeline", contactId)
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpected(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content[0].id").value(activityResponse.getId()));
            
            verify(activityService).getContactTimeline(eq(contactId), eq(tenantId), eq(0), eq(20));
        }
    }
}
