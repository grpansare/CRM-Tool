package com.crmplatform.contacts.service;

import com.crmplatform.contacts.entity.Account;
import com.crmplatform.contacts.entity.Contact;
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
    
    public void publishContactCreated(Contact contact) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "Contact.Created");
        event.put("contactId", contact.getContactId());
        event.put("tenantId", contact.getTenantId());
        event.put("ownerUserId", contact.getOwnerUserId());
        event.put("email", contact.getPrimaryEmail());
        event.put("timestamp", System.currentTimeMillis());
        
        streamBridge.send("contact-events", event);
        log.info("Published Contact.Created event for contact ID: {}", contact.getContactId());
    }
    
    public void publishAccountCreated(Account account) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "Account.Created");
        event.put("accountId", account.getAccountId());
        event.put("tenantId", account.getTenantId());
        event.put("ownerUserId", account.getOwnerUserId());
        event.put("accountName", account.getAccountName());
        event.put("timestamp", System.currentTimeMillis());
        
        streamBridge.send("account-events", event);
        log.info("Published Account.Created event for account ID: {}", account.getAccountId());
    }
} 