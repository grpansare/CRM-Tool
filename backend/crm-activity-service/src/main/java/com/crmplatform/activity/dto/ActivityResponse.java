package com.crmplatform.activity.dto;

import com.crmplatform.activity.entity.Activity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityResponse {
    
    private String id;
    private String activityId;
    private Long userId;
    private Activity.ActivityType type;
    private LocalDateTime timestamp;
    private String content;
    private String outcome;
    private ActivityAssociationsResponse associations;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityAssociationsResponse {
        private List<Long> contacts;
        private List<Long> accounts;
        private List<Long> deals;
        private List<Long> leads;
    }
}
