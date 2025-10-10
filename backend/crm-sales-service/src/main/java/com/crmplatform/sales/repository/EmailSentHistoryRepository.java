//package com.crmplatform.sales.repository;
//
//import com.crmplatform.sales.entity.EmailSentHistory;
//import com.crmplatform.sales.service.EmailSendStatus;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Repository
//public interface EmailSentHistoryRepository extends JpaRepository<EmailSentHistory, Long> {
//    
//    // Find emails by lead ID
//    List<EmailSentHistory> findByTenantIdAndLeadIdOrderBySentAtDesc(Long tenantId, Long leadId);
//    
//    // Find emails by contact ID
//    List<EmailSentHistory> findByTenantIdAndContactIdOrderBySentAtDesc(Long tenantId, Long contactId);
//    
//    // Find emails by recipient
//    List<EmailSentHistory> findByTenantIdAndRecipientEmailOrderBySentAtDesc(Long tenantId, String recipientEmail);
//    
//    // Find emails by sender
//    List<EmailSentHistory> findByTenantIdAndSentByOrderBySentAtDesc(Long tenantId, Long sentBy);
//    
//    // Find emails by status
//    List<EmailSentHistory> findByTenantIdAndSendStatusOrderBySentAtDesc(Long tenantId, EmailSendStatus sendStatus);
//    
//    // Find emails by template
//    List<EmailSentHistory> findByTenantIdAndTemplateIdOrderBySentAtDesc(Long tenantId, Long templateId);
//    
//    // Count emails by status
//    long countByTenantIdAndSendStatus(Long tenantId, EmailSendStatus sendStatus);
//    
//    // Count emails sent after date
//    long countByTenantIdAndSentAtAfter(Long tenantId, LocalDateTime after);
//    
//    // Find emails with pagination
//    Page<EmailSentHistory> findByTenantIdOrderBySentAtDesc(Long tenantId, Pageable pageable);
//    
//    // Find pending emails for retry
//    List<EmailSentHistory> findByTenantIdAndSendStatusAndCreatedAtBefore(
//        Long tenantId, EmailSendStatus sendStatus, LocalDateTime before);
//    
//    // Find by recipient, subject and status (for error handling)
//    EmailSentHistory findByRecipientEmailAndSubjectLineAndSendStatus(
//        String recipientEmail, String subjectLine, EmailSendStatus sendStatus);
//    
//    // Get email statistics by date range
//    @Query("SELECT COUNT(e) FROM EmailSentHistory e WHERE e.tenantId = :tenantId " +
//           "AND e.sendStatus = :status " +
//           "AND e.sentAt BETWEEN :startDate AND :endDate")
//    long countByStatusAndDateRange(@Param("tenantId") Long tenantId,
//                                  @Param("status") EmailSendStatus status,
//                                  @Param("startDate") LocalDateTime startDate,
//                                  @Param("endDate") LocalDateTime endDate);
//    
//    // Get recent email activity
//    @Query("SELECT e FROM EmailSentHistory e WHERE e.tenantId = :tenantId " +
//           "AND e.sendStatus IN ('SENT', 'DELIVERED', 'OPENED') " +
//           "ORDER BY e.sentAt DESC")
//    List<EmailSentHistory> findRecentEmailActivity(@Param("tenantId") Long tenantId, Pageable pageable);
//}
