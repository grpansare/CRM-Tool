package com.crmplatform.activity.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "activities")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Activity {
    
    @Id
    private String id;
    
    @Field("tenant_id")
    private Long tenantId;
    
    @Field("activity_id")
    private String activityId; // Human-readable ID like "act_12345"
    
    @Field("user_id")
    private Long userId; // User who performed the activity
    
    @Field("type")
    private ActivityType type; // CALL, EMAIL, MEETING, NOTE, TASK
    
    @CreatedDate
    @Field("timestamp")
    private LocalDateTime timestamp;
    
    @Field("content")
    private String content; // Activity description/notes
    
    @Field("outcome")
    private String outcome; // e.g., 'Connected', 'Left Voicemail', 'Scheduled Demo'
    
    @Field("associations")
    private ActivityAssociations associations;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityAssociations {
        private List<Long> contacts; // Array of contact_ids
        private List<Long> accounts; // Array of account_ids  
        private List<Long> deals;    // Array of deal_ids
    }
    
    public enum ActivityType {
        CALL, EMAIL, MEETING, NOTE, TASK
    }
}
