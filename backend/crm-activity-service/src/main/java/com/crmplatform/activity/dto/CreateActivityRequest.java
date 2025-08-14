package com.crmplatform.activity.dto;

import com.crmplatform.activity.entity.Activity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateActivityRequest {
    
    @NotNull(message = "Activity type is required")
    private Activity.ActivityType type;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    private String outcome;
    
    private ActivityAssociationsRequest associations;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityAssociationsRequest {
        private List<Long> contacts;
        private List<Long> accounts;
        private List<Long> deals;
    }
}
