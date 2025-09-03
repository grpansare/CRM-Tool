package com.crmplatform.contacts.service;

import com.crmplatform.contacts.entity.Account;
import com.crmplatform.contacts.entity.Contact;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EventPublisher {
    
    public void publishContactCreated(Contact contact) {
        // Event publishing disabled - RabbitMQ not configured
        log.info("Contact created event (publishing disabled): contactId={}, tenantId={}", 
                contact.getContactId(), contact.getTenantId());
    }
    
    public void publishAccountCreated(Account account) {
        // Event publishing disabled - RabbitMQ not configured
        log.info("Account created event (publishing disabled): accountId={}, tenantId={}", 
                account.getAccountId(), account.getTenantId());
    }
} 