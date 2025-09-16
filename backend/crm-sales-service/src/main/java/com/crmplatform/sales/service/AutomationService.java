package com.crmplatform.sales.service;

import com.crmplatform.sales.entity.Deal;
import com.crmplatform.sales.entity.PipelineStage;
import com.crmplatform.sales.repository.DealRepository;
import com.crmplatform.sales.repository.PipelineStageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutomationService {

    private final DealRepository dealRepository;
    private final PipelineStageRepository pipelineStageRepository;
    private final RestTemplate restTemplate;

    public void handleDealStageChanged(Long dealId, Long tenantId, Long oldStageId, Long newStageId, Long ownerUserId) {
        log.info("Processing deal stage change automation for deal: {}, from stage: {} to stage: {}", 
                dealId, oldStageId, newStageId);

        try {
            // Get deal and stage information
            Optional<Deal> dealOpt = dealRepository.findByTenantIdAndDealId(tenantId, dealId);
            Optional<PipelineStage> newStageOpt = pipelineStageRepository.findById(newStageId);

            if (dealOpt.isEmpty() || newStageOpt.isEmpty()) {
                log.warn("Deal or stage not found for automation processing");
                return;
            }

            Deal deal = dealOpt.get();
            PipelineStage newStage = newStageOpt.get();

            // Automation rules based on stage name
            String stageName = newStage.getStageName().toLowerCase();

            if (stageName.contains("proposal") || stageName.contains("quote")) {
                createFollowUpTask(deal, ownerUserId, "Follow up on proposal", 3);
                sendProposalEmail(deal, ownerUserId);
            } else if (stageName.contains("negotiation")) {
                createFollowUpTask(deal, ownerUserId, "Follow up on negotiation", 2);
            } else if (stageName.contains("demo") || stageName.contains("presentation")) {
                createFollowUpTask(deal, ownerUserId, "Schedule follow-up after demo", 1);
            } else if (stageName.contains("closed") || stageName.contains("won")) {
                createCelebrationActivity(deal, ownerUserId);
                updateDealLastActivityDate(deal);
            }

            log.info("Automation processing completed for deal: {}", dealId);

        } catch (Exception e) {
            log.error("Error processing deal stage change automation", e);
        }
    }

    public void handleActivityCreated(String activityId, Long tenantId, Long userId, String activityType) {
        log.info("Processing activity created automation for activity: {}, type: {}", activityId, activityType);

        try {
            if ("MEETING".equals(activityType)) {
                // Update last activity date on associated deals
                updateLastActivityDateForDeals(tenantId, userId);
            }

        } catch (Exception e) {
            log.error("Error processing activity created automation", e);
        }
    }

    private void createFollowUpTask(Deal deal, Long ownerUserId, String taskDescription, int daysFromNow) {
        try {
            log.info("Creating follow-up task for deal: {} - {}", deal.getDealId(), taskDescription);

            Map<String, Object> taskRequest = new HashMap<>();
            taskRequest.put("type", "TASK");
            taskRequest.put("content", taskDescription + " for deal: " + deal.getDealName());
            taskRequest.put("outcome", "Pending");

            Map<String, Object> associations = new HashMap<>();
            associations.put("deals", new Long[]{deal.getDealId()});
            associations.put("contacts", new Long[]{deal.getContactId()});
            associations.put("accounts", new Long[]{deal.getAccountId()});
            taskRequest.put("associations", associations);

            // Call activity service to create task
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Tenant-Id", deal.getTenantId().toString());
            headers.set("X-User-Id", ownerUserId.toString());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(taskRequest, headers);

            restTemplate.exchange(
                    "http://localhost:8084/api/v1/activities",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            log.info("Follow-up task created successfully for deal: {}", deal.getDealId());

        } catch (Exception e) {
            log.error("Failed to create follow-up task for deal: {}", deal.getDealId(), e);
        }
    }

    private void sendProposalEmail(Deal deal, Long ownerUserId) {
        try {
            log.info("Sending automated proposal email for deal: {}", deal.getDealId());

            // Call email service to send proposal email
            String emailUrl = "http://localhost:8085/api/email/proposal" +
                    "?tenantId=" + deal.getTenantId() +
                    "&contactId=" + deal.getContactId() +
                    "&dealId=" + deal.getDealId() +
                    "&userId=" + ownerUserId +
                    "&contactName=Contact" + deal.getContactId() +
                    "&dealName=" + deal.getDealName() +
                    "&dealAmount=" + deal.getAmount() +
                    "&proposalDetails=Please find our detailed proposal attached.";

            restTemplate.postForObject(emailUrl, null, String.class);

            log.info("Proposal email sent successfully for deal: {}", deal.getDealId());

        } catch (Exception e) {
            log.error("Failed to send proposal email for deal: {}", deal.getDealId(), e);
        }
    }

    private void createCelebrationActivity(Deal deal, Long ownerUserId) {
        try {
            log.info("Creating celebration activity for won deal: {}", deal.getDealId());

            Map<String, Object> activityRequest = new HashMap<>();
            activityRequest.put("type", "NOTE");
            activityRequest.put("content", "🎉 Deal won! " + deal.getDealName() + " - Amount: $" + deal.getAmount());
            activityRequest.put("outcome", "Won");

            Map<String, Object> associations = new HashMap<>();
            associations.put("deals", new Long[]{deal.getDealId()});
            associations.put("contacts", new Long[]{deal.getContactId()});
            associations.put("accounts", new Long[]{deal.getAccountId()});
            activityRequest.put("associations", associations);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Tenant-Id", deal.getTenantId().toString());
            headers.set("X-User-Id", ownerUserId.toString());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(activityRequest, headers);

            restTemplate.exchange(
                    "http://localhost:8084/api/v1/activities",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            log.info("Celebration activity created successfully for deal: {}", deal.getDealId());

        } catch (Exception e) {
            log.error("Failed to create celebration activity for deal: {}", deal.getDealId(), e);
        }
    }

    private void updateDealLastActivityDate(Deal deal) {
        try {
            log.info("Updating last activity date for deal: {}", deal.getDealId());
            // This would update a lastActivityDate field if it existed in the Deal entity
            // For now, we'll just log it as the field doesn't exist in current schema
            log.info("Last activity date would be updated to today for deal: {}", deal.getDealId());

        } catch (Exception e) {
            log.error("Failed to update last activity date for deal: {}", deal.getDealId(), e);
        }
    }

    private void updateLastActivityDateForDeals(Long tenantId, Long userId) {
        try {
            log.info("Updating last activity date for deals owned by user: {}", userId);
            // This would find deals by owner and update their last activity date
            // Implementation would depend on having a lastActivityDate field in Deal entity
            log.info("Last activity dates would be updated for user's deals: {}", userId);

        } catch (Exception e) {
            log.error("Failed to update last activity dates for user: {}", userId, e);
        }
    }
}
