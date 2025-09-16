package com.crmplatform.sales.listener;

import com.crmplatform.sales.service.AutomationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
// import org.springframework.cloud.stream.annotation.StreamListener;
// import org.springframework.messaging.handler.annotation.Payload;
// import org.springframework.stereotype.Component;

import java.util.Map;

// @Component - Disabled until RabbitMQ is configured
@RequiredArgsConstructor
@Slf4j
public class DealEventListener {

    private final AutomationService automationService;

    // @StreamListener("deal-events") - Disabled until RabbitMQ is configured
    public void handleDealEvent(/* @Payload */ Map<String, Object> event) {
        try {
            String eventType = (String) event.get("eventType");
            log.info("Received deal event: {}", eventType);

            if ("Deal.StageChanged".equals(eventType)) {
                Long dealId = ((Number) event.get("dealId")).longValue();
                Long tenantId = ((Number) event.get("tenantId")).longValue();
                Long oldStageId = ((Number) event.get("oldStageId")).longValue();
                Long newStageId = ((Number) event.get("newStageId")).longValue();
                Long ownerUserId = ((Number) event.get("ownerUserId")).longValue();

                automationService.handleDealStageChanged(dealId, tenantId, oldStageId, newStageId, ownerUserId);
            }

        } catch (Exception e) {
            log.error("Error processing deal event", e);
        }
    }
}
