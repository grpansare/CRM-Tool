package com.crmplatform.email.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String tenantId;
    
    @Column(nullable = false)
    private String fromEmail;
    
    @Column(nullable = false)
    private String toEmail;
    
    @Column
    private String ccEmail;
    
    @Column
    private String bccEmail;
    
    @Column(nullable = false)
    private String subject;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailType emailType;
    
    @Column
    private String errorMessage;
    
    @Column
    private String templateName;
    
    @Column
    private Integer retryCount = 0;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime sentAt;
    
    public enum EmailStatus {
        PENDING,
        SENT,
        FAILED,
        BOUNCED,
        DELIVERED
    }
    
    public enum EmailType {
        WELCOME,
        NOTIFICATION,
        MARKETING,
        SYSTEM_ALERT,
        PASSWORD_RESET,
        VERIFICATION
    }
}
