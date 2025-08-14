package com.crmplatform.activity.service;

import com.crmplatform.activity.entity.Activity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {
    
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${crm.events.exchange:crm.events}")
    private String eventsExchange;
    
    @Value("${crm.events.activity.created.routing-key:activity.created}")
    private String activityCreatedRoutingKey;
    
    public void publishActivityCreated(Activity activity) {
        try {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("eventType", "Activity.Created");
            eventData.put("timestamp", activity.getTimestamp());
            eventData.put("tenantId", activity.getTenantId());
            eventData.put("activityId", activity.getActivityId());
            eventData.put("userId", activity.getUserId());
            eventData.put("type", activity.getType().toString());
            eventData.put("content", activity.getContent());
            eventData.put("outcome", activity.getOutcome());
            eventData.put("associations", activity.getAssociations());
            
            String eventJson = objectMapper.writeValueAsString(eventData);
            
            rabbitTemplate.convertAndSend(eventsExchange, activityCreatedRoutingKey, eventJson);
            
            log.info("Published Activity.Created event for activity: {}", activity.getActivityId());
            
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize Activity.Created event for activity: {}", activity.getActivityId(), e);
        } catch (Exception e) {
            log.error("Failed to publish Activity.Created event for activity: {}", activity.getActivityId(), e);
        }
    }
}
