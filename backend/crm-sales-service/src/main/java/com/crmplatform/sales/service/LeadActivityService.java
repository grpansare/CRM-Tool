package com.crmplatform.sales.service;

import com.crmplatform.common.security.UserContext;
import com.crmplatform.sales.entity.LeadDisposition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LeadActivityService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${crm.activity.service.url:http://localhost:8083}")
    private String activityServiceUrl;
    
    public void logLeadCreated(Long leadId, String leadName, String leadSource) {
        try {
            Map<String, Object> activityData = new HashMap<>();
            activityData.put("type", "LEAD_CREATED");
            activityData.put("content", String.format("Lead '%s' created from %s", leadName, leadSource));
            activityData.put("outcome", "LEAD_ADDED");
            
            Map<String, Object> associations = new HashMap<>();
            associations.put("leads", List.of(leadId));
            activityData.put("associations", associations);
            
            createActivity(activityData);
        } catch (Exception e) {
            // Log error but don't fail the main operation
            System.err.println("Failed to log lead creation activity: " + e.getMessage());
        }
    }
    
    public void logLeadStatusChange(Long leadId, String leadName, String oldStatus, String newStatus) {
        try {
            Map<String, Object> activityData = new HashMap<>();
            activityData.put("type", "LEAD_STATUS_CHANGE");
            activityData.put("content", String.format("Lead '%s' status changed from %s to %s", leadName, oldStatus, newStatus));
            activityData.put("outcome", "STATUS_UPDATED");
            
            Map<String, Object> associations = new HashMap<>();
            associations.put("leads", List.of(leadId));
            activityData.put("associations", associations);
            
            createActivity(activityData);
        } catch (Exception e) {
            System.err.println("Failed to log lead status change activity: " + e.getMessage());
        }
    }
    
    public void logLeadUpdated(Long leadId, String leadName, String changes) {
        try {
            Map<String, Object> activityData = new HashMap<>();
            activityData.put("type", "LEAD_UPDATED");
            activityData.put("content", String.format("Lead '%s' updated: %s", leadName, changes));
            activityData.put("outcome", "LEAD_MODIFIED");
            
            Map<String, Object> associations = new HashMap<>();
            associations.put("leads", List.of(leadId));
            activityData.put("associations", associations);
            
            createActivity(activityData);
        } catch (Exception e) {
            System.err.println("Failed to log lead update activity: " + e.getMessage());
        }
    }
    
    public void logLeadConverted(Long leadId, String leadName, Long contactId, Long accountId) {
        try {
            Map<String, Object> activityData = new HashMap<>();
            activityData.put("type", "CONVERSION");
            activityData.put("content", String.format("Lead '%s' converted to contact (ID: %d)%s", 
                leadName, contactId, 
                accountId != null ? " and account (ID: " + accountId + ")" : ""));
            activityData.put("outcome", "Converted");
            
            Map<String, Object> associations = new HashMap<>();
            associations.put("leads", List.of(leadId));
            if (contactId != null) {
                associations.put("contacts", List.of(contactId));
            }
            if (accountId != null) {
                associations.put("accounts", List.of(accountId));
            }
            activityData.put("associations", associations);
            
            createActivity(activityData);
        } catch (Exception e) {
            System.err.println("Failed to log lead conversion activity: " + e.getMessage());
        }
    }
    
    public void logLeadScoreUpdate(Long leadId, String leadName, int newScore) {
        try {
            Map<String, Object> activityData = new HashMap<>();
            activityData.put("type", "SCORE_UPDATE");
            activityData.put("content", String.format("Lead score updated for '%s' to %d/100", leadName, newScore));
            activityData.put("outcome", "Score Updated");
            
            Map<String, Object> associations = new HashMap<>();
            associations.put("leads", List.of(leadId));
            activityData.put("associations", associations);
            
            createActivity(activityData);
        } catch (Exception e) {
            System.err.println("Failed to log lead score update activity: " + e.getMessage());
        }
    }
    
    public void logLeadDeleted(Long leadId, String leadName) {
        try {
            Map<String, Object> activityData = new HashMap<>();
            activityData.put("type", "LEAD_DELETED");
            activityData.put("content", String.format("Lead '%s' was deleted", leadName));
            activityData.put("outcome", "LEAD_REMOVED");
            
            Map<String, Object> associations = new HashMap<>();
            associations.put("leads", List.of(leadId));
            activityData.put("associations", associations);
            
            createActivity(activityData);
        } catch (Exception e) {
            System.err.println("Failed to log lead deletion activity: " + e.getMessage());
        }
    }
    
    public void logLeadNote(Long leadId, String leadName, String note) {
        try {
            Map<String, Object> activityData = new HashMap<>();
            activityData.put("type", "NOTE");
            activityData.put("content", String.format("Note added to lead '%s': %s", leadName, note));
            activityData.put("outcome", "NOTE_ADDED");
            
            Map<String, Object> associations = new HashMap<>();
            associations.put("leads", List.of(leadId));
            activityData.put("associations", associations);
            
            createActivity(activityData);
        } catch (Exception e) {
            System.err.println("Failed to log lead note activity: " + e.getMessage());
        }
    }
    
    public void logLeadCall(Long leadId, String leadName, String callNotes, String outcome) {
        try {
            Map<String, Object> activityData = new HashMap<>();
            activityData.put("type", "CALL");
            activityData.put("content", String.format("Called lead '%s': %s", leadName, callNotes));
            activityData.put("outcome", outcome);
            
            Map<String, Object> associations = new HashMap<>();
            associations.put("leads", List.of(leadId));
            activityData.put("associations", associations);
            
            createActivity(activityData);
        } catch (Exception e) {
            System.err.println("Failed to log lead call activity: " + e.getMessage());
        }
    }
    
    public void logLeadEmail(Long leadId, String leadName, String subject, String outcome) {
        try {
            Map<String, Object> activityData = new HashMap<>();
            activityData.put("type", "EMAIL");
            activityData.put("content", String.format("Email sent to lead '%s': %s", leadName, subject));
            activityData.put("outcome", outcome);
            
            Map<String, Object> associations = new HashMap<>();
            associations.put("leads", List.of(leadId));
            activityData.put("associations", associations);
            
            createActivity(activityData);
        } catch (Exception e) {
            System.err.println("Failed to log lead email activity: " + e.getMessage());
        }
    }
    
    public void logDispositionUpdate(Long leadId, String leadName, LeadDisposition disposition, String notes) {
        try {
            Map<String, Object> activityData = new HashMap<>();
            activityData.put("type", "DISPOSITION_UPDATE");
            activityData.put("content", String.format("Lead '%s' disposition set to '%s'%s", 
                leadName, 
                disposition.getDisplayName(),
                notes != null && !notes.trim().isEmpty() ? " - " + notes : ""));
            activityData.put("outcome", disposition.getDisplayName());
            
            Map<String, Object> associations = new HashMap<>();
            associations.put("leads", List.of(leadId));
            activityData.put("associations", associations);
            
            createActivity(activityData);
        } catch (Exception e) {
            System.err.println("Failed to log disposition update activity: " + e.getMessage());
        }
    }
    
    private void createActivity(Map<String, Object> activityData) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-User-Id", UserContext.getCurrentUserId().toString());
            headers.set("X-Tenant-Id", UserContext.getCurrentTenantId().toString());
            headers.set("X-Username", UserContext.getCurrentUsername());
            headers.set("X-User-Role", UserContext.getCurrentUserRole());
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(activityData, headers);
            
            String url = activityServiceUrl + "/api/v1/activities";
            restTemplate.postForEntity(url, entity, Object.class);
            
        } catch (Exception e) {
            System.err.println("Failed to create activity: " + e.getMessage());
        }
    }
}
