package com.crmplatform.email.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_templates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String tenantId;
    
    @Column(nullable = false, unique = true)
    private String templateName;
    
    @Column(nullable = false)
    private String subject;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String htmlContent;
    
    @Column(columnDefinition = "TEXT")
    private String textContent;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TemplateType templateType;
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    public enum TemplateType {
        WELCOME,
        PASSWORD_RESET,
        ACCOUNT_VERIFICATION,
        NOTIFICATION,
        MARKETING,
        SYSTEM_ALERT
    }
}
