package com.crmplatform.contacts.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "account_contacts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountContact {
    
    @EmbeddedId
    private AccountContactId id;
    
    @Embeddable
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountContactId implements Serializable {
        
        @Column(name = "account_id")
        private Long accountId;
        
        @Column(name = "contact_id")
        private Long contactId;
        
        @Column(name = "tenant_id")
        private Long tenantId;
    }
} 