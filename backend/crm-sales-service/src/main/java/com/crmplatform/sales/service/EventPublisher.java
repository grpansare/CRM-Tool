package com.crmplatform.sales.service;

import com.crmplatform.sales.entity.Deal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {
    
    private final StreamBridge streamBridge;
    
    public void publishDealCreated(Deal deal) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "Deal.Created");
        event.put("dealId", deal.getDealId());
        event.put("tenantId", deal.getTenantId());
        event.put("ownerUserId", deal.getOwnerUserId());
        event.put("stageId", deal.getStageId());
        event.put("amount", deal.getAmount());
        event.put("contactId", deal.getContactId());
        event.put("accountId", deal.getAccountId());
        event.put("timestamp", System.currentTimeMillis());
        
        streamBridge.send("deal-events", event);
        log.info("Published Deal.Created event for deal ID: {}", deal.getDealId());
    }
    
    public void publishDealStageChanged(Deal deal, Long oldStageId, Long newStageId) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "Deal.StageChanged");
        event.put("dealId", deal.getDealId());
        event.put("tenantId", deal.getTenantId());
        event.put("ownerUserId", deal.getOwnerUserId());
        event.put("oldStageId", oldStageId);
        event.put("newStageId", newStageId);
        event.put("amount", deal.getAmount());
        event.put("timestamp", System.currentTimeMillis());
        
        streamBridge.send("deal-events", event);
        log.info("Published Deal.StageChanged event for deal ID: {} from stage {} to {}", 
                deal.getDealId(), oldStageId, newStageId);
    }
} 