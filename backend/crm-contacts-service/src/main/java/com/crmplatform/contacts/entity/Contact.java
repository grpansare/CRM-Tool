package com.crmplatform.contacts.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "contacts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contact {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contact_id")
    private Long contactId;
    
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    
    @Column(name = "first_name", length = 100)
    private String firstName;
    
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;
    
    @Column(name = "primary_email", length = 255)
    private String primaryEmail;
    
    @Column(name = "phone_number", length = 50)
    private String phoneNumber;
    
    @Column(name = "job_title", length = 100)
    private String jobTitle;
    
    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
} 